package ca.kallistesoft.hunt.core.ads

import android.app.Activity
import ca.kallistesoft.hunt.core.flags.FeatureFlag
import ca.kallistesoft.hunt.core.flags.FeatureFlags
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * The ad rules, on top of the SDKs ([consent], [sdk]):
 * - nothing is initialised, loaded or shown until consent allows ads to be requested, nor while `ads_enabled` is off;
 * - the interstitial is preloaded, so the results never wait for a network request: not loaded means no ad;
 * - at most one interstitial every [CAP_MILLIS] (AdMob's frequency cap is set to the same);
 * - a loaded ad older than [AD_LIFETIME_MILLIS] is dropped (AdMob doesn't serve stale ones) and loaded again.
 *
 * Runs on the main thread ([scope] and callers), as the SDKs require; [clock] is the elapsed real time in ms.
 */
internal class AdsManager(
    private val consent: ConsentGateway,
    private val sdk: AdsSdk,
    private val flags: FeatureFlags,
    private val currentActivity: () -> Activity?,
    private val config: AdsConfig,
    private val scope: CoroutineScope,
    private val clock: () -> Long,
) : InterstitialAds, AdConsent {
    private val privacyRequired = MutableStateFlow(false)
    override val privacyOptionsRequired: StateFlow<Boolean> = privacyRequired.asStateFlow()

    private var initialized = false
    private var loading: Job? = null
    private var ad: Interstitial? = null
    private var loadedAt = 0L
    private var lastShown: Long? = null

    override fun gatherConsent(activity: Activity) {
        if (!config.enabled) return
        scope.launch {
            // Consent given on an earlier launch: ads can start while the information is refreshed.
            startIfAllowed()
            consent.update(activity)
            privacyRequired.value = consent.privacyOptionsRequired()
            startIfAllowed()
        }
    }

    override fun showPrivacyOptions() {
        val activity = currentActivity() ?: return
        scope.launch {
            consent.showPrivacyOptions(activity)
            privacyRequired.value = consent.privacyOptionsRequired()
            startIfAllowed()
        }
    }

    override suspend fun showBeforeResults() {
        if (!allowed()) return
        val last = lastShown
        if (last != null && clock() - last < CAP_MILLIS) return
        val activity = currentActivity() ?: return
        val ready = ad?.takeIf { clock() - loadedAt < AD_LIFETIME_MILLIS }
        ad = null
        if (ready != null) {
            lastShown = clock()
            ready.show(activity)
        }
        preload()
    }

    private suspend fun allowed(): Boolean =
        config.enabled && consent.canRequestAds() && flags.isEnabled(FeatureFlag.ADS_ENABLED).first()

    private suspend fun startIfAllowed() {
        if (allowed()) preload()
    }

    /** Loads the next interstitial, unless one is ready or loading. A failed load leaves none: the next test retries. */
    private fun preload() {
        if (ad != null || loading?.isActive == true) return
        loading = scope.launch {
            if (!initialized) {
                sdk.initialize()
                initialized = true
            }
            ad = sdk.loadInterstitial(config.interstitialUnitId)
            loadedAt = clock()
        }
    }
}

/** At most one interstitial every 3 minutes. */
internal const val CAP_MILLIS = 3 * 60 * 1_000L

/** A loaded interstitial is shown within the hour, or dropped. */
internal const val AD_LIFETIME_MILLIS = 60 * 60 * 1_000L
