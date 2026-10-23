package ca.kallistesoft.hunt.feature.quiz

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import ca.kallistesoft.hunt.core.data.ContentRepository
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.model.Content
import ca.kallistesoft.hunt.core.model.PHOTO_ID_EXAM
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.core.testing.FakeContentRepository
import ca.kallistesoft.hunt.core.testing.FakeFeatureFlags
import ca.kallistesoft.hunt.core.testing.FakeInterstitialAds
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import ca.kallistesoft.hunt.core.testing.TEST_DAY
import ca.kallistesoft.hunt.core.testing.TestContent
import ca.kallistesoft.hunt.core.ui.CONTACT_EMAIL
import ca.kallistesoft.hunt.core.ui.R
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** The quiz screen driven by a real [QuizViewModel] on the test content (three questions per exam, answer B). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK], qualifiers = ScreenshotDevice.LIGHT_EN)
class QuizScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val userData = FakeUserDataRepository()
    private var closed = 0
    private var practice = 0

    private fun show(
        examId: String = TestContent.pal.id,
        mode: QuizMode = QuizMode.PRACTICE,
        content: ContentRepository = FakeContentRepository(),
    ): QuizViewModel {
        val vm = QuizViewModel(examId, mode, content, userData, SavedStateHandle(), FakeFeatureFlags(), Today { TEST_DAY }, FakeInterstitialAds())
        compose.setContent {
            BetterHunterTheme {
                QuizScreen(examId, mode, onClose = { closed++ }, onStartPractice = { practice++ }, viewModel = vm)
            }
        }
        return vm
    }

    private val QuizViewModel.question get() = (uiState.value as QuizUiState.Running).session.question

    private fun answer(choice: Int) {
        compose.onNodeWithTag("choice-$choice").performClick()
        compose.onNodeWithTag("next").performClick()
    }

    @Test
    fun `practice shows right or wrong after each answer, then the score, and Try again starts over`() {
        show()
        compose.onNodeWithText(string(R.string.question_progress, 1, 3)).assertIsDisplayed()

        compose.onNodeWithTag("choice-0").performClick()
        compose.onNodeWithTag("feedback").assertIsDisplayed()
        compose.onAllNodesWithText(string(R.string.wrong)).onFirst().assertIsDisplayed()
        compose.onNodeWithText(string(R.string.why), ignoreCase = true).assertIsDisplayed()
        compose.onNodeWithTag("next").performClick()

        compose.onNodeWithTag("choice-1").performClick()
        compose.onAllNodesWithText(string(R.string.correct)).onFirst().assertIsDisplayed()
        compose.onNodeWithTag("next").performClick()

        compose.onNodeWithText(string(R.string.next)).assertDoesNotExist()
        compose.onNodeWithText(string(R.string.finish)).assertIsDisplayed()
        answer(1)

        compose.onNodeWithTag("score").assertIsDisplayed()
        compose.onNodeWithText(string(R.string.percent, 67)).assertIsDisplayed()
        assertEquals(listOf(false, true, true), userData.recorded.map { it.second })

        compose.onNodeWithTag("again").performClick()
        compose.onNodeWithText(string(R.string.question_progress, 1, 3)).assertIsDisplayed()
        compose.onNodeWithTag("feedback").assertDoesNotExist()
    }

    @Test
    fun `a photo question shows the answer's card, with the user's wrong pick`() {
        val vm = show(examId = PHOTO_ID_EXAM)
        compose.onNodeWithTag("choice-3").assertIsDisplayed()
        compose.onNodeWithTag("choice-0").performClick()
        compose.onNodeWithText(string(R.string.your_answer, "A")).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.how_recognise), ignoreCase = true).assertIsDisplayed()
        compose.onNodeWithTag("choice-0").assertDoesNotExist() // the choices make way for the animal's card
        assertEquals(PHOTO_ID_EXAM, (vm.uiState.value as QuizUiState.Running).session.exam.id)
    }

    @Test
    fun `quitting asks first`() {
        show()
        compose.onNodeWithContentDescription(string(R.string.back)).performClick()
        compose.onNodeWithText(string(R.string.quit_title)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.cancel)).performClick()
        compose.onNodeWithText(string(R.string.quit_title)).assertDoesNotExist()
        assertEquals(0, closed)

        compose.onNodeWithContentDescription(string(R.string.back)).performClick()
        compose.onNodeWithText(string(R.string.quit)).performClick()
        assertEquals(1, closed)
    }

    @Test
    fun `reporting a question opens an email about it`() {
        val vm = show()
        compose.onNodeWithTag("report").performClick()
        val intent = shadowOf(RuntimeEnvironment.getApplication()).nextStartedActivity
        assertEquals(Intent.ACTION_SENDTO, intent.action)
        assertEquals("mailto", intent.data?.scheme)
        assertTrue(intent.data.toString().contains(vm.question.id))
    }

    @Test
    fun `with no email app, reporting shows the address instead`() {
        shadowOf(RuntimeEnvironment.getApplication()).checkActivities(true)
        val vm = show()
        compose.onNodeWithTag("report").performClick()
        compose.onNodeWithText(string(R.string.report_fallback, CONTACT_EMAIL, vm.question.id)).assertIsDisplayed()
    }

    @Test
    fun `while the questions load, the quiz can be closed`() {
        show(content = object : ContentRepository {
            override val content = MutableSharedFlow<Content>() // never loads
        })
        compose.onNodeWithTag("choice-0").assertDoesNotExist()
        compose.onNodeWithContentDescription(string(R.string.back)).performClick()
        assertEquals(1, closed)
    }

    @Test
    fun `an exam without questions says so`() {
        show(examId = "NONE")
        compose.onNodeWithText(string(R.string.no_questions)).assertIsDisplayed()
        compose.onNodeWithTag("empty-practise").assertDoesNotExist()
    }

    @Test
    fun `no mistakes to review offers a practice run`() {
        show(mode = QuizMode.REVIEW)
        compose.onNodeWithText(string(R.string.nothing_to_review)).assertIsDisplayed()
        compose.onNodeWithTag("empty-practise").performClick()
        assertEquals(1, practice)
    }
}
