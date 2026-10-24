package ca.kallistesoft.hunt.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RemindersTest {
    private val today = 20_000L
    private val hunter = UserProgress().withProfile(HunterType.HUNTER, setOf(Weapon.FIREARM), Province.QC)
    private val evening = 19 * 60

    @Test
    fun `no reminder unless one is on, and only for hunters`() {
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
        val night = hunter.copy(dailyReminders = true).playedAt(23 * 60)
        assertEquals(UserProgress.QUIET_FROM_MINUTE - 30, Reminder.next(night, today, 0)?.minute)
        val early = hunter.copy(dailyReminders = true).playedAt(6 * 60)
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
        // The season has started: the next one is next year's late August.
        assertEquals(Reminder(Reminder.Kind.SEASON, LocalDate.of(2025, 8, 25).toEpochDay(), evening), Reminder.next(p.copy(dailyReminders = false), start, 0))
    }

    @Test
    fun `at most one a day, none later on a day one was shown`() {
        val p = hunter.copy(dailyReminders = true).withReminderShown(Reminder.Kind.SEASON, today)
        assertEquals(today + 1, Reminder.next(p, today, 9 * 60)?.day)
    }

    @Test
    fun `after 3 ignored daily reminders they come every other day, until the app is opened`() {
        val shown = (0L until 3).fold(hunter.copy(dailyReminders = true)) { p, d -> p.withReminderShown(Reminder.Kind.DAILY, today + d) }
        val last = today + 2
        assertEquals(3, shown.ignoredReminders)
        assertEquals(last + 2, Reminder.next(shown, last, 20 * 60)?.day)
        assertEquals(last + 1, Reminder.next(shown.withAppOpened(), last, 20 * 60)?.day)
        assertEquals(0, shown.withAppOpened().ignoredReminders)
        // Two ignored: still every day.
        assertEquals(today + 2, Reminder.next(hunter.copy(dailyReminders = true, reminderDay = today + 1, ignoredReminders = 2), today + 1, 20 * 60)?.day)
    }

    @Test
    fun `a season reminder doesn't count as an ignored daily one`() {
        assertEquals(0, hunter.withReminderShown(Reminder.Kind.SEASON, today).ignoredReminders)
        assertEquals(hunter, hunter.withAppOpened())
    }

    @Test
    fun `without a date, one Hunting season is coming on August 25`() {
        val p = hunter.copy(seasonReminders = true)
        // Today is October 4, 2024: next year's.
        val next = Reminder.next(p, today, 0)
        assertEquals(Reminder(Reminder.Kind.SEASON, LocalDate.of(2025, 8, 25).toEpochDay(), evening), next)
        assertNull(next?.daysLeft)
        val july = LocalDate.of(2025, 7, 1).toEpochDay()
        assertEquals(LocalDate.of(2025, 8, 25).toEpochDay(), Reminder.next(p, july, 0)?.day)
    }

    @Test
    fun `once the date has passed, late August asks about the next season`() {
        val start = LocalDate.of(2024, 9, 1).toEpochDay()
        val p = hunter.copy(seasonReminders = true, seasonStart = start)
        assertEquals(LocalDate.of(2025, 8, 25).toEpochDay(), Reminder.next(p, today, 0)?.day)
        // A date set for next year: no late August reminder before it.
        val nextYear = LocalDate.of(2025, 11, 1).toEpochDay()
        assertEquals(nextYear - 21, Reminder.next(p.copy(seasonStart = nextYear), today, 0)?.day)
        assertEquals(LocalDate.of(2026, 8, 25).toEpochDay(), Reminder.next(p.copy(seasonStart = nextYear), nextYear, 0)?.day)
    }

    @Test
    fun `the job shows today's reminder once its time has come, but never at night`() {
        val p = hunter.copy(dailyReminders = true)
        assertNull(Reminder.due(p, today, evening - 1))
        assertEquals(Reminder(Reminder.Kind.DAILY, today, evening), Reminder.due(p, today, evening + 90))
        assertNull(Reminder.due(p, today, UserProgress.QUIET_FROM_MINUTE))
        assertNull(Reminder.due(p.withReminderShown(Reminder.Kind.DAILY, today), today, evening + 1))
    }

    @Test
    fun `Done for today asks again a week after Not now, never once turned on or refused`() {
        assertTrue(hunter.asksDailyReminders(today))
        val notNow = hunter.copy(dailyReminderNotNowDay = today)
        assertFalse(notNow.asksDailyReminders(today + 1))
        assertFalse(notNow.asksDailyReminders(today + 6))
        assertTrue(notNow.asksDailyReminders(today + 7))
        assertFalse(hunter.copy(dailyReminderAsked = true).asksDailyReminders(today))
        assertFalse(hunter.copy(dailyReminders = true).asksDailyReminders(today))
    }

    /** Played 5 days at [minute]: the usual time is learned. */
    private fun UserProgress.playedAt(minute: Int) = (1L..5).fold(this) { p, d -> p.withPlayTime(today - d, minute) }
}
