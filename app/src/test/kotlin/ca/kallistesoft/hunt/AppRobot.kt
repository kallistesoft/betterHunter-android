package ca.kallistesoft.hunt

import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.ui.R
import kotlinx.coroutines.runBlocking

/**
 * Drives the real app (real Hilt graph, content and DataStore) under Robolectric, like the flow tests in androidTest:
 * finds screens by their test tags and the app's own strings, scrolling lists to reach them.
 */
@OptIn(ExperimentalTestApi::class)
class AppRobot(private val rule: ComposeTestRule) {
    val app: BetterHunterApplication = ApplicationProvider.getApplicationContext()
    lateinit var scenario: ActivityScenario<MainActivity>

    /** Saves [progress] (through the app's own repository), then starts the app. */
    fun launch(progress: suspend UserDataRepository.() -> Unit = {}) {
        runBlocking { app.userData.progress() }
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    /** Like [launch], with the [intent] a notification sends. */
    fun launch(intent: Intent, progress: suspend UserDataRepository.() -> Unit = {}) {
        runBlocking { app.userData.progress() }
        scenario = ActivityScenario.launch(intent)
    }

    /** Closes the app if [launch] started it. */
    fun close() {
        if (::scenario.isInitialized) scenario.close()
    }

    fun str(@StringRes id: Int, vararg args: Any): String = app.getString(id, *args)

    fun waitFor(matcher: SemanticsMatcher) {
        rule.waitUntil(TIMEOUT) { exists(matcher) || scrollTo(matcher) }
    }

    fun waitForTag(tag: String) = waitFor(hasTestTag(tag))

    fun waitForGone(tag: String) = rule.waitUntilDoesNotExist(hasTestTag(tag), TIMEOUT)

    fun click(matcher: SemanticsMatcher) {
        waitFor(matcher)
        val node = rule.onAllNodes(matcher).onFirst()
        runCatching { node.performScrollTo() } // Only in scrolling columns; lazy lists were scrolled by waitFor.
        node.performClick()
        rule.waitForIdle()
    }

    fun clickTag(tag: String) = click(hasTestTag(tag))

    fun clickText(text: String) = click(hasText(text))

    /** The back arrow (or cross) of the screen's top bar. */
    fun clickBackArrow() = click(hasContentDescription(str(R.string.back)))

    /** The system back button or gesture. */
    fun pressBack() {
        scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    /** Answers [count] questions with the first choice, then Next (Finish on the last one). */
    fun answerAll(count: Int) {
        repeat(count) {
            clickTag("choice-0")
            clickTag("next")
        }
    }

    fun exists(matcher: SemanticsMatcher) = rule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()

    /** Scrolls the list on screen to [matcher]; false when there's no list or nothing matches yet. */
    private fun scrollTo(matcher: SemanticsMatcher): Boolean = runCatching {
        rule.onAllNodes(hasScrollToNodeAction()).onFirst().performScrollToNode(matcher)
    }.isSuccess && exists(matcher)

    private companion object {
        const val TIMEOUT = 10_000L
    }
}
