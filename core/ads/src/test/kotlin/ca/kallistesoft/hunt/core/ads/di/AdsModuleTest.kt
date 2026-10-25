package ca.kallistesoft.hunt.core.ads.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import ca.kallistesoft.hunt.core.ads.AdsConfig
import ca.kallistesoft.hunt.core.testing.FakeFeatureFlags
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNetworkCapabilities

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK])
class AdsModuleTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @Test
    fun `one manager serves the quiz and the consent, and nothing is asked before launch`() {
        val manager = AdsModule.adsManager(context, FakeFeatureFlags(), AdsConfig("unit-1"))
        assertSame(manager, AdsModule.interstitialAds(manager))
        assertSame(manager, AdsModule.adConsent(manager))
        assertSame(manager, AdsModule.rewardedAds(manager))
        assertFalse(manager.privacyOptionsRequired.value)
    }

    @Test
    fun `online means a validated network`() {
        val connectivity = context.getSystemService(ConnectivityManager::class.java)
        val network = connectivity.activeNetwork
        val validated = ShadowNetworkCapabilities.newInstance().also {
            shadowOf(it).addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
        shadowOf(connectivity).setNetworkCapabilities(network, validated)
        assertTrue(AdsModule.online(context))

        shadowOf(connectivity).setNetworkCapabilities(network, ShadowNetworkCapabilities.newInstance())
        assertFalse("connected but no internet", AdsModule.online(context))

        shadowOf(connectivity).setDefaultNetworkActive(false)
        shadowOf(connectivity).setActiveNetworkInfo(null)
        shadowOf(connectivity).setNetworkCapabilities(network, null)
        assertFalse("no network", AdsModule.online(context))
    }
}
