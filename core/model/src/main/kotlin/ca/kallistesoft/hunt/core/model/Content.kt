package ca.kallistesoft.hunt.core.model

/** Exam recipes and the question banks bundled with the app. */
data class Content(
    val exams: List<Exam>,
    private val questionsByScope: Map<String, List<Question>>,
) {
    fun questionsFor(exam: Exam): List<Question> = exam.scopes.flatMap { questionsByScope[it].orEmpty() }

    fun exam(id: String): Exam? = exams.firstOrNull { it.id == id }

    /** Exams with questions, in section order (firearms, hunting, identification), then exams.json order. */
    val availableExams: List<Exam> by lazy {
        exams.filter { !it.comingSoon && questionsFor(it).isNotEmpty() }.sortedBy { it.group.ordinal }
    }

    val trainingExams: List<Exam> get() = availableExams.filter { it.isTraining }
    val games: List<Exam> get() = availableExams.filterNot { it.isTraining }

    val hasSampleContent: Boolean by lazy { questionsByScope.values.any { qs -> qs.any { it.isSample } } }

    companion object {
        val EMPTY = Content(emptyList(), emptyMap())
    }
}
