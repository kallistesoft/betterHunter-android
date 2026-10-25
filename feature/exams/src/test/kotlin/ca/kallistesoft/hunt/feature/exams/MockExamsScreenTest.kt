package ca.kallistesoft.hunt.feature.exams

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import ca.kallistesoft.hunt.core.data.Now
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.model.MOCK_COUNT
import ca.kallistesoft.hunt.core.model.MockResult
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.testing.FakeContentRepository
import ca.kallistesoft.hunt.core.testing.FakeRewardedAds
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import ca.kallistesoft.hunt.core.testing.TEST_NOW
import ca.kallistesoft.hunt.core.testing.TestContent
import ca.kallistesoft.hunt.core.ui.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK], qualifiers = ScreenshotDevice.LIGHT_EN)
class MockExamsScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<String>()

    private fun show(state: MockExamsUiState) = compose.setContent {
        BetterHunterTheme {
            MockExamsContent(
                state,
                onBack = { events += "back" },
                onStart = { events += "start $it" },
                onUnlock = { events += "unlock $it" },
                onDismissOffline = { events += "dismiss" },
            )
        }
    }

    private fun scrollTo(tag: String) = compose.onNodeWithTag("mocks-list").performScrollToNode(hasTestTag(tag))

    @Test
    fun `a mock not taken starts, a finished one asks to watch an ad first`() {
        show(previewMocksState)
        compose.onNodeWithText(string(R.string.mocks_summary, 3, MOCK_COUNT)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.mock_scores, 88, 80)).assertIsDisplayed()

        scrollTo("mock-start-6")
        compose.onNodeWithTag("mock-start-6").performClick()

        scrollTo("mock-retake-2")
        compose.onNodeWithTag("mock-retake-2").performClick()
        compose.onNodeWithText(string(R.string.retake_title, 2)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.cancel)).performClick()
        compose.onNodeWithTag("mock-retake-2").performClick()
        compose.onNodeWithTag("watch-ad").performClick()

        compose.onNodeWithContentDescription(string(R.string.back)).performClick()
        assertEquals(listOf("start 6", "unlock ${MockUnlock.Retake(2)}", "back"), events)
    }

    @Test
    fun `all ten done offers Reset all behind an ad`() {
        show(previewMocksState.copy(mocks = (1..MOCK_COUNT).map { MockRow(it, MockResult(90, 90), passed = true) }))
        scrollTo("mocks-reset")
        compose.onNodeWithTag("mocks-reset").performClick()
        compose.onNodeWithText(string(R.string.reset_title)).assertIsDisplayed()
        compose.onNodeWithTag("watch-ad").performClick()
        assertEquals(listOf("unlock ${MockUnlock.ResetAll}"), events)
    }

    @Test
    fun `while the ad loads the buttons wait`() {
        show(previewMocksState.copy(watchingAd = true))
        compose.onNodeWithTag("ad-loading").assertIsDisplayed()
        compose.onNodeWithTag("mock-retake-1").assertIsNotEnabled()
    }

    @Test
    fun `offline says why nothing happened`() {
        show(previewMocksState.copy(offline = true))
        compose.onNodeWithText(string(R.string.ad_offline_body)).assertIsDisplayed()
        compose.onNodeWithTag("offline-ok").performClick()
        assertEquals(listOf("dismiss"), events)
    }

    @Test
    fun `an unlocked retake opens as a rewarded run`() {
        val userData = FakeUserDataRepository(UserProgress().withMockResult(TestContent.pal.id, 1, 70))
        val vm = MockExamsViewModel(TestContent.pal.id, FakeContentRepository(), userData, FakeRewardedAds(), Now { TEST_NOW })
        val started = mutableListOf<Pair<Int, Boolean>>()
        compose.setContent {
            BetterHunterTheme {
                MockExamsScreen(TestContent.pal.id, onBack = {}, onStartMock = { n, rewarded -> started += n to rewarded }, onOpenRemoveAds = {}, viewModel = vm)
            }
        }
        compose.onNodeWithTag("mock-retake-1").performClick()
        compose.onNodeWithTag("watch-ad").performClick()
        compose.onNodeWithTag("mock-start-2").performClick()
        compose.waitForIdle()
        assertEquals(listOf(1 to true, 2 to false), started)
    }

    @Test
    fun `the retake dialog offers to remove ads`() {
        val opened = mutableListOf<String>()
        compose.setContent {
            BetterHunterTheme {
                MockExamsContent(
                    previewMocksState,
                    onBack = {},
                    onStart = {},
                    onUnlock = { events += "unlock $it" },
                    onDismissOffline = {},
                    onOpenRemoveAds = { opened += "remove-ads" },
                )
            }
        }
        compose.onNodeWithTag("mock-retake-1").performClick()
        compose.onNodeWithTag("retake-remove-ads").performClick()
        assertEquals(listOf("remove-ads"), opened)
        assertEquals("nothing unlocked", emptyList<String>(), events)
    }

    @Test
    fun `without ads, a retake and Reset all go through at once`() {
        show(previewMocksState.copy(adFree = true, mocks = (1..MOCK_COUNT).map { MockRow(it, MockResult(90, 90), passed = true) }))
        compose.onNodeWithTag("mock-retake-1").performClick()
        compose.onNodeWithTag("watch-ad").assertDoesNotExist()
        compose.onNodeWithTag("mocks-reset").performClick()
        assertEquals(listOf("unlock ${MockUnlock.Retake(1)}", "unlock ${MockUnlock.ResetAll}"), events)
    }

    @Test
    fun `the list knows when ads are removed`() {
        val userData = FakeUserDataRepository(UserProgress().withAdFreeForever(true))
        val vm = MockExamsViewModel(TestContent.pal.id, FakeContentRepository(), userData, FakeRewardedAds(), Now { TEST_NOW })
        compose.setContent {
            BetterHunterTheme {
                MockExamsScreen(TestContent.pal.id, onBack = {}, onStartMock = { _, _ -> }, onOpenRemoveAds = {}, viewModel = vm)
            }
        }
        compose.waitUntil { vm.uiState.value.adFree }
        compose.onNodeWithTag("mock-start-1").assertIsDisplayed()
    }
}
