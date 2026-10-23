package ca.kallistesoft.hunt.feature.exams

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import ca.kallistesoft.hunt.core.ui.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK], qualifiers = ScreenshotDevice.LIGHT_EN)
class PassedExamScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val userData = FakeUserDataRepository()
    private var confirmed = 0
    private var back = 0

    private fun show() = compose.setContent {
        BetterHunterTheme {
            PassedExamScreen("ICAF", onConfirmed = { confirmed++ }, onBack = { back++ }, viewModel = PassedExamViewModel("ICAF", userData))
        }
    }

    @Test
    fun `"Yes, I passed" marks the exam passed and leaves`() {
        show()
        compose.onNodeWithText(string(R.string.passed_title, "ICAF")).assertIsDisplayed()
        compose.onNodeWithTag("passed-yes").performClick()
        compose.waitForIdle()
        assertEquals(setOf("ICAF"), userData.progress.value.passedExams)
        assertEquals(1, confirmed)
    }

    @Test
    fun `"Not yet" and back leave without saving`() {
        show()
        compose.onNodeWithTag("passed-not-yet").performClick()
        compose.onNodeWithContentDescription(string(R.string.back)).performClick()
        assertEquals(2, back)
        assertEquals(emptySet<String>(), userData.progress.value.passedExams)
        assertEquals(0, confirmed)
    }
}
