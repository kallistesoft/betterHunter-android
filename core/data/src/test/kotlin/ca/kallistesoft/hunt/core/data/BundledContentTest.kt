package ca.kallistesoft.hunt.core.data

import ca.kallistesoft.hunt.core.model.Lang
import ca.kallistesoft.hunt.core.model.PHOTO_ID_EXAM
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Checks the content actually bundled in src/main/assets. */
class BundledContentTest {
    private val assets = File("src/main/assets")
    private val content = ContentParser.content(File(assets, "content/exams.json").readText()) { scope ->
        File(assets, "content/questions/$scope.json").takeIf { it.exists() }?.readText()
    }

    @Test
    fun `every available exam has questions in both languages`() {
        assertTrue(content.availableExams.isNotEmpty())
        for (exam in content.availableExams) {
            for (q in content.questionsFor(exam)) {
                for (lang in Lang.entries) {
                    val text = q.texts[lang]
                    assertNotNull("${q.id} has no $lang text", text)
                    assertEquals("${q.id} ($lang) should have 4 choices", 4, text!!.choices.size)
                }
                assertTrue("${q.id} answer index out of range", q.answerIndex in 0..3)
            }
        }
    }

    @Test
    fun `photo questions point at bundled photos with credits`() {
        val idExam = content.exam(PHOTO_ID_EXAM)!!
        val photos = content.questionsFor(idExam)
        assertTrue(photos.isNotEmpty())
        for (q in photos) {
            val image = q.image!!
            assertFalse(image.startsWith("assets/"))
            assertTrue("missing $image", File(assets, image).exists())
            assertNotNull(q.imageCredit)
            assertNotNull(q.imageAspect)
        }
    }

    @Test
    fun `exams are ordered firearms, hunting, identification`() {
        val groups = content.availableExams.map { it.group.ordinal }
        assertEquals(groups.sorted(), groups)
    }

    @Test
    fun `exam names and questions never name a provincial course or its publisher`() {
        for (exam in content.exams) {
            for (lang in Lang.entries) assertNoCourseName("exam ${exam.id}", exam.name(lang) + " " + exam.shortName(lang))
        }
        for (exam in content.availableExams) {
            for (q in content.questionsFor(exam)) {
                for (text in q.texts.values) assertNoCourseName(q.id, (listOf(text.q, text.explanation) + text.choices).joinToString(" "))
            }
        }
    }

    @Test
    fun `comment keys are not exams`() {
        assertTrue(content.exams.none { it.id.startsWith("_") })
    }
}

/** Names the app must never show (DECISIONS.md, P13); source citations are allowed to name their manual. */
internal val COURSE_NAMES = listOf("ICAF", "ICAA", "ABHE", "AHEIA", "FédéCP", "FedeCP", "Fédération québécoise", "Conservation and Hunter Education")

internal fun assertNoCourseName(where: String, text: String) {
    COURSE_NAMES.forEach { assertFalse("$where names \"$it\": $text", text.contains(it, ignoreCase = true)) }
}
