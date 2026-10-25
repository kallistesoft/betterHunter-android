package ca.kallistesoft.hunt.core.billing

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

/**
 * What the user can hold (P15). Purchases give entitlements; the app checks entitlements, never products, so a later
 * Pro can also give [NO_ADS] without touching what was sold.
 */
enum class Entitlement { NO_ADS }

/** The products sold in Google Play (Play Console → Monetize → One-time products), and what each one gives. */
enum class AdFreeProduct(val productId: String) {
    /** CA$1.99: 7 days without ads, kept on this device only (consumed, so it can be bought again). */
    WEEK("remove_ads_week"),

    /** CA$14.99: no ads, forever; Play restores it on the user's other devices. */
    FOREVER("remove_ads_forever"),
    ;

    companion object {
        fun of(productId: String): AdFreeProduct? = entries.firstOrNull { it.productId == productId }
    }
}

/** What came of a purchase. */
enum class BuyResult {
    /** Paid: ads are off. */
    BOUGHT,

    /** Waiting for a payment made outside the app (cash in some countries): ads stop once it's paid. */
    PENDING,

    /** The user closed Play's purchase sheet. */
    CANCELLED,

    /** Play can't sell it right now (offline, Play Store missing or not signed in, product not set up). */
    UNAVAILABLE,

    /** Something else went wrong. */
    FAILED,
}

/** Remove ads, bought in Google Play. */
interface Purchases {
    /** Play's localized price per product; empty until Play answered (or when it can't). */
    val prices: StateFlow<Map<AdFreeProduct, String>>

    /** At launch: prices, purchases made while the app was closed (a pending one now paid, a refund). */
    fun refresh()

    /** Shows Play's purchase sheet over [activity] and returns once it's closed. */
    suspend fun buy(activity: Activity, product: AdFreeProduct): BuyResult

    /** "Restore purchases": asks Play what the account owns. False when Play couldn't be reached. */
    suspend fun restore(): Boolean
}
