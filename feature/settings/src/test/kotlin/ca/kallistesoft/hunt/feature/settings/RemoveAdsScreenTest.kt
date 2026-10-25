package ca.kallistesoft.hunt.feature.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ca.kallistesoft.hunt.core.billing.AdFreeProduct
import ca.kallistesoft.hunt.core.data.Now
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.testing.FakePurchases
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import ca.kallistesoft.hunt.core.testing.TEST_NOW
import ca.kallistesoft.hunt.core.ui.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK], qualifiers = ScreenshotDevice.LIGHT_EN)
class RemoveAdsScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val events = mutableListOf<String>()

    private fun show(state: RemoveAdsUiState) = compose.setContent {
        BetterHunterTheme {
            RemoveAdsContent(
                state,
                onBack = { events += "back" },
                onBuy = { events += "buy $it" },
                onRestore = { events += "restore" },
                onDismissMessage = { events += "dismiss" },
            )
        }
    }

    private val prices = mapOf(AdFreeProduct.WEEK to "CA$1.99", AdFreeProduct.FOREVER to "CA$14.99")

    @Test
    fun `the week says it's lost with the app, before Play's sheet too`() {
        show(RemoveAdsUiState(prices = prices, loading = false))
        compose.onNodeWithText(string(R.string.remove_ads_week_lost)).assertIsDisplayed()
        compose.onNodeWithTag("remove-ads-week-buy").performClick()
        compose.onNodeWithText(string(R.string.remove_ads_week_confirm_title)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.cancel)).performClick()
        assertTrue("cancel buys nothing", events.isEmpty())

        compose.onNodeWithTag("remove-ads-week-buy").performClick()
        compose.onNodeWithTag("remove-ads-week-continue").performClick()
        compose.onNodeWithTag("remove-ads-forever-buy").performClick()
        compose.onNodeWithTag("remove-ads-restore").performClick()
        assertEquals(listOf("buy ${AdFreeProduct.WEEK}", "buy ${AdFreeProduct.FOREVER}", "restore"), events)
    }

    @Test
    fun `no price from Play, nothing to buy`() {
        show(RemoveAdsUiState(loading = false))
        compose.onNodeWithTag("remove-ads-forever-buy").assertIsNotEnabled()
    }

    @Test
    fun `a running week, a message, and forever owned`() {
        show(RemoveAdsUiState(prices = prices, untilDay = 20_758L, message = RemoveAdsMessage.FAILED, busy = true, loading = false))
        compose.onNodeWithTag("remove-ads-week-running").assertIsDisplayed()
        compose.onNodeWithTag("remove-ads-busy").assertIsDisplayed()
        compose.onNodeWithText(string(R.string.remove_ads_failed)).assertIsDisplayed()
        compose.onNodeWithTag("remove-ads-message-ok").performClick()
        assertEquals(listOf("dismiss"), events)
    }

    @Test
    fun `forever owned says thanks and sells nothing`() {
        show(RemoveAdsUiState(forever = true, message = RemoveAdsMessage.RESTORED, loading = false))
        compose.onNodeWithTag("remove-ads-forever-owned").assertIsDisplayed()
        compose.onNodeWithText(string(R.string.remove_ads_restored)).assertIsDisplayed()
        assertTrue(compose.onAllNodes(hasTestTag("remove-ads-week")).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `the screen buys over its activity`() {
        val userData = FakeUserDataRepository()
        val purchases = FakePurchases(userData)
        compose.setContent {
            BetterHunterTheme { RemoveAdsScreen(onBack = {}, viewModel = RemoveAdsViewModel(userData, purchases, Now { TEST_NOW })) }
        }
        compose.onNodeWithTag("remove-ads-forever-buy").performClick()
        compose.waitUntil { userData.progress.value.adFreeForever }
        compose.onNodeWithTag("remove-ads-forever-owned").assertIsDisplayed()
        compose.onNodeWithText(string(R.string.remove_ads_pending)).assertDoesNotExist()
    }
}
