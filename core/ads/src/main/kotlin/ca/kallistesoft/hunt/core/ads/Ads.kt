package ca.kallistesoft.hunt.core.ads

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

/**
 * The one ad placement (P8): an interstitial between a finished test and its results, never during a question.
 * Screens and ViewModels use this interface; tests use a fake (`FakeInterstitialAds` in `:core:testing`).
 */
fun interface InterstitialAds {
    /**
     * Shows the preloaded interstitial and returns once it's closed. Returns at once, showing nothing, when ads can't
     * be shown: no consent, `ads_enabled` off, one shown less than 3 minutes ago, or none loaded (the next one then
     * starts loading). The caller shows the results after it returns.
     */
    suspend fun showBeforeResults()
}

/** What came of asking for a rewarded ad (P14). */
enum class RewardOutcome {
    /** The ad was watched to the end: give the reward. */
    EARNED,

    /** The ad was closed before the end: no reward. */
    NOT_EARNED,

    /** No network, so no ad: no reward either (letting it through would teach users to go offline). */
    OFFLINE,

    /** No ad to watch through no fault of the user (ads turned off by us, or none available online): give the reward. */
    NO_ADS,
}

/** A rewarded ad the user chooses to watch for something: a mock exam retake, or resetting all of them (P14). */
interface RewardedAds {
    /** Starts loading one ahead of time (the mock exams screen opens), so it shows without waiting. */
    fun preload()

    /** Shows a rewarded ad and returns once it's closed, waiting a few seconds for one still loading. */
    suspend fun showForReward(): RewardOutcome
}

/** The consent to ads (Google's User Messaging Platform), asked at launch and changed from Settings. */
interface AdConsent {
    /** The user's region requires a way back to their consent choices: Settings shows "Privacy options" while true. */
    val privacyOptionsRequired: StateFlow<Boolean>

    /**
     * At launch: refreshes the consent information and shows the consent form if it's required. Ads start (Mobile
     * Ads initialised, the first interstitial loading) only once ads may be requested.
     */
    fun gatherConsent(activity: Activity)

    /** Settings → Privacy options: shows the consent choices again, over the app's current screen. */
    fun showPrivacyOptions()
}

/**
 * The ad units of the interstitial and the rewarded ad; the build type chooses them (Google's test units in debug, see
 * app/build.gradle.kts). [enabled] false turns ads off entirely, consent request included (the app's instrumented
 * tests).
 */
data class AdsConfig(val interstitialUnitId: String, val enabled: Boolean = true, val rewardedUnitId: String = "")
