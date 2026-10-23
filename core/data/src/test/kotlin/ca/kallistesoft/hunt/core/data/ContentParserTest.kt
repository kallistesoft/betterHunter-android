package ca.kallistesoft.hunt.core.data

import ca.kallistesoft.hunt.core.model.ImageCredit
import ca.kallistesoft.hunt.core.model.Lang
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentParserTest {
    @Test
    fun `exam recipes fall back to the defaults`() {
        val exams = ContentParser.parseExams(
            """
            {
              "_comment": "not an exam",
              "PAL": { "name": { "fr": "PPA", "en": "PAL", "de": "ignored" }, "scopes": ["federal"] },
              "BOW": {
                "name": { "en": "Bow" }, "scopes": ["QC"], "questionCount": 20, "minutes": 30, "passMark": 0.75,
                "comingSoon": true
              }
            }
            """,
        )
        assertEquals(listOf("PAL", "BOW"), exams.map { it.id })
        val (pal, bow) = exams
        assertEquals(mapOf(Lang.FR to "PPA", Lang.EN to "PAL"), pal.names)
        assertEquals(50, pal.questionCount)
        assertEquals(60, pal.minutes)
        assertEquals(0.8, pal.passMark, 0.0)
        assertFalse(pal.comingSoon)
        assertEquals(mapOf(Lang.EN to "Bow"), bow.names)
        assertEquals(20, bow.questionCount)
        assertEquals(30, bow.minutes)
        assertEquals(0.75, bow.passMark, 0.0)
        assertTrue(bow.comingSoon)
    }

    @Test
    fun `a question with only the required fields`() {
        val q = ContentParser.parseQuestions(
            """[{ "id": "q1", "scope": "QC", "chapter": "l01", "answerIndex": 2,
                  "fr": { "q": "Quoi ?", "choices": ["a", "b", "c", "d"], "explanation": "Parce que." } }]""",
        ).single()
        assertEquals("q1", q.id)
        assertEquals("QC", q.scope)
        assertEquals("l01", q.chapter)
        assertEquals(2, q.answerIndex)
        assertEquals(setOf(Lang.FR), q.texts.keys)
        val fr = q.texts.getValue(Lang.FR)
        assertEquals("Quoi ?", fr.q)
        assertEquals("Parce que.", fr.explanation)
        assertNull(fr.habitat)
        assertNull(fr.confuse)
        assertEquals("", q.sourceRef)
        assertNull(q.reviewYear)
        assertNull(q.sourceUrl)
        assertNull(q.image)
        assertNull(q.imageCredit)
        assertNull(q.imageAspect)
    }

    @Test
    fun `a photo question with every field`() {
        val q = ContentParser.parseQuestions(
            """[{ "id": "id-1", "scope": "wildlife-id", "chapter": "deer", "answerIndex": 0,
                  "fr": { "q": "Qui ?", "choices": ["Cerf"], "explanation": "E", "habitat": "Forêt", "confuse": "Orignal" },
                  "en": { "q": "Who?", "choices": ["Deer"], "explanation": "E", "habitat": "Forest", "confuse": "Moose" },
                  "sourceRef": "Guide p. 3", "reviewYear": 2026, "sourceUrl": "https://example.org",
                  "image": "assets/wildlife/deer.jpg",
                  "imageCredit": { "author": "A. B.", "license": "CC BY 4.0", "page": "https://commons.example/deer" },
                  "imageSize": [400, 300], "unknownKey": 1 }]""",
        ).single()
        assertEquals("Forest", q.texts.getValue(Lang.EN).habitat)
        assertEquals("Orignal", q.texts.getValue(Lang.FR).confuse)
        assertEquals("Guide p. 3", q.sourceRef)
        assertEquals(2026, q.reviewYear)
        assertEquals("https://example.org", q.sourceUrl)
        assertEquals("wildlife/deer.jpg", q.image)
        assertEquals(ImageCredit("A. B.", "CC BY 4.0", "https://commons.example/deer"), q.imageCredit)
        assertEquals(4f / 3f, q.imageAspect!!, 0.001f)
    }

    @Test
    fun `recipes and questions built in code map like parsed ones`() {
        val exam = ContentParser.ExamDto(mapOf("en" to "PAL"), listOf("federal"), 3, 1, 0.5, comingSoon = false).toModel("PAL")
        assertEquals(listOf("federal"), exam.scopes)
        assertEquals(0.5, exam.passMark, 0.0)

        val q = ContentParser.QuestionDto(
            id = "id-1",
            scope = "wildlife-id",
            chapter = "deer",
            answerIndex = 1,
            en = ContentParser.TextDto("Who?", listOf("Moose", "Deer"), "E", habitat = "Forest"),
            image = "assets/wildlife/deer.jpg",
            imageCredit = ContentParser.CreditDto("A. B.", "CC BY 4.0", "https://commons.example/deer"),
            imageSize = listOf(300.0, 300.0),
        ).toModel()
        assertEquals("Deer", q.texts.getValue(Lang.EN).choices[q.answerIndex])
        assertEquals("Forest", q.texts.getValue(Lang.EN).habitat)
        assertEquals("wildlife/deer.jpg", q.image)
        assertEquals("A. B.", q.imageCredit!!.author)
        assertEquals(1f, q.imageAspect!!, 0f)
    }

    @Test
    fun `a malformed photo size gives no aspect`() {
        val qs = ContentParser.parseQuestions(
            """[{ "id": "a", "scope": "s", "chapter": "c", "answerIndex": 0, "imageSize": [400, 0] },
                { "id": "b", "scope": "s", "chapter": "c", "answerIndex": 0, "imageSize": [400] }]""",
        )
        assertEquals(listOf(null, null), qs.map { it.imageAspect })
        assertTrue(qs.all { it.texts.isEmpty() })
    }

    @Test
    fun `a recipe or question without its required fields is rejected`() {
        val malformed = listOf(
            { ContentParser.parseExams("""{ "PAL": {} }""") },
            { ContentParser.parseExams("""{ "PAL": { "name": { "en": "PAL" } } }""") },
            { ContentParser.parseQuestions("""[{}]""") },
            { ContentParser.parseQuestions("""[{ "id": "q1", "scope": "QC", "chapter": "l01" }]""") },
            { ContentParser.parseQuestions("""[{ "id": "q1", "scope": "QC", "chapter": "l01", "answerIndex": 0, "fr": {} }]""") },
            {
                ContentParser.parseQuestions(
                    """[{ "id": "q1", "scope": "QC", "chapter": "l01", "answerIndex": 0, "fr": { "q": "Q", "choices": [] } }]""",
                )
            },
        )
        for (parse in malformed) assertThrows(SerializationException::class.java) { parse() }
    }

    @Test
    fun `only the banks of available exams are read, and missing banks are skipped`() {
        val read = mutableListOf<String>()
        val content = ContentParser.content(
            """{ "PAL": { "name": { "en": "PAL" }, "scopes": ["federal", "AB"] },
                 "BOW": { "name": { "en": "Bow" }, "scopes": ["bow"], "comingSoon": true } }""",
        ) { scope ->
            read += scope
            if (scope == "federal") """[{ "id": "f1", "scope": "federal", "chapter": "m01", "answerIndex": 0 }]""" else null
        }
        assertEquals(listOf("federal", "AB"), read)
        assertEquals(listOf("f1"), content.questionsFor(content.exam("PAL")!!).map { it.id })
        assertTrue(content.questionsInScope("AB").isEmpty())
    }
}
