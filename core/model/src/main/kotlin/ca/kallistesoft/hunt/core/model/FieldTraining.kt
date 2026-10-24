package ca.kallistesoft.hunt.core.model

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.MonthDay
import kotlin.random.Random

/** The hunter's runs (P11): one skill, today's mix, the season check, or today's questions. */
@Serializable
enum class FieldRun(val size: Int) {
    SKILL(10),

    /** "Today's mix", or "Keep your edge" once every skill is mastered. */
    MIX(10),
    SEASON_CHECK(15),

    /** Today's questions: 3 species, then 3 general. */
    DAILY(6),

    /** "My mistakes": missed field skill questions until answered right twice. */
    MISTAKES(20),
}

/** Draws the questions of the hunter's runs. Pure, so the rules are tested on the JVM. */
object FieldTraining {
    /** Questions from mastered skills kept in each mix, so they stay sharp. */
    const val KEEP_SHARP = 2

    const val DAILY_SPECIES = 3
    const val DAILY_GENERAL = 3

    /** One skill: what isn't right (or has expired) first, then the oldest right answers. */
    fun skill(pool: List<Question>, progress: UserProgress, today: Long, random: Random, size: Int = FieldRun.SKILL.size): List<Question> =
        ordered(pool, progress, today, random).take(size)

    /**
     * Today's mix: mostly from the weakest skills (the weakest gives two for each one the others give), plus 1 or 2
     * from mastered skills. With every skill mastered, "Keep your edge": all skills, oldest answers first.
     */
    fun mix(pools: Map<FieldSkill, List<Question>>, progress: UserProgress, today: Long, random: Random, size: Int = FieldRun.MIX.size): List<Question> {
        val summaries = pools.map { (skill, pool) -> progress.skillSummary(skill, pool, today) }.filter { it.questionCount > 0 }
        val (mastered, open) = summaries.partition { it.mastered }
        if (open.isEmpty()) return keepEdge(pools, progress, today, random, size)
        val keep = minOf(KEEP_SHARP, mastered.size)
        val kept = roundRobin(mastered.map { oldestFirst(pools.getValue(it.skill), progress, random) }, keep)
        val queues = open.sortedBy { it.readiness }.map { ordered(pools.getValue(it.skill), progress, today, random).toMutableList() }
        val picked = mutableListOf<Question>()
        while (picked.size < size - keep && queues.any { it.isNotEmpty() }) {
            queues.forEachIndexed { i, queue ->
                repeat(if (i == 0) 2 else 1) {
                    if (picked.size < size - keep && queue.isNotEmpty()) picked += queue.removeAt(0)
                }
            }
        }
        return (picked + kept).distinctBy { it.id }.shuffled(random)
    }

    /** "Keep your edge" and the season check's order: across all skills, oldest answers first. */
    fun keepEdge(pools: Map<FieldSkill, List<Question>>, progress: UserProgress, today: Long, random: Random, size: Int = FieldRun.MIX.size): List<Question> =
        roundRobin(pools.values.map { oldestFirst(it, progress, random) }, size).shuffled(random)

    /** The season check: across all skills, what isn't right first, then the oldest answers. */
    fun seasonCheck(pools: Map<FieldSkill, List<Question>>, progress: UserProgress, today: Long, random: Random, size: Int = FieldRun.SEASON_CHECK.size): List<Question> =
        roundRobin(pools.values.map { ordered(it, progress, today, random) }, size).shuffled(random)

    /** Today's questions, the same all day: 3 species photos, then 3 general questions from the skills. */
    fun daily(species: List<Question>, general: List<Question>, today: Long): List<Question> {
        val random = Random(today)
        return species.shuffled(random).take(DAILY_SPECIES) + general.distinctBy { it.id }.shuffled(random).take(DAILY_GENERAL)
    }

    private fun ordered(pool: List<Question>, progress: UserProgress, today: Long, random: Random): List<Question> {
        val (fresh, toDo) = pool.partition { progress.isFresh(it.id, today) }
        return toDo.shuffled(random) + oldestFirst(fresh, progress, random)
    }

    private fun oldestFirst(pool: List<Question>, progress: UserProgress, random: Random): List<Question> =
        pool.shuffled(random).sortedBy { progress.correctDay[it.id] ?: Long.MIN_VALUE }

    private fun roundRobin(queues: List<List<Question>>, size: Int): List<Question> {
        val iterators = queues.map { it.iterator() }
        val picked = LinkedHashMap<String, Question>()
        while (picked.size < size && iterators.any { it.hasNext() }) {
            for (it in iterators) if (picked.size < size && it.hasNext()) it.next().let { q -> picked.putIfAbsent(q.id, q) }
        }
        return picked.values.toList()
    }
}

/** What Home says about the season (P11): the app never knows legal dates, only the hunter's own. */
sealed interface SeasonStatus {
    /** The hunter's date is close: Home counts down to it. */
    data class Countdown(val start: Long, val daysLeft: Int) : SeasonStatus

    /** No date for this year, late August to November: "Hunting season is coming", with a link to set the date. */
    data object Coming : SeasonStatus

    companion object {
        /** The countdown shows from 4 weeks before the hunter's date. */
        const val COUNTDOWN_DAYS = 28
        private val COMING_FROM = MonthDay.of(8, 20)
        private val COMING_UNTIL = MonthDay.of(11, 30)

        fun of(seasonStart: Long?, today: Long): SeasonStatus? {
            if (seasonStart != null && seasonStart >= today) {
                val left = (seasonStart - today).toInt()
                return if (left <= COUNTDOWN_DAYS) Countdown(seasonStart, left) else null
            }
            val date = LocalDate.ofEpochDay(today)
            // A date from this year that has passed: the season is on. The date is asked again each year.
            if (seasonStart != null && LocalDate.ofEpochDay(seasonStart).year == date.year) return null
            val monthDay = MonthDay.from(date)
            return if (monthDay >= COMING_FROM && monthDay <= COMING_UNTIL) Coming else null
        }
    }
}
