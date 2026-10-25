package ca.kallistesoft.hunt.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** A product as Play sells it: its id, localized price, and what launching the purchase needs. */
internal class StoreProduct(val id: String, val price: String, val details: ProductDetails?)

/** A purchase as Play reports it. */
internal data class StorePurchase(val token: String, val productIds: List<String>, val state: State, val acknowledged: Boolean) {
    enum class State { PURCHASED, PENDING, OTHER }
}

/** What Play sent after a purchase sheet closed (or a pending purchase changed). */
internal data class PurchaseUpdate(val code: Int, val purchases: List<StorePurchase>)

/** Google Play Billing, as [PlayPurchases] needs it. Response codes are [BillingResponseCode]'s. */
internal interface BillingGateway {
    val updates: SharedFlow<PurchaseUpdate>

    /** Connects to Play, if not already: false when Play can't be reached (offline, no Play Store). */
    suspend fun connect(): Boolean

    /** The one-time products [ids] Play sells to this user; null when Play couldn't answer. */
    suspend fun products(ids: List<String>): List<StoreProduct>?

    /** Opens the purchase sheet; the result comes in [updates]. Returns the launch's response code. */
    fun launch(activity: Activity, product: StoreProduct): Int

    /** What the account owns or has pending (one-time products); null when Play couldn't answer. */
    suspend fun purchases(): List<StorePurchase>?

    suspend fun acknowledge(token: String): Boolean

    suspend fun consume(token: String): Boolean
}

/** [BillingGateway] on Google's [BillingClient]; [newClient] builds it with the purchases listener. */
internal class GoogleBillingGateway(
    newClient: (listener: (BillingResult, List<Purchase>?) -> Unit) -> BillingClient,
) : BillingGateway {
    private val _updates = MutableSharedFlow<PurchaseUpdate>(extraBufferCapacity = 8)
    override val updates: SharedFlow<PurchaseUpdate> = _updates

    private val client = newClient { result, purchases ->
        _updates.tryEmit(PurchaseUpdate(result.responseCode, purchases.orEmpty().map(::storePurchase)))
    }

    override suspend fun connect(): Boolean {
        if (client.isReady) return true
        return suspendCancellableCoroutine { done ->
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (done.isActive) done.resume(result.responseCode == BillingResponseCode.OK)
                }

                override fun onBillingServiceDisconnected() {
                    if (done.isActive) done.resume(false)
                }
            })
        }
    }

    override suspend fun products(ids: List<String>): List<StoreProduct>? = suspendCancellableCoroutine { done ->
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                ids.map { QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(ProductType.INAPP).build() },
            )
            .build()
        client.queryProductDetailsAsync(params) { result, details ->
            done.resume(
                if (result.responseCode != BillingResponseCode.OK) {
                    null
                } else {
                    details.productDetailsList.mapNotNull { d ->
                        d.oneTimePurchaseOfferDetails?.let { StoreProduct(d.productId, it.formattedPrice, d) }
                    }
                },
            )
        }
    }

    override fun launch(activity: Activity, product: StoreProduct): Int {
        val details = product.details ?: return BillingResponseCode.ITEM_UNAVAILABLE
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).build()))
            .build()
        return client.launchBillingFlow(activity, params).responseCode
    }

    override suspend fun purchases(): List<StorePurchase>? = suspendCancellableCoroutine { done ->
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(ProductType.INAPP).build()) { result, list ->
            done.resume(if (result.responseCode == BillingResponseCode.OK) list.map(::storePurchase) else null)
        }
    }

    override suspend fun acknowledge(token: String): Boolean = suspendCancellableCoroutine { done ->
        client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(token).build()) { result ->
            done.resume(result.responseCode == BillingResponseCode.OK)
        }
    }

    override suspend fun consume(token: String): Boolean = suspendCancellableCoroutine { done ->
        client.consumeAsync(ConsumeParams.newBuilder().setPurchaseToken(token).build()) { result, _ ->
            done.resume(result.responseCode == BillingResponseCode.OK)
        }
    }

    private fun storePurchase(p: Purchase) = StorePurchase(
        token = p.purchaseToken,
        productIds = p.products,
        state = when (p.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> StorePurchase.State.PURCHASED
            Purchase.PurchaseState.PENDING -> StorePurchase.State.PENDING
            else -> StorePurchase.State.OTHER
        },
        acknowledged = p.isAcknowledged,
    )

    companion object {
        /** The real client: pending one-time purchases allowed, reconnecting by itself when Play drops it. */
        fun create(context: Context) = GoogleBillingGateway { listener ->
            BillingClient.newBuilder(context)
                .setListener { result, purchases -> listener(result, purchases) }
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                .enableAutoServiceReconnection()
                .build()
        }
    }
}
