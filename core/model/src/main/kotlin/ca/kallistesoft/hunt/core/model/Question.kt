package ca.kallistesoft.hunt.core.model

data class QuestionText(
    val q: String,
    val choices: List<String>,
    val explanation: String,
    /** Photo ID questions: where the animal lives, and a protected look-alike warning. */
    val habitat: String? = null,
    val confuse: String? = null,
)

/** Credit line required by the photo's free licence (Wikimedia Commons). */
data class ImageCredit(val author: String, val license: String, val page: String)

data class Question(
    val id: String,
    val scope: String,
    val chapter: String,
    val texts: Map<Lang, QuestionText>,
    val answerIndex: Int,
    val sourceRef: String = "",
    val reviewYear: Int? = null,
    /** Official page confirming the current rule (law, quebec.ca, rcmp.ca), if checked. */
    val sourceUrl: String? = null,
    /** Asset path of the photo for "What is it?" questions (e.g. `wildlife/abc.jpg`), with its licence credit. */
    val image: String? = null,
    val imageCredit: ImageCredit? = null,
    /** Width / height of the photo, so its space is reserved before it loads. */
    val imageAspect: Float? = null,
) {
    /** Sample questions are placeholders until reviewed questions are bundled. */
    val isSample: Boolean get() = id.startsWith("sample-")

    fun text(lang: Lang): QuestionText = texts[lang] ?: texts.getValue(Lang.FR)
}
