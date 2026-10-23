package ca.kallistesoft.hunt.smoketest

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.regex.Pattern

/**
 * The release build from first launch to a question and back, then a restart: bundled questions load and progress
 * is saved and read back (kotlinx.serialization and DataStore after R8). Black-box: by visible English text.
 */
@RunWith(AndroidJUnit4::class)
class ReleaseSmokeTest {
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    fun freshInstall() {
        device.executeShellCommand("pm clear $PACKAGE")
        device.executeShellCommand("cmd locale set-app-locales $PACKAGE --locales en-CA")
    }

    @Test
    fun firstLaunchToAQuestionThenProgressSurvivesARestart() {
        launch()

        // Onboarding
        tap("Working on it")
        tap("Continue")
        waitFor("What do you hunt with?")
        tap("Continue")
        tap("Québec")
        tap("Continue")

        // Home, then one practice question from the bundled banks, answered with its explanation
        tap("Practise now")
        waitFor("Question 1/20")
        tap("A")
        assertNotNull("no answer feedback", device.wait(Until.findObject(By.text(Pattern.compile("Correct!|Incorrect"))), TIMEOUT))
        device.pressBack()
        tap("Quit")
        waitFor("Practise now")

        // Restart: the saved profile (and its enums) reads back, so Home opens, not onboarding
        device.executeShellCommand("am force-stop $PACKAGE")
        launch()
        waitFor("Practise now")
        assertNull("onboarding shown again", device.findObject(By.text("What kind of hunter are you?")))
    }

    private fun launch() {
        device.executeShellCommand("monkey -p $PACKAGE -c android.intent.category.LAUNCHER 1")
        assertNotNull("app didn't start", device.wait(Until.hasObject(By.pkg(PACKAGE).depth(0)), TIMEOUT))
    }

    private fun waitFor(text: String) {
        assertNotNull("\"$text\" not shown", device.wait(Until.findObject(By.text(text)), TIMEOUT))
    }

    private fun tap(text: String) {
        waitFor(text)
        device.findObject(By.text(text)).click()
    }

    private companion object {
        const val PACKAGE = "ca.kallistesoft.hunt"
        const val TIMEOUT = 10_000L
    }
}
