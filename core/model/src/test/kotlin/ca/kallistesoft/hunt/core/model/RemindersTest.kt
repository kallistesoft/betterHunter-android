package ca.kallistesoft.hunt.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RemindersTest {
    private val today = 20_000L
    private val hunter = UserProgress().withProfile(HunterType.NEW_HUNTER, setOf(Weapon.FIREARM), Province.QC)
    private val evening = 19 * 60

    @Test
    fun `no reminder unless one is on, and only for new hunters`() {
        assertNull(Reminder.next(hunter, today, 0))
        assertNull(Reminder.next(hunter.copy(dailyReminders = true, hunterType = HunterType.WORKING_ON_IT), today, 0))
    }

    @Test
    fun `the daily reminder comes at 7 pm until the playing time is learned`() {
        val p = hunter.copy(dailyReminders = true)
        assertEquals(Reminder(Reminder.Kind.DAILY, today, evening), Reminder.next(p, today, 9 * 60))
        assertEquals(Reminder(Reminder.Kind.DAILY, today + 1, evening), Reminder.next(p, today, 20 * 60))
    }

    @Test
    fun `the daily reminder skips a day whose questions are done`() {
        val done = (1..6).fold(hunter.copy(dailyReminders = true)) { p, i -> p.recordDaily("q$i", true, today) }
        assertEquals(today + 1, Reminder.next(done, today, 9 * 60)?.day)
    }

    @Test
    fun `reminders never come at night`() {
        val night = hunter.copy(dailyReminders = true, playMinute = 23 * 60)
        assertEquals(UserProgress.QUIET_FROM_MINUTE - 30, Reminder.next(night, today, 0)?.minute)
        val early = hunter.copy(dailyReminders = true, playMinute = 6 * 60)
        assertEquals(UserProgress.QUIET_UNTIL_MINUTE, Reminder.next(early, today, 0)?.minute)
    }

    @Test
    fun `season reminders come 3 weeks and 1 day before, instead of the daily one`() {
        val start = today + 30
        val p = hunter.copy(dailyReminders = true, seasonReminders = true, seasonStart = start)
        assertEquals(Reminder.Kind.DAILY, Reminder.next(p, today, 0)?.kind)
        val threeWeeks = Reminder.next(p, start - 21, 0)
        assertEquals(Reminder(Reminder.Kind.SEASON, start - 21, evening, start), threeWeeks)
        assertEquals(21, threeWeeks?.daysLeft)
        assertEquals(Reminder(Reminder.Kind.SEASON, start - 1, evening, start), Reminder.next(p.copy(dailyReminders = false), start - 20, 0))
        assertNull(Reminder.next(p.copy(dailyReminders = false), start, 0))
    }
}
