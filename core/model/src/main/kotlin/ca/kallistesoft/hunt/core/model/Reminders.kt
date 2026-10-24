package ca.kallistesoft.hunt.core.model

import java.time.LocalDate
import java.time.MonthDay

/**
 * A local notification to show (P11): today's questions, or the hunter's season coming. A season reminder without
 * [seasonStart] is the late-August "Hunting season is coming", for a hunter with no date for the coming season.
 */
data class Reminder(val kind: Kind, val day: Long, val minute: Int, val seasonStart: Long? = null) {
    enum class Kind { DAILY, SEASON }

    /** Days from the reminder to the hunter's season (season reminders). */
    val daysLeft: Int? get() = seasonStart?.let { (it - day).toInt() }

    companion object {
        /** Season reminders: 3 weeks before the hunter's date, and the day before. */
        val SEASON_DAYS_BEFORE = listOf(21L, 1L)

        /** Without a date for the coming season: one "Hunting season is coming" on this day. */
        val SEASON_COMING: MonthDay = MonthDay.of(8, 25)

        /** Daily reminders ignored in a row (the app not opened since) before they come every other day. */
        const val BACK_OFF_AFTER = 3

        /**
         * The next reminder from now: at most one a day (the season one wins), at the hunter's usual time (never
         * between 9 pm and 8 am), and no daily reminder once today's questions are done. Null when none is on.
         */
        fun next(progress: UserProgress, today: Long, nowMinute: Int): Reminder? {
            if (!progress.isHunter) return null
            val minute = progress.reminderMinute
            // One a day: nothing more today once its time has passed or once one was shown.
            val firstDay = if (nowMinute < minute && progress.reminderDay != today) today else today + 1
            val season = if (progress.seasonReminders) seasonReminder(progress.seasonStart, firstDay, minute) else null
            val daily = if (progress.dailyReminders) dailyDay(progress, today, firstDay) else null
            return when {
                season != null && (daily == null || season.day <= daily) -> season
                daily != null -> Reminder(Kind.DAILY, daily, minute)
                else -> null
            }
        }

        /**
         * The reminder to show now, from the reminder job: today's, even late (the job can be delayed by Doze), but
         * never in the quiet hours (then it's dropped and the next one is planned).
         */
        fun due(progress: UserProgress, today: Long, nowMinute: Int): Reminder? =
            next(progress, today, nowMinute = 0)
                ?.takeIf { it.day == today && it.minute <= nowMinute && nowMinute < UserProgress.QUIET_FROM_MINUTE }

        private fun dailyDay(progress: UserProgress, today: Long, firstDay: Long): Long {
            val dailyDone = progress.dailyResults(today).size >= FieldTraining.DAILY_SPECIES + FieldTraining.DAILY_GENERAL
            val day = if (firstDay == today && dailyDone) today + 1 else firstDay
            // Ignored again and again: every other day, until the hunter opens the app.
            val last = progress.reminderDay
            return if (progress.ignoredReminders >= BACK_OFF_AFTER && last != null) maxOf(day, last + 2) else day
        }

        /** 3 weeks and 1 day before the hunter's date; once it has passed (or without one), late August. */
        private fun seasonReminder(start: Long?, firstDay: Long, minute: Int): Reminder {
            val beforeStart = start?.let { s -> SEASON_DAYS_BEFORE.map { s - it }.filter { it >= firstDay }.minOrNull() }
            if (beforeStart != null) return Reminder(Kind.SEASON, beforeStart, minute, start)
            // The date is for one season: late August of a later year asks about the next one.
            val from = LocalDate.ofEpochDay(firstDay)
            val afterYear = start?.let { LocalDate.ofEpochDay(it).year } ?: Int.MIN_VALUE
            val coming = generateSequence(from.year) { it + 1 }
                .map { SEASON_COMING.atYear(it) }
                .first { it.year > afterYear && !it.isBefore(from) }
            return Reminder(Kind.SEASON, coming.toEpochDay(), minute)
        }
    }
}
