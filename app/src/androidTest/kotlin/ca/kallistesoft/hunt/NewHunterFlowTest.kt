package ca.kallistesoft.hunt

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/** A new hunter from onboarding (P11): today's 6 questions, a field skill, and the season start date. */
@RunWith(AndroidJUnit4::class)
class NewHunterFlowTest {
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()
    private val app = rule.robot()

    @Test
    fun todaysQuestionsEndOnDoneForToday() {
        app.onboard(hunterType = "NEW_HUNTER")
        rule.onNode(hasTestTag("hub-exams")).assertDoesNotExist()
        app.clickTag("daily-continue")
        app.answerAll(6)

        app.waitForTag("daily-done")
        // Reminders are offered here, once; "Not now" doesn't ask for the permission.
        app.clickTag("nudge-not-now")
        app.waitForGone("nudge")
        app.clickTag("daily-home")
        app.waitForTag("daily-all-done")
    }

    @Test
    fun aSkillRunShowsTheSkillsProgress() {
        app.onboard(hunterType = "NEW_HUNTER")
        app.clickTag("hub-skills")
        app.clickTag("skill-SHOT_PLACEMENT")
        app.answerAll(10)

        app.waitForTag("score")
        app.waitFor(hasTestTag("skill-change") or hasTestTag("staying-mastered"))
        app.clickTag("field-skills")
        app.waitForTag("mix-card")
    }

    @Test
    fun theSeasonStartDateIsSavedAndShownOnHome() {
        app.onboard(hunterType = "NEW_HUNTER")
        app.clickTag("settings")
        app.clickTag("settings-season")
        app.clickTag("day-${LocalDate.now().toEpochDay()}")
        app.clickTag("season-save")

        app.clickTag("season-no-thanks")
        app.waitForTag("settings-season-reminders")
        app.back()
        // Today is the hunter's first day: Home counts down to it.
        app.waitForTag("season-card")
        app.waitForText(app.str(ca.kallistesoft.hunt.core.ui.R.string.season_today))
    }
}
