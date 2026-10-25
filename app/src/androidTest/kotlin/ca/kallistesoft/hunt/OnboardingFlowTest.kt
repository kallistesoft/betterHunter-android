package ca.kallistesoft.hunt

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ca.kallistesoft.hunt.core.ui.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** First launch: the three onboarding questions, then Home built around the province's hunting exam. */
@RunWith(AndroidJUnit4::class)
class OnboardingFlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()
    private val app = rule.robot()

    @Test
    fun answersTheThreeQuestionsThenShowsHome() {
        app.waitForText(app.str(R.string.hunter_type_title))
        rule.onNode(hasTestTag("onboarding-continue")).assertIsNotEnabled()

        app.onboard(province = "QC")

        app.waitForTag("today-practice")
        app.clickTag("hub-exams")
        app.waitForTag("exam-card-ICAF")
        app.waitForTag("exam-card-PAL")
    }

    @Test
    fun albertaSetsItsCourseAndThePal() {
        app.onboard(province = "AB")
        app.clickTag("hub-exams")
        app.waitForTag("exam-card-ABHE")
        app.waitForTag("exam-card-PAL")
    }

    @Test
    fun backReturnsToThePreviousQuestionKeepingTheAnswer() {
        app.clickTag("choice-HUNTER")
        app.clickTag("onboarding-continue")
        app.waitForText(app.str(R.string.hunt_with_title))

        app.back()

        app.waitForText(app.str(R.string.hunter_type_title))
        rule.onNode(hasTestTag("choice-HUNTER")).assertIsSelected()
        rule.onNode(hasTestTag("onboarding-continue")).assertIsEnabled()
    }

    @Test
    fun answersSurviveTheActivityBeingRecreated() {
        app.clickTag("choice-WORKING_ON_IT")
        app.clickTag("onboarding-continue")
        app.clickTag("choice-BOW")

        rule.activityRule.scenario.recreate()

        app.waitForText(app.str(R.string.hunt_with_title))
        rule.onNode(hasTestTag("choice-BOW")).assertIsOn()
    }
}
