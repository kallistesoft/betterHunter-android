package ca.kallistesoft.hunt.navigation

import ca.kallistesoft.hunt.core.model.FieldRun
import ca.kallistesoft.hunt.core.model.Reminder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderLinkTest {
    @Test
    fun `each reminder opens its screen over Home`() {
        assertEquals(listOf(Home, FieldQuiz(FieldRun.DAILY)), ReminderLink.backStack(Reminder.Kind.DAILY.name))
        assertEquals(listOf(Home, SeasonDate), ReminderLink.backStack(Reminder.Kind.SEASON.name))
    }

    @Test
    fun `a launch without a known reminder opens the app as usual`() {
        assertNull(ReminderLink.backStack(null as String?))
        assertNull(ReminderLink.backStack(""))
        assertNull(ReminderLink.backStack("daily"))
        assertNull(ReminderLink.backStack("Settings"))
    }
}
