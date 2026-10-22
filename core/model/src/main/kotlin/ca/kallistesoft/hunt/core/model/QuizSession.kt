package ca.kallistesoft.hunt.core.model

import kotlin.random.Random

enum class QuizMode { PRACTICE, EXAM, REVIEW }

/** One run through a set of questions. Immutable: every change returns a new session. */
data class QuizSession(
    val exam: Exam,
    val mode: QuizMode,
    val questions: List<Question>,
    /** Choice picked per question index. */
    val answers: Map<Int, Int> = emptyMap(),
    val current: Int = 0,
) {
    val isEmpty: Boolean get() = questions.isEmpty()
    val question: Question get() = questions[current]
    val isLast: Boolean get() = current == questions.lastIndex
    val currentAnswer: Int? get() = answers[current]
    val currentAnswered: Boolean get() = current in answers

    /**
     * Records [choice] for the current question. In practice/review the first answer is final;
     * in exam mode it can be changed until the end.
     */
    fun answer(choice: Int): QuizSession =
        if (mode != QuizMode.EXAM && currentAnswered) this else copy(answers = answers + (current to choice))

    fun next(): QuizSession = if (isLast) this else copy(current = current + 1)

    val correctCount: Int get() = questions.indices.count { answers[it] == questions[it].answerIndex }
    val score: Double get() = if (questions.isEmpty()) 0.0 else correctCount.toDouble() / questions.size
    val passed: Boolean get() = score >= exam.passMark

    /** Missed questions with the choice the user picked (null if left unanswered). */
    val missed: List<Pair<Question, Int?>>
        get() = questions.indices.filter { answers[it] != questions[it].answerIndex }.map { questions[it] to answers[it] }

    companion object {
        const val PRACTICE_SIZE = 20

        /** [onlyIds] restricts the pool (weak-spot review). */
        fun start(
            exam: Exam,
            pool: List<Question>,
            mode: QuizMode,
            random: Random = Random.Default,
            onlyIds: Set<String>? = null,
        ): QuizSession {
            val candidates = pool.filter { onlyIds == null || it.id in onlyIds }.shuffled(random)
            val size = if (mode == QuizMode.EXAM) exam.questionCount else PRACTICE_SIZE
            return QuizSession(exam, mode, candidates.take(size))
        }
    }
}
