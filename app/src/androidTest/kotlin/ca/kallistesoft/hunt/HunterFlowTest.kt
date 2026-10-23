package ca.kallistesoft.hunt

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Adding a province's quizzes, and "I passed the real exam" turning Home into the new hunter's. */
@RunWith(AndroidJUnit4::class)
class HunterFlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()
    private val app = rule.robot()

    @Test
    fun anotherProvinceAddsItsQuizzes() {
        app.onboard(province = "QC")
        app.clickTag("hub-exams")
        app.clickTag("other-provinces")
        app.clickTag("province-AB")
        app.back()

        app.waitForTag("exam-card-ICAF")
        app.waitForTag("exam-card-ABHE")
    }

    @Test
    fun passingTheRealExamMakesANewHunter() {
        app.onboard()
        app.clickTag("hub-exams")
        app.clickTag("passed-ICAF")
        app.clickTag("passed-yes")

        // Straight back to Home, the new hunter's version.
        app.waitForTag("new-hunter-banner")
        app.waitForTag("today-play-id")
        rule.onNode(hasTestTag("today-practice")).assertDoesNotExist()

        app.clickTag("banner-close")
        app.waitForGone("new-hunter-banner")
    }

    @Test
    fun notYetChangesNothing() {
        app.onboard()
        app.clickTag("hub-exams")
        app.clickTag("passed-ICAF")
        app.clickTag("passed-not-yet")

        app.waitForTag("passed-ICAF")
        app.back()
        app.waitForTag("today-practice")
    }
}
