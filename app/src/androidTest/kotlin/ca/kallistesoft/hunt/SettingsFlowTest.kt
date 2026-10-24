package ca.kallistesoft.hunt

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ca.kallistesoft.hunt.core.ui.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Settings, from the gear on Home: each change shows on Home and in Exam training. */
@RunWith(AndroidJUnit4::class)
class SettingsFlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()
    private val app = rule.robot()

    @Test
    fun changingTheHunterTypeChangesHome() {
        app.onboard()
        app.clickTag("settings")
        app.clickTag("settings-hunter-type")
        app.clickTag("choice-HUNTER")
        app.clickTag("profile-save")
        app.waitForText(app.str(R.string.hunter_hunter))
        app.back()

        // A hunter without a passed exam gets the field Home (P11): Field skills instead of Exam training.
        app.waitForTag("hub-skills")
        rule.onNode(hasTestTag("hub-exams")).assertDoesNotExist()
    }

    @Test
    fun theDailyGoalShowsOnHome() {
        app.onboard()
        app.clickTag("settings")
        app.clickTag("toggle-30")
        app.back()

        app.waitFor(hasText(app.str(R.string.goal_of, 30)))
    }

    @Test
    fun movingToAnotherProvinceKeepsTheQuizzesAdded() {
        app.onboard(province = "QC")
        app.clickTag("settings")
        app.clickTag("settings-home")
        app.clickTag("choice-AB")
        app.clickTag("profile-save")
        app.back()

        app.clickTag("hub-exams")
        app.waitForTag("exam-card-ABHE")
        app.waitForTag("exam-card-PAL")
        app.waitForTag("exam-card-ICAF")
    }

    @Test
    fun aboutOpensFromSettings() {
        app.onboard()
        app.clickTag("settings")
        app.clickTag("settings-about")
        app.waitForTag("disclaimer")
    }
}
