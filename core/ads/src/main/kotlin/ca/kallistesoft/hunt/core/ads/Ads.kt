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
 * The ad unit of the interstitial; the build type chooses it (Google's test unit in debug, see app/build.gradle.kts).
 * [enabled] false turns ads off entirely, consent request included (the app's instrumented tests).
 */
data class AdsConfig(val interstitialUnitId: String, val enabled: Boolean = true)
