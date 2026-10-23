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
    /** Day of the last right answer per question: right answers count for [ANSWER_LIFETIME_DAYS] (P11). */
    val correctDay: Map<String, Long> = emptyMap(),
    /** New hunter's daily questions: the day they were drawn for and the answers given that day. */
    val dailyDay: Long? = null,
    val dailyResults: Map<String, Boolean> = emptyMap(),
    /** The hunter's own first hunting day ("When does your season start?"), as a day number. */
    val seasonStart: Long? = null,
    /** Local reminders, opt-in: today's questions, and before the hunter's season. */
    val dailyReminders: Boolean = false,
    val seasonReminders: Boolean = false,
    /** "Want a nudge tomorrow?" was answered (asked once). */
    val dailyReminderAsked: Boolean = false,
    /** When this hunter usually plays, in minutes after midnight (a moving average); null until they play. */
    val playMinute: Int? = null,
) {
    val onboarded: Boolean get() = hunterType != null && homeProvince != null

    /**
     * A new hunter who came from onboarding (no real exam passed in the app): Home leads with today's 6 questions,
     * Field skills replaces Exam training, and the season check and reminders apply (P11).
     */
    val isFieldHunter: Boolean get() = hunterType == HunterType.NEW_HUNTER && passedExams.isEmpty()

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
            correctDay = if (correct) correctDay + (questionId to today) else correctDay - questionId,
            weak = newWeak,
            dayCount = answeredToday(today) + 1,
            dayCountDay = today,
            streak = newStreak,
            streakDay = newStreakDay,
        )
    }

    /** A right answer from the last [ANSWER_LIFETIME_DAYS]; older ones count as unanswered again. */
    fun isFresh(questionId: String, today: Long): Boolean =
        lastResult[questionId] == true && correctDay[questionId].let { it == null || today - it < ANSWER_LIFETIME_DAYS }

    fun skillSummary(skill: FieldSkill, pool: List<Question>, today: Long): SkillSummary {
        val fresh = pool.count { isFresh(it.id, today) }
        val expired = pool.count { lastResult[it.id] == true && !isFresh(it.id, today) }
        return SkillSummary(skill, pool.size, if (pool.isEmpty()) 0f else fresh.toFloat() / pool.size, expired)
    }

    /** Answers one of today's daily questions; it counts like any answer (goal, streak, skills). */
    fun recordDaily(questionId: String, correct: Boolean, today: Long): UserProgress {
        val results = if (dailyDay == today) dailyResults else emptyMap()
        return record(questionId, correct, today).copy(dailyDay = today, dailyResults = results + (questionId to correct))
    }

    /** Today's daily answers (empty on a new day). */
    fun dailyResults(today: Long): Map<String, Boolean> = if (dailyDay == today) dailyResults else emptyMap()

    /** Learns when this hunter plays: a moving average of the minutes after midnight. */
    fun withPlayTime(minute: Int): UserProgress =
        copy(playMinute = playMinute?.let { (it * 4 + minute) / 5 } ?: minute)

    /** When reminders come: when this hunter usually plays, kept between 8 am and 9 pm (7 pm until learned). */
    val reminderMinute: Int
        get() = (playMinute ?: DEFAULT_PLAY_MINUTE).coerceIn(QUIET_UNTIL_MINUTE, QUIET_FROM_MINUTE - 30)

    fun withSeasonStart(day: Long?): UserProgress = copy(seasonStart = day)

    fun weakIds(pool: List<Question>): Set<String> = pool.mapNotNullTo(mutableSetOf()) { q -> q.id.takeIf { it in weak } }

    /** Share of the pool answered right in the last 3 months (0..1): Field skills' readiness. */
    fun readinessFresh(pool: List<Question>, today: Long): Float =
        if (pool.isEmpty()) 0f else pool.count { isFresh(it.id, today) }.toFloat() / pool.size

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

        /** Right answers count for about 3 months, so mastered skills come back before each season. */
        const val ANSWER_LIFETIME_DAYS = 91

        /** Reminders: 7 pm until the usual playing time is learned, never between 9 pm and 8 am. */
        const val DEFAULT_PLAY_MINUTE = 19 * 60
        const val QUIET_FROM_MINUTE = 21 * 60
        const val QUIET_UNTIL_MINUTE = 8 * 60
    }
}
