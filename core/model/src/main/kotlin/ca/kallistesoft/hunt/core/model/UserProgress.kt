package ca.kallistesoft.hunt.core.model

import kotlinx.serialization.Serializable

/**
 * Local progress: last result per question, weak spots, daily streak and goal, chosen exams, photo of the day.
 * Days are local calendar days since the epoch (`LocalDate.toEpochDay()`), passed in so the rules are testable.
 */
@Serializable
data class UserProgress(
    val lastResult: Map<String, Boolean> = emptyMap(),
    /** Missed questions, with how many correct answers in a row are still needed to clear them. */
    val weak: Map<String, Int> = emptyMap(),
    val streak: Int = 0,
    val streakDay: Long? = null,
    val dayCount: Int = 0,
    val dayCountDay: Long? = null,
    val photoOfDayDay: Long? = null,
    val photoOfDayChoice: Int? = null,
    /** Exams the user is preparing for; null until they've chosen (first launch shows the picker). */
    val myExams: List<String>? = null,
) {
    /** Questions answered today (any exam), for the daily goal. */
    fun answeredToday(today: Long): Int = if (dayCountDay == today) dayCount else 0

    /** Choice made on today's photo of the day, or null if not answered yet today. */
    fun photoOfDayChoice(today: Long): Int? = if (photoOfDayDay == today) photoOfDayChoice else null

    fun withPhotoOfDayChoice(choice: Int, today: Long): UserProgress =
        copy(photoOfDayDay = today, photoOfDayChoice = choice)

    fun currentStreak(today: Long): Int = if (streakDay != null && today - streakDay <= 1) streak else 0

    fun record(questionId: String, correct: Boolean, today: Long): UserProgress {
        val newWeak = when {
            !correct -> weak + (questionId to CORRECT_TO_CLEAR)
            questionId in weak -> {
                val left = weak.getValue(questionId) - 1
                if (left <= 0) weak - questionId else weak + (questionId to left)
            }
            else -> weak
        }
        val (newStreak, newStreakDay) = when (streakDay) {
            today -> streak to streakDay
            today - 1 -> (streak + 1) to today
            else -> 1 to today
        }
        return copy(
            lastResult = lastResult + (questionId to correct),
            weak = newWeak,
            dayCount = answeredToday(today) + 1,
            dayCountDay = today,
            streak = newStreak,
            streakDay = newStreakDay,
        )
    }

    fun weakIds(pool: List<Question>): Set<String> = pool.mapNotNullTo(mutableSetOf()) { q -> q.id.takeIf { it in weak } }

    /** Share of the pool whose last answer was correct (0..1). */
    fun readiness(pool: List<Question>): Float =
        if (pool.isEmpty()) 0f else pool.count { lastResult[it.id] == true }.toFloat() / pool.size

    companion object {
        /** Questions a day for the dashboard's goal ring. */
        const val DAILY_GOAL = 20

        /** A missed question leaves the weak list after this many correct answers in a row. */
        const val CORRECT_TO_CLEAR = 2
    }
}
