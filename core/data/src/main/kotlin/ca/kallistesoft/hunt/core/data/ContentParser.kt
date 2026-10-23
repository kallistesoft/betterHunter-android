package ca.kallistesoft.hunt.core.data

import ca.kallistesoft.hunt.core.model.Content
import ca.kallistesoft.hunt.core.model.Exam
import ca.kallistesoft.hunt.core.model.ImageCredit
import ca.kallistesoft.hunt.core.model.Lang
import ca.kallistesoft.hunt.core.model.Question
import ca.kallistesoft.hunt.core.model.QuestionText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

/** Reads the bundled JSON (`assets/content/`), independent of Android for tests. */
object ContentParser {
    private val json = Json { ignoreUnknownKeys = true }

    /** Exam recipes; keys starting with `_` are comments. */
    fun parseExams(text: String): List<Exam> =
        json.parseToJsonElement(text).jsonObject
            .filterKeys { !it.startsWith("_") }
            .map { (id, value) -> json.decodeFromJsonElement<ExamDto>(value).toModel(id) }

    fun parseQuestions(text: String): List<Question> =
        json.decodeFromString<List<QuestionDto>>(text).map { it.toModel() }

    /**
     * Builds the content from the exam recipes and a reader for `questions/<scope>.json`
     * (null when a scope has no bank yet: its exams show as unavailable).
     */
    fun content(examsText: String, readScope: (String) -> String?): Content {
        val exams = parseExams(examsText)
        val scopes = exams.filterNot { it.comingSoon }.flatMap { it.scopes }.toSet()
        val byScope = scopes.mapNotNull { scope -> readScope(scope)?.let { scope to parseQuestions(it) } }.toMap()
        return Content(exams, byScope)
    }

    // The JSON shapes; internal so tests can also build them in code.

    @Serializable
    internal class ExamDto(
        val name: Map<String, String>,
        val scopes: List<String>,
        val questionCount: Int = 50,
        val minutes: Int = 60,
        val passMark: Double = 0.8,
        val comingSoon: Boolean = false,
    ) {
        fun toModel(id: String) = Exam(
            id = id,
            names = name.mapKeysToLang(),
            scopes = scopes,
            questionCount = questionCount,
            minutes = minutes,
            passMark = passMark,
            comingSoon = comingSoon,
        )
    }

    @Serializable
    internal class TextDto(
        val q: String,
        val choices: List<String>,
        val explanation: String,
        val habitat: String? = null,
        val confuse: String? = null,
    ) {
        fun toModel() = QuestionText(q, choices, explanation, habitat, confuse)
    }

    @Serializable
    internal class CreditDto(val author: String, val license: String, val page: String)

    @Serializable
    internal class QuestionDto(
        val id: String,
        val scope: String,
        val chapter: String,
        val answerIndex: Int,
        val fr: TextDto? = null,
        val en: TextDto? = null,
        val sourceRef: String? = null,
        val reviewYear: Int? = null,
        val sourceUrl: String? = null,
        val image: String? = null,
        val imageCredit: CreditDto? = null,
        val imageSize: List<Double>? = null,
    ) {
        fun toModel() = Question(
            id = id,
            scope = scope,
            chapter = chapter,
            texts = buildMap {
                fr?.let { put(Lang.FR, it.toModel()) }
                en?.let { put(Lang.EN, it.toModel()) }
            },
            answerIndex = answerIndex,
            sourceRef = sourceRef.orEmpty(),
            reviewYear = reviewYear,
            sourceUrl = sourceUrl,
            // The banks use Flutter asset paths ("assets/wildlife/x.jpg"); Android assets drop the prefix.
            image = image?.removePrefix("assets/"),
            imageCredit = imageCredit?.let { ImageCredit(it.author, it.license, it.page) },
            imageAspect = imageSize?.takeIf { it.size == 2 && it[1] > 0 }?.let { (it[0] / it[1]).toFloat() },
        )
    }

    private fun Map<String, String>.mapKeysToLang(): Map<Lang, String> =
        Lang.entries.mapNotNull { lang -> this[lang.code]?.let { lang to it } }.toMap()
}
