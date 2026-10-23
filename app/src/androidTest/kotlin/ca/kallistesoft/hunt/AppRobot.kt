package ca.kallistesoft.hunt

import androidx.annotation.StringRes
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.SemanticsMatcher
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.rules.ActivityScenarioRule
import ca.kallistesoft.hunt.core.ui.R

typealias AppRule = AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>

/**
 * Drives the real app in flow tests: finds screens by their test tags and the app's own strings (so tests follow
 * the device language), and waits for each screen, since saving progress and loading content are asynchronous.
 */
@OptIn(ExperimentalTestApi::class)
class AppRobot(private val rule: AppRule) {
    fun str(@StringRes id: Int, vararg args: Any): String = rule.activity.getString(id, *args)

    fun waitFor(matcher: SemanticsMatcher, timeoutMillis: Long = TIMEOUT) = rule.waitUntilAtLeastOneExists(matcher, timeoutMillis)

    fun waitForTag(tag: String) = waitFor(hasTestTag(tag))

    fun waitForText(text: String) = waitFor(hasText(text))

    fun waitForGone(tag: String) = rule.waitUntilDoesNotExist(hasTestTag(tag), TIMEOUT)

    fun clickTag(tag: String) {
        waitForTag(tag)
        rule.onNode(hasTestTag(tag)).performClick()
    }

    fun clickText(text: String) {
        waitForText(text)
        rule.onNode(hasText(text)).performClick()
    }

    fun back() = Espresso.pressBack()

    /** First launch: answers onboarding ([hunterType] and [province] are choice tags) and waits for Home. */
    fun onboard(hunterType: String = "WORKING_ON_IT", province: String = "QC") {
        clickTag("choice-$hunterType")
        clickTag("onboarding-continue")
        waitForText(str(R.string.hunt_with_title))
        clickTag("onboarding-continue") // firearm is ticked by default
        clickTag("choice-$province")
        clickTag("onboarding-continue")
        // A new hunter's Home leads with today's questions (P11); the others' with exam training.
        waitForTag(if (hunterType == "NEW_HUNTER") "daily-card" else "hub-exams")
    }

    /** Answers every question of the run on screen with the first choice, up to its results. */
    fun answerAll(count: Int) {
        repeat(count) {
            clickTag("choice-0")
            clickTag("next")
        }
    }

    private companion object {
        const val TIMEOUT = 10_000L
    }
}

fun AppRule.robot() = AppRobot(this)
