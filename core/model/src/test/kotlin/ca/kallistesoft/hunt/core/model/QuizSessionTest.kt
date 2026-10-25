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
        val drawn = QuizSession.start(testExam, testPool, QuizMode.EXAM, Random(2))
        val right = drawn.questions.map { it.answerIndex }
        val wrong = (right[1] + 1) % 4
        val s = drawn.answer(right[0]).next().answer(wrong).next().answer(right[2])
        assertEquals(2, s.correctCount)
        assertTrue(s.passed) // 2/3 >= 0.6
        assertEquals(listOf(s.questions[1] to wrong), s.missed)
        assertEquals(67, s.percent)
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

    @Test
    fun `each run draws a question once, in a random order, with its choices shuffled`() {
        val withCopies = testPool + testPool.take(2)
        val runs = (1..20).map { QuizSession.start(testExam.copy(questionCount = 5), withCopies, QuizMode.EXAM, Random(it)) }
        runs.forEach { run ->
            assertEquals("no question twice", 5, run.questions.map { it.id }.toSet().size)
            run.questions.forEach { q ->
                // The right choice follows the shuffle, the same in both languages.
                assertEquals("Deux", q.text(Lang.FR).choices[q.answerIndex])
                assertEquals("Two", q.text(Lang.EN).choices[q.answerIndex])
            }
        }
        assertTrue("questions in different orders", runs.map { r -> r.questions.map { it.id } }.toSet().size > 1)
        assertTrue("choices in different orders", runs.map { it.questions.first().choiceOrder }.toSet().size > 1)
    }

    @Test
    fun `a choice order is kept, reapplied and undone`() {
        val q = testQuestion("q1")
        val shown = q.withChoiceOrder(listOf(2, 0, 3, 1))
        assertEquals(listOf("Three", "One", "Four", "Two"), shown.text(Lang.EN).choices)
        assertEquals(3, shown.answerIndex)
        assertEquals(listOf(2, 0, 3, 1), shown.choiceOrder)
        assertEquals(q, shown.inBankOrder())
        assertEquals("reordering starts from the bank", shown, shown.withChoiceOrder(listOf(1, 0, 2, 3)).withChoiceOrder(listOf(2, 0, 3, 1)))
        assertEquals("an order that doesn't fit is ignored", q, q.withChoiceOrder(listOf(0, 1)))
        assertEquals(q, q.inBankOrder())
    }
}
