package ca.kallistesoft.hunt

import android.view.KeyboardShortcutGroup
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.Reminder
import ca.kallistesoft.hunt.core.model.Weapon
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.navigation.ReminderLink
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** What the activity adds around the app: reminders' deep links and the keyboard shortcut list. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "en-w411dp-h891dp")
class MainActivityTest {
    @get:Rule
    val rule = createEmptyComposeRule()
    private val app = AppRobot(rule)

    @After
    fun tearDown() = app.close()

    @Test
    fun `a season reminder opens the season check over Home`() {
        app.launch(ReminderLink.intent(app.app, Reminder.Kind.SEASON), newHunter)
        app.waitFor(seasonCheck)
        app.clickBackArrow()
        app.clickText(app.str(R.string.quit))
        app.waitForTag("daily-card")
    }

    private val seasonCheck get() = hasText(app.str(R.string.season_check_overline), ignoreCase = true)

    @Test
    fun `a reminder is dropped while onboarding shows`() {
        app.launch(ReminderLink.intent(app.app, Reminder.Kind.DAILY))
        app.waitForTag("choice-WORKING_ON_IT")
    }

    @Test
    fun `a reminder tapped while the app runs opens its screen`() {
        runBlocking { app.app.userData.newHunter() }
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        app.waitForTag("daily-card")
        controller.newIntent(ReminderLink.intent(app.app, Reminder.Kind.SEASON))
        app.waitFor(seasonCheck)
        controller.pause().stop().destroy()
    }

    @Test
    fun `the quiz shortcuts are listed in the system's shortcut helper`() {
        app.launch(newHunter)
        app.scenario.onActivity { activity ->
            val groups = mutableListOf<KeyboardShortcutGroup>()
            activity.onProvideKeyboardShortcuts(groups, null, 0)
            assertEquals(app.str(R.string.shortcuts_quiz), groups.last().label)
        }
    }

    private companion object {
        val newHunter: suspend UserDataRepository.() -> Unit = {
            saveProfile(HunterType.NEW_HUNTER, setOf(Weapon.FIREARM), Province.QC)
        }
    }
}
