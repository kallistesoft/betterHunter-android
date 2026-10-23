package ca.kallistesoft.hunt.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentTest {
    @Test
    fun `nothing is available before the content loads`() {
        val empty = Content.EMPTY
        assertTrue(empty.exams.isEmpty())
        assertTrue(empty.availableExams.isEmpty())
        assertTrue(empty.questionsInScope("federal").isEmpty())
        assertNull(empty.exam("PAL"))
        assertFalse(empty.hasSampleContent)
    }

    @Test
    fun `an exam's questions come from its scopes`() {
        val content = Content(listOf(testExam), mapOf("federal" to testPool, "QC" to listOf(testQuestion("qc"))))
        assertEquals(testPool, content.questionsFor(testExam))
        assertEquals(listOf(testExam), content.availableExams)
        assertEquals(testExam, content.exam("PAL"))
    }
}
