package ca.kallistesoft.hunt.feature.exams

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
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
class ExamTrainingScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<String>()

    @Test
    fun `each exam card starts its quiz, and both provinces links open My provinces`() {
        // A Québec hunter with one ICAF mistake: practice, mock exam, review and "I passed" are all offered.
        val userData = FakeUserDataRepository(
            UserProgress().withProfile(HunterType.WORKING_ON_IT, emptySet(), Province.QC).record("qc-1", correct = false, TEST_DAY),
        )
        compose.setContent {
            BetterHunterTheme {
                ExamTrainingScreen(
                    onBack = { events += "back" },
                    onOpenProvinces = { events += "provinces" },
                    onStartQuiz = { id, mode -> events += "$id $mode" },
                    onPassedRealExam = { events += "passed $it" },
                    viewModel = ExamsViewModel(FakeContentRepository(), userData),
                )
            }
        }
        compose.onNodeWithTag("exam-card-ICAF").assertIsDisplayed()
        compose.onNodeWithTag("practice-ICAF").performClick()
        compose.onNodeWithTag("exam-ICAF").performClick()
        compose.onNodeWithTag("review-ICAF").performClick()
        compose.onNodeWithTag("passed-ICAF").performClick()
        compose.onNodeWithTag("change-exams").performClick()
        compose.onNodeWithTag("other-provinces").performClick()
        compose.onNodeWithContentDescription(string(R.string.back)).performClick()
        assertEquals(
            listOf(
                "ICAF ${QuizMode.PRACTICE}",
                "ICAF ${QuizMode.EXAM}",
                "ICAF ${QuizMode.REVIEW}",
                "passed ICAF",
                "provinces",
                "provinces",
                "back",
            ),
            events,
        )
    }

    @Test
    fun `with every province added, Other provinces says so`() {
        compose.setContent {
            BetterHunterTheme {
                ExamTrainingContent(
                    previewExamsState.copy(otherProvinces = emptyList()),
                    onBack = {},
                    onOpenProvinces = {},
                    onStartQuiz = { _, _ -> },
                    onPassedRealExam = {},
                )
            }
        }
        compose.onNodeWithText(string(R.string.all_provinces_added)).assertIsDisplayed()
    }
}
