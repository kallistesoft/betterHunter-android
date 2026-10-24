package ca.kallistesoft.hunt.core.model

data class Exam(
    val id: String,
    val names: Map<Lang, String>,
    val scopes: List<String>,
    val questionCount: Int = 50,
    val minutes: Int = 60,
    val passMark: Double = 0.8,
    val comingSoon: Boolean = false,
    /** A few words for overlines and "… passed" ("PAL", "Hunting exam"); never a provincial course's name (P13). */
    val shortNames: Map<Lang, String> = emptyMap(),
) {
    fun name(lang: Lang): String = names[lang] ?: names[Lang.FR] ?: id

    fun shortName(lang: Lang): String = shortNames[lang] ?: shortNames[Lang.FR] ?: name(lang)

    val group: ExamGroup get() = ExamStyle.of(id).group
    val region: String get() = ExamStyle.of(id).region

    /** "Hunting exam · Québec", "PAL · Canada": the short name and the region, above a quiz or its results. */
    fun label(lang: Lang): String = listOf(shortName(lang), region).filter { it.isNotEmpty() }.joinToString(" · ")

    /** Real exams (licence, hunter education), as opposed to recognition games. */
    val isTraining: Boolean get() = group != ExamGroup.IDENTIFICATION
}

/** Sections exams are grouped in, in display order: the hunter course makes the hunter, the PAL is optional. */
enum class ExamGroup { HUNTING, FIREARMS, IDENTIFICATION }

/** Where an exam belongs and which region it covers; the UI adds the icon. */
data class ExamStyle(val region: String, val group: ExamGroup) {
    companion object {
        private val styles = mapOf(
            "PAL" to ExamStyle("Canada", ExamGroup.FIREARMS),
            "ICAF" to ExamStyle("Québec", ExamGroup.HUNTING),
            "ABHE" to ExamStyle("Alberta", ExamGroup.HUNTING),
            "ID" to ExamStyle("Canada", ExamGroup.IDENTIFICATION),
        )
        private val fallback = ExamStyle("", ExamGroup.FIREARMS)

        fun of(examId: String): ExamStyle = styles[examId] ?: fallback
    }
}

/** The photo ID game ("What is it?"), which also supplies the photo of the day. */
const val PHOTO_ID_EXAM = "ID"
