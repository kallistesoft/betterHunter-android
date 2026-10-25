package ca.kallistesoft.hunt

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ca.kallistesoft.hunt.core.model.QuizSession
import ca.kallistesoft.hunt.core.ui.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Working on it: practice to the results, progress on Home, review of mistakes, leaving a mock exam. */
@RunWith(AndroidJUnit4::class)
class ExamPrepFlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()
    private val app = rule.robot()

    @Test
    fun practiceRunEndsOnResultsAndCountsTowardTheGoal() {
        app.onboard()
        app.clickTag("today-practice")
        answerAll(QuizSession.PRACTICE_SIZE)

        app.waitForText(app.str(R.string.practice_results))
        app.waitForTag("score")
        app.clickText(app.str(R.string.home))

        // 20 answers: the default daily goal is reached.
        app.waitForText(app.str(R.string.goal_done))
    }

    @Test
    fun mistakesGoToReviewMyMistakes() {
        app.onboard()
        app.clickTag("today-practice")
        answerAll(QuizSession.PRACTICE_SIZE) // always the first choice: some are wrong
        app.clickText(app.str(R.string.home))

        app.clickTag("hub-exams")
        app.clickTag("review-ICAF")
        app.waitForTag("choice-0")
    }

    @Test
    fun leavingAMockExamAsksFirst() {
        app.onboard()
        app.clickTag("hub-exams")
        app.clickTag("exam-ICAF")
        app.clickTag("mock-start-1")
        app.waitForTag("choice-0")

        app.back()
        app.waitForText(app.str(R.string.quit_title))
        app.clickText(app.str(R.string.cancel))
        app.waitForTag("choice-0")

        app.back()
        app.clickText(app.str(R.string.quit))
        app.waitForTag("mock-start-1") // back on the mock exams, Mock 1 still to take
        app.back()
        app.waitForTag("exam-card-ICAF")
    }

    /** Answers every question with its first choice, then Next (Finish on the last one). */
    private fun answerAll(count: Int) {
        repeat(count) {
            app.clickTag("choice-0")
            app.clickTag("next")
        }
    }
}
