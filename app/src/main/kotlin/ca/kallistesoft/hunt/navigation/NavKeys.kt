package ca.kallistesoft.hunt.navigation

import androidx.navigation3.runtime.NavKey
import ca.kallistesoft.hunt.core.model.QuizMode
import kotlinx.serialization.Serializable

// Destinations. Serializable so the back stack survives configuration changes and process death.

@Serializable
data object Home : NavKey

@Serializable
data object ExamTraining : NavKey

/** My provinces: which provinces' quizzes are shown. */
@Serializable
data object Provinces : NavKey

/** "Did you pass your hunter course?" for [examId]. */
@Serializable
data class PassedExam(val examId: String) : NavKey

@Serializable
data object Recognition : NavKey

@Serializable
data object About : NavKey

@Serializable
data class Quiz(val examId: String, val mode: QuizMode) : NavKey
