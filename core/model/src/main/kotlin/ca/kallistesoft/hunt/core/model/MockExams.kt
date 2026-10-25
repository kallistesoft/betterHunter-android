package ca.kallistesoft.hunt.core.model

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

/** Each exam has this many mock exams (Mock 1 to 10, P14). */
const val MOCK_COUNT = 10

/** A finished mock exam's scores, in percent: the best one decides passed or failed. */
@Serializable
data class MockResult(val best: Int, val last: Int) {
    fun passed(exam: Exam): Boolean = best >= (exam.passMark * 100).roundToInt()
}

/** Percent of right answers on a finished run, as mock results keep it. */
val QuizSession.percent: Int get() = (score * 100).roundToInt()
