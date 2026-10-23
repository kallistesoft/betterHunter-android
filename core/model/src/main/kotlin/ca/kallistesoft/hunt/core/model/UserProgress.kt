package ca.kallistesoft.hunt.core.model

import kotlinx.serialization.Serializable

/**
 * Local progress: last result per question, weak spots, daily streak and goal, photo of the day, and the hunter
 * profile from onboarding (type, weapons, provinces, real exams passed).
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
    /** Null until onboarding is done (first launch shows it). */
    val hunterType: HunterType? = null,
    val weapons: Set<Weapon> = emptySet(),
    /** Where the user hunts: its quizzes come first. */
    val homeProvince: Province? = null,
    /** Provinces whose quizzes are shown (Settings → My provinces), the home province included. */
    val provinces: Set<Province> = emptySet(),
    /** Exams the user says they passed for real ("I passed the real exam"); nothing is verified. */
    val passedExams: Set<String> = emptySet(),
    /** The "Welcome, new hunter" banner on Home, shown after passing a real exam until it is closed. */
    val newHunterBanner: Boolean = false,
    /** Questions a day for the goal ring (Settings: one of [DAILY_GOALS]). */
    val dailyGoal: Int = DEFAULT_DAILY_GOAL,
) {
    val onboarded: Boolean get() = hunterType != null && homeProvince != null

    /** New and experienced hunters get the Home built around "What is it?" instead of exam practice. */
    val isHunter: Boolean get() = hunterType != null && hunterType != HunterType.WORKING_ON_IT

    /** The user's exams: the home province's first (unless switched off), then the other provinces', each once. */
    val myExamIds: List<String>
        get() = (listOfNotNull(homeProvince) + Province.entries).filter { it in provinces }.distinct()
            .flatMap { it.examIds }
            .distinct()

    /** The answers to onboarding; the home province's quizzes are the only ones shown at first. */
    fun withProfile(type: HunterType, weapons: Set<Weapon>, home: Province): UserProgress =
        copy(hunterType = type, weapons = weapons, homeProvince = home, provinces = setOf(home))

    /** Adds or hides a province's quizzes; the last province can't be switched off. */
    fun withProvince(province: Province, enabled: Boolean): UserProgress = when {
        enabled -> copy(provinces = provinces + province)
        provinces == setOf(province) -> this
        else -> copy(provinces = provinces - province)
    }

    /** Settings → Hunter type. Going back to "Working on it" also drops the new-hunter welcome. */
    fun withHunterType(type: HunterType): UserProgress =
        copy(hunterType = type, newHunterBanner = newHunterBanner && type == HunterType.NEW_HUNTER)

    /** Settings → Hunting with; at least one weapon. */
    fun withWeapons(weapons: Set<Weapon>): UserProgress = if (weapons.isEmpty()) this else copy(weapons = weapons)

    /** Settings → Where you hunt: the new home province's quizzes are added and come first; others stay as they were. */
    fun withHomeProvince(home: Province): UserProgress = copy(homeProvince = home, provinces = provinces + home)

    fun withDailyGoal(goal: Int): UserProgress = if (goal in DAILY_GOALS) copy(dailyGoal = goal) else this

    /** "Yes, I passed": the user becomes a new hunter, welcomed by a banner on Home. */
    fun withExamPassed(examId: String): UserProgress =
        copy(passedExams = passedExams + examId, hunterType = HunterType.NEW_HUNTER, newHunterBanner = true)

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
        /** Questions a day for the dashboard's goal ring, until changed in Settings. */
        const val DEFAULT_DAILY_GOAL = 20

        /** The daily goals Settings offers. */
        val DAILY_GOALS = listOf(10, 20, 30)

        /** A missed question leaves the weak list after this many correct answers in a row. */
        const val CORRECT_TO_CLEAR = 2
    }
}
