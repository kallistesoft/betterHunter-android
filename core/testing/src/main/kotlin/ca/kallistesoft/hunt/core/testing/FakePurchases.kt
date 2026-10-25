package ca.kallistesoft.hunt.core.testing

import android.app.Activity
import ca.kallistesoft.hunt.core.billing.AdFreeProduct
import ca.kallistesoft.hunt.core.billing.BuyResult
import ca.kallistesoft.hunt.core.billing.Purchases
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Purchases that answer [result] and, when bought, give the entitlement in [userData] like Play would. With [onBuy] set,
 * Play's sheet stays open until it's completed.
 */
class FakePurchases(
    private val userData: FakeUserDataRepository,
    var result: BuyResult = BuyResult.BOUGHT,
    var restores: Boolean = true,
) : Purchases {
    override val prices = MutableStateFlow(mapOf(AdFreeProduct.WEEK to "CA$1.99", AdFreeProduct.FOREVER to "CA$14.99"))
    val bought = mutableListOf<AdFreeProduct>()
    var refreshed = 0
        private set
    var onBuy: CompletableDeferred<Unit>? = null

    override fun refresh() {
        refreshed++
    }

    override suspend fun buy(activity: Activity, product: AdFreeProduct): BuyResult {
        onBuy?.await()
        if (result == BuyResult.BOUGHT) {
            bought += product
            when (product) {
                AdFreeProduct.WEEK -> userData.addWeekPass("token-${bought.size}", TEST_NOW)
                AdFreeProduct.FOREVER -> userData.setAdFreeForever(true)
            }
        }
        return result
    }

    override suspend fun restore(): Boolean = restores
}
