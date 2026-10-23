package ca.kallistesoft.hunt.feature.exams

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.model.PHOTO_ID_EXAM
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.testing.FakeContentRepository
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import ca.kallistesoft.hunt.core.testing.TEST_DAY
import ca.kallistesoft.hunt.core.ui.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK], qualifiers = ScreenshotDevice.LIGHT_EN)
class RecognitionScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `the photo game starts in practice, mock exam or review`() {
        val events = mutableListOf<String>()
        // One wrong photo answer: "Review mistakes" shows.
        val userData = FakeUserDataRepository(UserProgress().record("id-1", correct = false, TEST_DAY))
        compose.setContent {
            BetterHunterTheme {
                RecognitionScreen(
                    onBack = { events += "back" },
                    onStartQuiz = { id, mode -> events += "$id $mode" },
                    viewModel = ExamsViewModel(FakeContentRepository(), userData),
                )
            }
        }
        // The state is built on Dispatchers.Default, which Compose doesn't wait for.
        compose.waitUntil { compose.onAllNodesWithTag("practice-$PHOTO_ID_EXAM").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(string(R.string.hub_id_sub)).assertIsDisplayed()
        compose.onNodeWithTag("practice-$PHOTO_ID_EXAM").performClick()
        compose.onNodeWithTag("exam-$PHOTO_ID_EXAM").performClick()
        compose.onNodeWithTag("review-$PHOTO_ID_EXAM").performClick()
        compose.onNodeWithContentDescription(string(R.string.back)).performClick()
        assertEquals(
            listOf("$PHOTO_ID_EXAM ${QuizMode.PRACTICE}", "$PHOTO_ID_EXAM ${QuizMode.EXAM}", "$PHOTO_ID_EXAM ${QuizMode.REVIEW}", "back"),
            events,
        )
    }
}
