package ca.kallistesoft.hunt.core.testing

import android.app.Activity
import ca.kallistesoft.hunt.core.ads.AdConsent
import ca.kallistesoft.hunt.core.ads.InterstitialAds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Counts the interstitials asked for. With [onScreen] set, an ad stays "on screen" (the call doesn't return) until
 * it's completed, so a test can check what shows meanwhile.
 */
class FakeInterstitialAds : InterstitialAds {
    var shown = 0
        private set
    var onScreen: CompletableDeferred<Unit>? = null

    override suspend fun showBeforeResults() {
        shown++
        onScreen?.await()
    }
}

/** Consent that a test sets: whether Settings offers the privacy options, and how often they were opened. */
class FakeAdConsent(privacyOptionsRequired: Boolean = false) : AdConsent {
    override val privacyOptionsRequired = MutableStateFlow(privacyOptionsRequired)
    var privacyOptionsShown = 0
        private set

    override fun gatherConsent(activity: Activity) = Unit

    override fun showPrivacyOptions() {
        privacyOptionsShown++
    }
}
