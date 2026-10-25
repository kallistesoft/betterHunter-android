package ca.kallistesoft.hunt.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProgressTest {
    private val today = 20_000L

    @Test
    fun `a missed question stays weak until answered correctly twice`() {
        val q = testPool.first()
        var p = UserProgress().record(q.id, correct = false, today)
        assertEquals(setOf(q.id), p.weakIds(testPool))
        p = p.record(q.id, correct = true, today)
        assertEquals(setOf(q.id), p.weakIds(testPool))
        p = p.record(q.id, correct = true, today)
        assertTrue(p.weakIds(testPool).isEmpty())
    }

    @Test
    fun `readiness is the share of questions last answered correctly`() {
        val p = UserProgress()
            .record(testPool[0].id, correct = true, today)
            .record(testPool[1].id, correct = false, today)
        assertEquals(1f / 5, p.readiness(testPool), 1e-6f)
        assertEquals(0f, p.readiness(emptyList()), 0f)
    }

    @Test
    fun `streak counts consecutive days`() {
        val p = UserProgress()
            .record("q1", correct = true, today - 1)
            .record("q1", correct = true, today)
            .record("q2", correct = true, today) // same day: unchanged
        assertEquals(2, p.currentStreak(today))
        assertEquals(2, p.currentStreak(today + 1)) // still alive until a day is missed
    }

    @Test
    fun `a missed day resets the streak`() {
        val p = UserProgress().record("q1", correct = true, today - 3)
        assertEquals(0, p.currentStreak(today))
        assertEquals(1, p.record("q1", correct = true, today).currentStreak(today))
    }

    @Test
    fun `daily count resets on a new day`() {
        val p = UserProgress().record("q1", true, today - 1).record("q2", true, today - 1)
        assertEquals(2, p.answeredToday(today - 1))
        assertEquals(0, p.answeredToday(today))
        assertEquals(1, p.record("q3", false, today).answeredToday(today))
    }

    @Test
    fun `photo of the day choice only counts for its day`() {
        val p = UserProgress().withPhotoOfDayChoice(2, today)
        assertEquals(2, p.photoOfDayChoice(today))
        assertNull(p.photoOfDayChoice(today + 1))
    }

    @Test
    fun `photo of the day is stable for a day and rotates`() {
        assertEquals(photoOfDay(testPool, today), photoOfDay(testPool, today))
        assertEquals(testPool[(today % 5).toInt()], photoOfDay(testPool, today))
        assertNull(photoOfDay(emptyList(), today))
    }

    @Test
    fun `mock exams keep their best and last score, and reset all clears one exam's`() {
        val p = UserProgress()
            .withMockResult("ICAF", 1, 72)
            .withMockResult("ICAF", 1, 88)
            .withMockResult("ICAF", 1, 76)
            .withMockResult("ICAF", 2, 60)
            .withMockResult("PAL", 1, 90)
        assertEquals(MockResult(best = 88, last = 76), p.mockResults("ICAF")[1])
        assertEquals(setOf(1, 2), p.mockResults("ICAF").keys)
        assertTrue(p.mockResults("ICAF").getValue(1).passed(testExam.copy(passMark = 0.8)))
        assertFalse(p.mockResults("ICAF").getValue(2).passed(testExam.copy(passMark = 0.8)))

        val reset = p.withMocksReset("ICAF")
        assertTrue(reset.mockResults("ICAF").isEmpty())
        assertEquals("other exams keep theirs", p.mockResults("PAL"), reset.mockResults("PAL"))
    }

    @Test
    fun `a week pass lasts 7 days, adds up when bought again, and counts each purchase once`() {
        val now = 1_000_000L
        val week = UserProgress.WEEK_PASS_MILLIS
        assertFalse(UserProgress().isAdFree(now))

        val one = UserProgress().withWeekPass("a", now)
        assertEquals(now + week, one.adFreeUntil)
        assertTrue(one.isAdFree(now + week - 1))
        assertFalse("over after 7 days", one.isAdFree(now + week))
        assertEquals("the same purchase twice counts once", one, one.withWeekPass("a", now + 10))

        val two = one.withWeekPass("b", now + 1_000)
        assertEquals("7 more days after the running pass", now + 2 * week, two.adFreeUntil)
        val later = two.withWeekPass("c", now + 3 * week)
        assertEquals("after a pass ended: 7 days from now", now + 4 * week, later.adFreeUntil)
    }

    @Test
    fun `forever follows what Play says, a refund takes it back`() {
        val owned = UserProgress().withAdFreeForever(true)
        assertTrue(owned.isAdFree(Long.MAX_VALUE))
        assertEquals(owned, owned.withAdFreeForever(true))
        assertFalse(owned.withAdFreeForever(false).isAdFree(0))
    }
}
