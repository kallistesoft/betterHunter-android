package ca.kallistesoft.hunt.core.model

data class Exam(
    val id: String,
    val names: Map<Lang, String>,
    val scopes: List<String>,
    val questionCount: Int = 50,
    val minutes: Int = 60,
    val passMark: Double = 0.8,
    val comingSoon: Boolean = false,
) {
    fun name(lang: Lang): String = names[lang] ?: names[Lang.FR] ?: id

    val group: ExamGroup get() = ExamStyle.of(id).group
    val region: String get() = ExamStyle.of(id).region

    /** Real exams (licence, hunter education), as opposed to recognition games. */
    val isTraining: Boolean get() = group != ExamGroup.IDENTIFICATION
}

/** Sections exams are grouped in (picker, dashboard), in display order. */
enum class ExamGroup { FIREARMS, HUNTING, IDENTIFICATION }

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
