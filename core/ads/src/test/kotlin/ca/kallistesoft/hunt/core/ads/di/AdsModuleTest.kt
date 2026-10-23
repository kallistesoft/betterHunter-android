package ca.kallistesoft.hunt.core.ads.di

import android.content.Context
import ca.kallistesoft.hunt.core.ads.AdsConfig
import ca.kallistesoft.hunt.core.testing.FakeFeatureFlags
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK])
class AdsModuleTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @Test
    fun `one manager serves the quiz and the consent, and nothing is asked before launch`() {
        val manager = AdsModule.adsManager(context, FakeFeatureFlags(), AdsConfig("unit-1"))
        assertSame(manager, AdsModule.interstitialAds(manager))
        assertSame(manager, AdsModule.adConsent(manager))
        assertFalse(manager.privacyOptionsRequired.value)
    }
}
