package ca.kallistesoft.hunt.core.billing

import android.app.Activity
import ca.kallistesoft.hunt.core.data.Now
import ca.kallistesoft.hunt.core.data.UserDataRepository
import com.android.billingclient.api.BillingClient.BillingResponseCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The purchase rules on top of Play ([gateway]), P15:
 * - "Remove ads forever" is acknowledged (or Play refunds it after 3 days) and follows what Play says it owns, so a
 *   refund takes it back;
 * - a 7-day pass is counted once (by its token), then consumed so it can be bought again; it adds 7 days from now, or
 *   after a pass still running. Counted before consuming: a consume that fails is retried at the next launch;
 * - a pending purchase counts once it's paid, whenever Play reports it (an update, or the next launch).
 */
internal class PlayPurchases(
    private val gateway: BillingGateway,
    private val userData: UserDataRepository,
    private val now: Now,
    private val scope: CoroutineScope,
) : Purchases {
    private val _prices = MutableStateFlow<Map<AdFreeProduct, String>>(emptyMap())
    override val prices: StateFlow<Map<AdFreeProduct, String>> = _prices.asStateFlow()

    private var products: Map<AdFreeProduct, StoreProduct> = emptyMap()
    private var waiting: CompletableDeferred<PurchaseUpdate>? = null
    private var listening = false

    override fun refresh() {
        listen()
        scope.launch {
            if (!gateway.connect()) return@launch
            loadProducts()
            gateway.purchases()?.let { owned -> apply(owned, fromQuery = true) }
        }
    }

    override suspend fun buy(activity: Activity, product: AdFreeProduct): BuyResult {
        listen()
        if (!gateway.connect()) return BuyResult.UNAVAILABLE
        val store = products[product] ?: loadProducts()[product] ?: return BuyResult.UNAVAILABLE
        val update = CompletableDeferred<PurchaseUpdate>().also { waiting = it }
        val launched = gateway.launch(activity, store)
        val result = if (launched == BillingResponseCode.OK) update.await() else PurchaseUpdate(launched, emptyList())
        waiting = null
        return when (result.code) {
            BillingResponseCode.OK -> {
                val mine = result.purchases.filter { product.productId in it.productIds }
                when {
                    mine.any { it.state == StorePurchase.State.PURCHASED } -> BuyResult.BOUGHT
                    mine.any { it.state == StorePurchase.State.PENDING } -> BuyResult.PENDING
                    else -> BuyResult.FAILED
                }
            }
            BillingResponseCode.USER_CANCELED -> BuyResult.CANCELLED
            BillingResponseCode.ITEM_ALREADY_OWNED -> if (restore()) BuyResult.BOUGHT else BuyResult.FAILED
            BillingResponseCode.SERVICE_UNAVAILABLE, BillingResponseCode.SERVICE_DISCONNECTED,
            BillingResponseCode.BILLING_UNAVAILABLE, BillingResponseCode.ITEM_UNAVAILABLE,
            BillingResponseCode.NETWORK_ERROR,
            -> BuyResult.UNAVAILABLE
            else -> BuyResult.FAILED
        }
    }

    override suspend fun restore(): Boolean {
        if (!gateway.connect()) return false
        val owned = gateway.purchases() ?: return false
        apply(owned, fromQuery = true)
        return true
    }

    /** Purchases reported by Play, applied as they come (a pending one paid later, too) and handed to [buy]. */
    private fun listen() {
        if (listening) return
        listening = true
        scope.launch {
            gateway.updates.collect { update ->
                if (update.code == BillingResponseCode.OK) apply(update.purchases, fromQuery = false)
                waiting?.complete(update)
                waiting = null
            }
        }
    }

    private suspend fun loadProducts(): Map<AdFreeProduct, StoreProduct> {
        val found = gateway.products(AdFreeProduct.entries.map { it.productId }) ?: return products
        products = found.mapNotNull { p -> AdFreeProduct.of(p.id)?.let { it to p } }.toMap()
        _prices.value = products.mapValues { it.value.price }
        return products
    }

    /** [fromQuery]: everything the account owns, so "forever" missing from it means it was refunded. */
    private suspend fun apply(purchases: List<StorePurchase>, fromQuery: Boolean) {
        val paid = purchases.filter { it.state == StorePurchase.State.PURCHASED }
        val forever = paid.filter { AdFreeProduct.FOREVER.productId in it.productIds }
        if (fromQuery || forever.isNotEmpty()) userData.setAdFreeForever(forever.isNotEmpty())
        forever.filterNot { it.acknowledged }.forEach { gateway.acknowledge(it.token) }
        paid.filter { AdFreeProduct.WEEK.productId in it.productIds }.forEach {
            userData.addWeekPass(it.token, now())
            gateway.consume(it.token)
        }
    }
}
