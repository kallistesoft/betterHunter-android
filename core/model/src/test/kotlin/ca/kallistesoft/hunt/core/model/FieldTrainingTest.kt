package ca.kallistesoft.hunt.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random

class FieldTrainingTest {
    private val today = 20_000L
    private val random = Random(1)

    private fun pool(prefix: String, n: Int) = (1..n).map { testQuestion("$prefix$it") }

    private fun UserProgress.allRight(pool: List<Question>, day: Long = today) =
        pool.fold(this) { p, q -> p.record(q.id, correct = true, day) }

    @Test
    fun `right answers expire after 3 months and a mastered skill fades`() {
        val shots = pool("s", 4)
        val p = UserProgress().allRight(shots, today - 100).record("s1", correct = true, today)
        val summary = p.skillSummary(FieldSkill.SHOT_PLACEMENT, shots, today)
        assertEquals(0.25f, summary.readiness, 1e-6f)
        assertEquals(3, summary.expired)
        assertFalse(summary.mastered)
        assertTrue(summary.faded)
        assertTrue(UserProgress().allRight(shots).skillSummary(FieldSkill.SHOT_PLACEMENT, shots, today).mastered)
    }

    @Test
    fun `a miss takes a skill below mastered`() {
        val shots = pool("s", 3)
        val p = UserProgress().allRight(shots).record("s2", correct = false, today)
        val summary = p.skillSummary(FieldSkill.SHOT_PLACEMENT, shots, today)
        assertFalse(summary.mastered)
        assertFalse(summary.faded)
    }

    @Test
    fun `the mix comes mostly from the weakest skill and keeps two from mastered ones`() {
        val pools = mapOf(
            FieldSkill.SHOT_PLACEMENT to pool("s", 10),
            FieldSkill.AFTER_SHOT to pool("a", 10),
            FieldSkill.SAFE_HANDLING to pool("h", 5),
            FieldSkill.RULES to pool("r", 5),
        )
        val p = UserProgress()
            .allRight(pools.getValue(FieldSkill.SAFE_HANDLING))
            .allRight(pools.getValue(FieldSkill.RULES))
            .allRight(pools.getValue(FieldSkill.AFTER_SHOT).take(5))
        val mix = FieldTraining.mix(pools, p, today, random)
        assertEquals(10, mix.size)
        val ids = mix.map { it.id }
        assertEquals(2, ids.count { it.startsWith("h") || it.startsWith("r") })
        assertTrue(ids.count { it.startsWith("s") } > ids.count { it.startsWith("a") })
        assertTrue(mix.filter { it.id.startsWith("s") || it.id.startsWith("a") }.none { p.isFresh(it.id, today) })
    }

    @Test
    fun `with every skill mastered the mix keeps the oldest answers sharp`() {
        val pools = mapOf(FieldSkill.SHOT_PLACEMENT to pool("s", 8), FieldSkill.RULES to pool("r", 8))
        val p = UserProgress()
            .allRight(pools.getValue(FieldSkill.SHOT_PLACEMENT), today - 10)
            .allRight(pools.getValue(FieldSkill.RULES))
            .allRight(pools.getValue(FieldSkill.SHOT_PLACEMENT).take(3))
        val mix = FieldTraining.mix(pools, p, today, random, size = 4)
        assertEquals(2, mix.count { it.id.startsWith("s") })
        assertTrue(mix.filter { it.id.startsWith("s") }.all { p.correctDay[it.id] == today - 10 })
    }

    @Test
    fun `a skill run asks what isn't right first`() {
        val shots = pool("s", 12)
        val p = UserProgress().allRight(shots.take(6))
        val run = FieldTraining.skill(shots, p, today, random)
        assertEquals(10, run.size)
        assertEquals(shots.drop(6).map { it.id }.toSet(), run.take(6).map { it.id }.toSet())
    }

    @Test
    fun `the season check covers every skill`() {
        val pools = FieldSkill.entries.associateWith { pool(it.name, 5) }
        val check = FieldTraining.seasonCheck(pools, UserProgress(), today, random)
        assertEquals(15, check.size)
        FieldSkill.entries.forEach { skill -> assertEquals(3, check.count { it.id.startsWith(skill.name) }) }
    }

    @Test
    fun `today's questions are the same all day and change the next day`() {
        val species = pool("p", 20)
        val general = pool("g", 20)
        val first = FieldTraining.daily(species, general, today)
        assertEquals(first, FieldTraining.daily(species, general, today))
        assertEquals(listOf(true, true, true, false, false, false), first.map { it.id.startsWith("p") })
        assertTrue(first != FieldTraining.daily(species, general, today + 1))
    }

    @Test
    fun `daily answers count for today only`() {
        val p = UserProgress().recordDaily("p1", correct = true, today).recordDaily("g1", correct = false, today)
        assertEquals(mapOf("p1" to true, "g1" to false), p.dailyResults(today))
        assertTrue(p.dailyResults(today + 1).isEmpty())
        assertEquals(1, p.recordDaily("p2", correct = true, today + 1).dailyResults(today + 1).size)
        assertEquals(2, p.answeredToday(today))
    }

    @Test
    fun `play time is a moving average`() {
        assertEquals(19 * 60, UserProgress().withPlayTime(19 * 60).playMinute)
        assertEquals(19 * 60 - 12, UserProgress().withPlayTime(19 * 60).withPlayTime(18 * 60).playMinute)
    }

    @Test
    fun `safe firearm handling is only for firearm hunters`() {
        assertFalse(FieldSkill.SAFE_HANDLING in FieldSkill.forWeapons(setOf(Weapon.BOW)))
        assertTrue(FieldSkill.SAFE_HANDLING in FieldSkill.forWeapons(setOf(Weapon.BOW, Weapon.FIREARM)))
    }

    private fun day(y: Int, m: Int, d: Int) = LocalDate.of(y, m, d).toEpochDay()

    @Test
    fun `the season counts down to the hunter's date`() {
        val start = day(2026, 11, 14)
        assertEquals(SeasonStatus.Countdown(start, 21), SeasonStatus.of(start, day(2026, 10, 24)))
        assertEquals(SeasonStatus.Countdown(start, 0), SeasonStatus.of(start, start))
        assertNull(SeasonStatus.of(start, day(2026, 9, 1))) // far away
        assertNull(SeasonStatus.of(start, day(2026, 11, 20))) // the season is on
    }

    @Test
    fun `without a date for this year the season is coming from late August`() {
        assertNull(SeasonStatus.of(null, day(2026, 8, 1)))
        assertEquals(SeasonStatus.Coming, SeasonStatus.of(null, day(2026, 8, 25)))
        assertEquals(SeasonStatus.Coming, SeasonStatus.of(day(2025, 11, 14), day(2026, 9, 2)))
        assertNull(SeasonStatus.of(null, day(2026, 12, 15)))
    }
}
