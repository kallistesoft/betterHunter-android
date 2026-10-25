package ca.kallistesoft.hunt.core.billing

import android.app.Activity
import ca.kallistesoft.hunt.core.data.Now
import ca.kallistesoft.hunt.core.data.UserDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Purchases without Play, for debug builds (their application id has no products in Play): buying gives the
 * entitlement at once and charges nothing. The prices shown are the ones set in Play Console.
 */
class TestPurchases(private val userData: UserDataRepository, private val now: Now) : Purchases {
    override val prices: StateFlow<Map<AdFreeProduct, String>> =
        MutableStateFlow(mapOf(AdFreeProduct.WEEK to "CA$1.99", AdFreeProduct.FOREVER to "CA$14.99")).asStateFlow()

    override fun refresh() = Unit

    override suspend fun buy(activity: Activity, product: AdFreeProduct): BuyResult {
        when (product) {
            AdFreeProduct.WEEK -> userData.addWeekPass("test-${now()}", now())
            AdFreeProduct.FOREVER -> userData.setAdFreeForever(true)
        }
        return BuyResult.BOUGHT
    }

    override suspend fun restore(): Boolean = true
}
