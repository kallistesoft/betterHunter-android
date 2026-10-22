package ca.kallistesoft.hunt.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuizSessionTest {
    @Test
    fun `exam mode takes questionCount questions from the pool`() {
        val s = QuizSession.start(testExam, testPool, QuizMode.EXAM, Random(1))
        assertEquals(3, s.questions.size)
        assertEquals(3, s.questions.map { it.id }.toSet().size)
    }

    @Test
    fun `practice mode is capped by the pool size`() {
        assertEquals(5, QuizSession.start(testExam, testPool, QuizMode.PRACTICE).questions.size)
    }

    @Test
    fun `review mode only uses the given ids`() {
        val s = QuizSession.start(testExam, testPool, QuizMode.REVIEW, onlyIds = setOf("q2", "q4"))
        assertEquals(setOf("q2", "q4"), s.questions.map { it.id }.toSet())
        assertTrue(QuizSession.start(testExam, testPool, QuizMode.REVIEW, onlyIds = emptySet()).isEmpty)
    }

    @Test
    fun `scoring, pass mark and missed questions`() {
        val s = QuizSession.start(testExam, testPool, QuizMode.EXAM, Random(2))
            .answer(1).next()
            .answer(0).next()
            .answer(1)
        assertEquals(2, s.correctCount)
        assertTrue(s.passed) // 2/3 >= 0.6
        assertEquals(listOf(s.questions[1] to 0), s.missed)
    }

    @Test
    fun `practice answers are final, exam answers can change`() {
        val practice = QuizSession.start(testExam, testPool, QuizMode.PRACTICE).answer(0).answer(1)
        assertEquals(0, practice.currentAnswer)

        val mock = QuizSession.start(testExam, testPool, QuizMode.EXAM).answer(0).answer(1)
        assertEquals(1, mock.currentAnswer)
    }

    @Test
    fun `unanswered questions count as missed`() {
        val s = QuizSession.start(testExam, testPool, QuizMode.EXAM)
        assertEquals(3, s.missed.size)
        assertFalse(s.passed)
    }

    @Test
    fun `next stops at the last question`() {
        val s = QuizSession.start(testExam, testPool, QuizMode.EXAM).next().next().next()
        assertEquals(2, s.current)
        assertTrue(s.isLast)
    }
}
