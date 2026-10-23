package ca.kallistesoft.hunt.core.ads

import android.app.Activity
import ca.kallistesoft.hunt.core.flags.FeatureFlag
import ca.kallistesoft.hunt.core.testing.FakeFeatureFlags
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The ad rules on fake SDKs: consent gating, the `ads_enabled` flag, preloading, the 3-minute cap and fallbacks. */
@OptIn(ExperimentalCoroutinesApi::class)
class AdsManagerTest {
    private val activity = mockk<Activity>()
    private val flags = FakeFeatureFlags()
    private val consent = FakeConsent()
    private val sdk = FakeSdk()
    private var now = 0L
    private var onScreen: Activity? = activity

    private fun TestScope.manager() = AdsManager(
        consent, sdk, flags, { onScreen }, AdsConfig("unit-1"), backgroundScope, { now },
    )

    /** Consent gathered at launch, ads allowed: the SDK starts and an ad is preloaded. */
    private fun TestScope.started(): AdsManager = manager().also {
        it.gatherConsent(activity)
        runCurrent()
    }

    @Test
    fun `nothing starts before consent allows ads`() = runTest {
        consent.canRequest = false
        val ads = manager()
        ads.gatherConsent(activity)
        runCurrent()
        assertEquals(1, consent.updates)
        assertEquals(0, sdk.initialized)
        assertEquals(0, sdk.loads)

        ads.showBeforeResults()
        assertEquals("no ad without consent", 0, sdk.shown)
        assertEquals(0, sdk.initialized)
    }

    @Test
    fun `consent given in the form starts the SDK and preloads the interstitial`() = runTest {
        consent.canRequest = false
        consent.onUpdate = { consent.canRequest = true }
        manager().gatherConsent(activity)
        runCurrent()
        assertEquals(1, sdk.initialized)
        assertEquals(listOf("unit-1"), sdk.unitIds)
    }

    @Test
    fun `consent from an earlier launch starts ads once, while the information refreshes`() = runTest {
        val update = CompletableDeferred<Unit>()
        consent.waitOnUpdate = update
        manager().gatherConsent(activity)
        runCurrent()
        assertEquals("started before the refresh ends", 1, sdk.initialized)
        update.complete(Unit)
        runCurrent()
        assertEquals("initialised once, one ad loaded", 1 to 1, sdk.initialized to sdk.loads)
    }

    @Test
    fun `the preloaded interstitial shows after a test, then the next one loads`() = runTest {
        val ads = started()
        ads.showBeforeResults()
        assertEquals(listOf(activity), sdk.shownOn)
        runCurrent()
        assertEquals(2, sdk.loads)
        assertEquals(1, sdk.initialized)
    }

    @Test
    fun `the results wait while the ad is on screen`() = runTest {
        val ads = started()
        val closed = CompletableDeferred<Unit>()
        sdk.closeAd = closed
        var resultsShown = false
        launch {
            ads.showBeforeResults()
            resultsShown = true
        }
        runCurrent()
        assertFalse(resultsShown)
        closed.complete(Unit)
        runCurrent()
        assertTrue(resultsShown)
    }

    @Test
    fun `at most one interstitial every 3 minutes`() = runTest {
        val ads = started()
        ads.showBeforeResults()
        runCurrent()
        now += 2 * 60_000
        ads.showBeforeResults()
        assertEquals("capped", 1, sdk.shown)
        now += 60_000
        ads.showBeforeResults()
        assertEquals(2, sdk.shown)
    }

    @Test
    fun `no ad loaded means the results come at once, and one starts loading`() = runTest {
        sdk.fill = false
        val ads = started()
        assertEquals(1, sdk.loads)
        ads.showBeforeResults()
        assertEquals(0, sdk.shown)
        runCurrent()
        assertEquals("loads again for the next test", 2, sdk.loads)

        sdk.fill = true
        ads.showBeforeResults()
        runCurrent()
        assertEquals(3, sdk.loads)
        ads.showBeforeResults()
        assertEquals(1, sdk.shown)
    }

    @Test
    fun `an ad still loading isn't waited for, nor loaded twice`() = runTest {
        val loaded = CompletableDeferred<Unit>()
        sdk.waitOnLoad = loaded
        val ads = started()
        ads.showBeforeResults()
        runCurrent()
        assertEquals(0, sdk.shown)
        assertEquals(1, sdk.loads)
        loaded.complete(Unit)
        runCurrent()
        ads.showBeforeResults()
        assertEquals(1, sdk.shown)
    }

    @Test
    fun `an ad loaded over an hour ago is dropped and loaded again`() = runTest {
        val ads = started()
        now += 60 * 60_000
        ads.showBeforeResults()
        assertEquals(0, sdk.shown)
        runCurrent()
        assertEquals(2, sdk.loads)
        ads.showBeforeResults()
        assertEquals(1, sdk.shown)
    }

    @Test
    fun `with ads_enabled off nothing loads or shows`() = runTest {
        flags.set(FeatureFlag.ADS_ENABLED, false)
        val ads = started()
        assertEquals(0, sdk.initialized)
        ads.showBeforeResults()
        assertEquals(0, sdk.shown)

        flags.set(FeatureFlag.ADS_ENABLED, true) // switched back on remotely: the next test loads one
        ads.showBeforeResults()
        runCurrent()
        assertEquals(1, sdk.loads)
    }

    @Test
    fun `ads_enabled switched off stops a preloaded ad from showing`() = runTest {
        val ads = started()
        flags.set(FeatureFlag.ADS_ENABLED, false)
        ads.showBeforeResults()
        assertEquals(0, sdk.shown)
    }

    @Test
    fun `no activity on screen, no ad, and the loaded one is kept`() = runTest {
        val ads = started()
        onScreen = null
        ads.showBeforeResults()
        runCurrent()
        assertEquals(0, sdk.shown)
        assertEquals("not loaded again", 1, sdk.loads)
        onScreen = activity
        ads.showBeforeResults()
        assertEquals(1, sdk.shown)
    }

    @Test
    fun `privacy options follow the consent, and the form shows on the current screen`() = runTest {
        consent.privacyRequired = true
        val ads = started()
        assertTrue(ads.privacyOptionsRequired.value)

        consent.onPrivacyOptions = { consent.privacyRequired = false }
        ads.showPrivacyOptions()
        runCurrent()
        assertEquals(listOf(activity), consent.privacyShownOn)
        assertFalse(ads.privacyOptionsRequired.value)
    }

    @Test
    fun `consent given from the privacy options starts ads`() = runTest {
        consent.canRequest = false
        val ads = started()
        consent.onPrivacyOptions = { consent.canRequest = true }
        ads.showPrivacyOptions()
        runCurrent()
        assertEquals(1, sdk.initialized)
    }

    @Test
    fun `privacy options need a screen to show on`() = runTest {
        val ads = started()
        onScreen = null
        ads.showPrivacyOptions()
        runCurrent()
        assertTrue(consent.privacyShownOn.isEmpty())
    }

    private class FakeConsent : ConsentGateway {
        var canRequest = true
        var privacyRequired = false
        var updates = 0
        var onUpdate: () -> Unit = {}
        var waitOnUpdate: CompletableDeferred<Unit>? = null
        var onPrivacyOptions: () -> Unit = {}
        val privacyShownOn = mutableListOf<Activity>()

        override suspend fun update(activity: Activity) {
            updates++
            waitOnUpdate?.await()
            onUpdate()
        }

        override fun canRequestAds() = canRequest

        override fun privacyOptionsRequired() = privacyRequired

        override suspend fun showPrivacyOptions(activity: Activity) {
            privacyShownOn += activity
            onPrivacyOptions()
        }
    }

    private class FakeSdk : AdsSdk {
        var initialized = 0
        var loads = 0
        val unitIds = mutableListOf<String>()
        var fill = true
        var waitOnLoad: CompletableDeferred<Unit>? = null
        var closeAd: CompletableDeferred<Unit>? = null
        val shownOn = mutableListOf<Activity>()
        val shown get() = shownOn.size

        override suspend fun initialize() {
            initialized++
        }

        override suspend fun loadInterstitial(unitId: String): Interstitial? {
            loads++
            unitIds += unitId
            waitOnLoad?.await()
            return if (fill) {
                Interstitial { activity ->
                    shownOn += activity
                    closeAd?.await()
                }
            } else {
                null
            }
        }
    }
}
