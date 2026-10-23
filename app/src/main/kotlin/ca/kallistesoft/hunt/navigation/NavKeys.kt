package ca.kallistesoft.hunt.navigation

import androidx.navigation3.runtime.NavKey
import ca.kallistesoft.hunt.core.model.FieldRun
import ca.kallistesoft.hunt.core.model.FieldSkill
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.feature.settings.ProfileField
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

@Serializable
data object Settings : NavKey

/** Settings: change one onboarding answer. */
@Serializable
data class EditProfile(val field: ProfileField) : NavKey

/** New hunters (P11): today's mix and each skill. */
@Serializable
data object FieldSkills : NavKey

/** A new hunter's run: one [skill], today's mix, the season check, today's questions or "My mistakes". */
@Serializable
data class FieldQuiz(val run: FieldRun, val skill: FieldSkill? = null) : NavKey

/** "When does your season start?" */
@Serializable
data object SeasonDate : NavKey
