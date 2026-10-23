package ca.kallistesoft.hunt.core.model

/** A local notification to show (P11): today's questions, or the hunter's season coming. */
data class Reminder(val kind: Kind, val day: Long, val minute: Int, val seasonStart: Long? = null) {
    enum class Kind { DAILY, SEASON }

    /** Days from the reminder to the hunter's season (season reminders). */
    val daysLeft: Int? get() = seasonStart?.let { (it - day).toInt() }

    companion object {
        /** Season reminders: 3 weeks before the hunter's date, and the day before. */
        val SEASON_DAYS_BEFORE = listOf(21L, 1L)

        /**
         * The next reminder from now: at most one a day (the season one wins), at the hunter's usual time (never
         * between 9 pm and 8 am), and no daily reminder once today's questions are done. Null when none is on.
         */
        fun next(progress: UserProgress, today: Long, nowMinute: Int): Reminder? {
            if (!progress.isFieldHunter) return null
            val minute = progress.reminderMinute
            val firstDay = if (nowMinute < minute) today else today + 1
            val start = progress.seasonStart
            val season = if (progress.seasonReminders && start != null) {
                SEASON_DAYS_BEFORE.map { start - it }.filter { it >= firstDay }.minOrNull()
            } else {
                null
            }
            val dailyDone = progress.dailyResults(today).size >= FieldTraining.DAILY_SPECIES + FieldTraining.DAILY_GENERAL
            val daily = when {
                !progress.dailyReminders -> null
                firstDay == today && dailyDone -> today + 1
                else -> firstDay
            }
            return when {
                season != null && (daily == null || season <= daily) -> Reminder(Kind.SEASON, season, minute, start)
                daily != null -> Reminder(Kind.DAILY, daily, minute)
                else -> null
            }
        }
    }
}
