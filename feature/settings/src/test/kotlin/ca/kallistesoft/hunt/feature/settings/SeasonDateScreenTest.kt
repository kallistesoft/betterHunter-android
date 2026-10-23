package ca.kallistesoft.hunt.feature.settings

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import ca.kallistesoft.hunt.core.testing.TEST_DAY
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** The season date calendar (TEST_DAY is Friday, October 4, 2024), then "Remind me before my season?". */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK], qualifiers = "en-rCA-w411dp-h2400dp")
class SeasonDateScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val events = mutableListOf<String>()

    private fun setSeasonDate(state: SeasonDateUiState) = compose.setContent {
        BetterHunterTheme {
            SeasonDateContent(
                state,
                onBack = { events += "back" },
                onPick = { events += "pick:${it - TEST_DAY}" },
                onSave = { events += "save" },
                onDontKnow = { events += "dont-know" },
                onRemind = { events += "remind" },
                onNoThanks = { events += "no-thanks" },
            )
        }
    }

    @Test
    fun `days from today can be picked, month by month`() {
        setSeasonDate(SeasonDateUiState(loading = false, today = TEST_DAY))
        compose.onNodeWithText("October 2024").assertIsDisplayed()
        compose.onNodeWithTag("season-save").assertIsNotEnabled()
        compose.onNodeWithText("Pick your first hunting day").assertIsDisplayed()
        compose.onNodeWithContentDescription("Previous month").assertIsNotEnabled()
        compose.onNodeWithTag("day-${TEST_DAY - 1}").assertIsNotEnabled()

        compose.onNodeWithTag("day-${TEST_DAY - 1}").performClick()
        compose.onNodeWithTag("day-$TEST_DAY").performClick()
        compose.onNodeWithTag("day-${TEST_DAY + 20}").performClick()

        compose.onNodeWithContentDescription("Next month").performClick()
        compose.onNodeWithText("November 2024").assertIsDisplayed()
        compose.onNodeWithTag("day-${TEST_DAY + 40}").performClick()
        compose.onNodeWithContentDescription("Previous month").assertIsEnabled().performClick()
        compose.onNodeWithText("October 2024").assertIsDisplayed()

        compose.onNodeWithTag("season-dont-know").performClick()
        compose.onNodeWithContentDescription("Back").performClick()
        assertEquals(listOf("pick:0", "pick:20", "pick:40", "dont-know", "back"), events)
    }

    @Test
    fun `the calendar ends a year ahead`() {
        setSeasonDate(SeasonDateUiState(loading = false, today = TEST_DAY, picked = TEST_DAY + SeasonDateViewModel.MAX_DAYS_AHEAD))
        compose.onNodeWithText("October 2025").assertIsDisplayed()
        compose.onNodeWithContentDescription("Next month").assertIsNotEnabled()
        compose.onNodeWithTag("day-${TEST_DAY + SeasonDateViewModel.MAX_DAYS_AHEAD + 1}").assertIsNotEnabled()
    }

    @Test
    fun `a picked day is selected and can be saved`() {
        setSeasonDate(SeasonDateUiState(loading = false, today = TEST_DAY, picked = TEST_DAY + 20))
        compose.onNodeWithTag("day-${TEST_DAY + 20}").assertIsSelected()
        compose.onNodeWithTag("season-save").assertIsEnabled().performClick()
        assertEquals(listOf("save"), events)
    }

    @Test
    fun `once saved, it asks to remind before the season`() {
        setSeasonDate(SeasonDateUiState(loading = false, today = TEST_DAY, picked = TEST_DAY + 20, askReminders = true))
        compose.onNodeWithTag("season-save").assertDoesNotExist()
        compose.onNodeWithText("Saved:", substring = true, ignoreCase = true).assertIsDisplayed()
        compose.onNodeWithTag("season-remind-me").performClick()
        compose.onNodeWithTag("season-no-thanks").performClick()
        assertEquals(listOf("remind", "no-thanks"), events)
    }

    @Test
    fun `nothing shows while loading`() {
        setSeasonDate(SeasonDateUiState())
        compose.onNodeWithTag("calendar").assertDoesNotExist()
        compose.onNodeWithTag("season-save").assertDoesNotExist()
    }

    private val userData = FakeUserDataRepository(UserProgress())

    private fun setScreen(): SeasonDateViewModel {
        val viewModel = SeasonDateViewModel(userData, Today { TEST_DAY }, SavedStateHandle())
        compose.setContent {
            BetterHunterTheme { SeasonDateScreen(onDone = { events += "done" }, viewModel = viewModel) }
        }
        return viewModel
    }

    @Test
    fun `picking and saving a date, then Remind me, turns season reminders on and closes`() {
        shadowOf(compose.activity.application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        setScreen()
        compose.onNodeWithTag("day-${TEST_DAY + 20}").performClick()
        compose.onNodeWithTag("season-save").performClick()
        compose.onNodeWithTag("season-remind-me").performClick()
        compose.waitForIdle()

        assertEquals(TEST_DAY + 20, userData.progress.value.seasonStart)
        assertTrue(userData.progress.value.seasonReminders)
        assertEquals(listOf("done"), events)
    }

    @Test
    fun `No thanks leaves season reminders off and closes`() {
        setScreen()
        compose.onNodeWithTag("day-${TEST_DAY + 3}").performClick()
        compose.onNodeWithTag("season-save").performClick()
        compose.onNodeWithTag("season-no-thanks").performClick()
        compose.waitForIdle()

        assertEquals(TEST_DAY + 3, userData.progress.value.seasonStart)
        assertFalse(userData.progress.value.seasonReminders)
        assertEquals(listOf("done"), events)
    }

    @Test
    fun `I don't know yet clears the saved date and closes`() {
        runBlocking { userData.setSeasonStart(TEST_DAY + 10) }
        setScreen()
        compose.onNodeWithTag("day-${TEST_DAY + 10}").assertIsSelected()
        compose.onNodeWithTag("season-dont-know").performClick()
        compose.waitForIdle()

        assertNull(userData.progress.value.seasonStart)
        assertEquals(listOf("done"), events)
    }
}
