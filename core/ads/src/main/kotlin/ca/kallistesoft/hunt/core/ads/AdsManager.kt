package ca.kallistesoft.hunt.core.ads

import android.app.Activity
import ca.kallistesoft.hunt.core.flags.FeatureFlag
import ca.kallistesoft.hunt.core.flags.FeatureFlags
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The ad rules, on top of the SDKs ([consent], [sdk]):
 * - nothing is initialised, loaded or shown until consent allows ads to be requested, nor while `ads_enabled` is off;
 * - the interstitial is preloaded, so the results never wait for a network request: not loaded means no ad;
 * - at most one interstitial every [CAP_MILLIS] (AdMob's frequency cap is set to the same);
 * - a loaded ad older than [AD_LIFETIME_MILLIS] is dropped (AdMob doesn't serve stale ones) and loaded again;
 * - a rewarded ad is never skipped by going offline: no network means no reward. It's given without an ad only when
 *   the user can't cause it: ads turned off by us (config, `ads_enabled`), or no ad available while online (P14).
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
    private val isOnline: () -> Boolean,
) : InterstitialAds, RewardedAds, AdConsent {
    private val privacyRequired = MutableStateFlow(false)
    override val privacyOptionsRequired: StateFlow<Boolean> = privacyRequired.asStateFlow()

    private var initialized = false
    private var loading: Job? = null
    private var ad: Interstitial? = null
    private var loadedAt = 0L
    private var lastShown: Long? = null
    private var rewardedLoading: Deferred<Rewarded?>? = null
    private var rewarded: Rewarded? = null
    private var rewardedLoadedAt = 0L

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
        preloadInterstitial()
    }

    override fun preload() {
        scope.launch { if (allowed() && config.rewardedUnitId.isNotBlank()) loadRewarded() }
    }

    override suspend fun showForReward(): RewardOutcome {
        if (!adsOn() || config.rewardedUnitId.isBlank()) return RewardOutcome.NO_ADS
        if (!isOnline()) return RewardOutcome.OFFLINE
        val activity = currentActivity() ?: return RewardOutcome.NOT_EARNED
        // Consent not settled yet (the launch request failed offline): ask now rather than skip the ad.
        if (!consent.canRequestAds()) consent.update(activity)
        if (!consent.canRequestAds()) return RewardOutcome.NO_ADS
        val ad = withTimeoutOrNull(REWARDED_WAIT_MILLIS) { loadRewarded().await() }
        rewarded = null
        return when {
            ad != null -> if (ad.show(activity)) RewardOutcome.EARNED else RewardOutcome.NOT_EARNED
            isOnline() -> RewardOutcome.NO_ADS
            else -> RewardOutcome.OFFLINE
        }.also { preload() }
    }

    /** The load in progress, the loaded rewarded ad (under [AD_LIFETIME_MILLIS] old), or a new load. */
    private fun loadRewarded(): Deferred<Rewarded?> {
        rewardedLoading?.takeIf { it.isActive }?.let { return it }
        rewarded?.takeIf { clock() - rewardedLoadedAt < AD_LIFETIME_MILLIS }?.let { return CompletableDeferred(it) }
        return scope.async {
            initialize()
            sdk.loadRewarded(config.rewardedUnitId).also {
                rewarded = it
                rewardedLoadedAt = clock()
            }
        }.also { rewardedLoading = it }
    }

    private suspend fun initialize() {
        if (!initialized) {
            initialized = true
            sdk.initialize()
        }
    }

    private suspend fun adsOn(): Boolean = config.enabled && flags.isEnabled(FeatureFlag.ADS_ENABLED).first()

    private suspend fun allowed(): Boolean = adsOn() && consent.canRequestAds()

    private suspend fun startIfAllowed() {
        if (allowed()) preloadInterstitial()
    }

    /** Loads the next interstitial, unless one is ready or loading. A failed load leaves none: the next test retries. */
    private fun preloadInterstitial() {
        if (ad != null || loading?.isActive == true) return
        loading = scope.launch {
            initialize()
            ad = sdk.loadInterstitial(config.interstitialUnitId)
            loadedAt = clock()
        }
    }
}

/** At most one interstitial every 3 minutes. */
internal const val CAP_MILLIS = 3 * 60 * 1_000L

/** A loaded interstitial is shown within the hour, or dropped. */
internal const val AD_LIFETIME_MILLIS = 60 * 60 * 1_000L

/** How long "Watch ad" waits for a rewarded ad still loading. */
internal const val REWARDED_WAIT_MILLIS = 8_000L
