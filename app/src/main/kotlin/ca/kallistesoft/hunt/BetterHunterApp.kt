package ca.kallistesoft.hunt

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ca.kallistesoft.hunt.core.model.Lang
import ca.kallistesoft.hunt.core.model.FieldRun
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.feature.about.AboutScreen
import ca.kallistesoft.hunt.feature.exams.ExamTrainingScreen
import ca.kallistesoft.hunt.feature.exams.FieldSkillsScreen
import ca.kallistesoft.hunt.feature.exams.PassedExamScreen
import ca.kallistesoft.hunt.feature.exams.ProvincesScreen
import ca.kallistesoft.hunt.feature.exams.RecognitionScreen
import ca.kallistesoft.hunt.feature.home.HomeScreen
import ca.kallistesoft.hunt.feature.onboarding.OnboardingScreen
import ca.kallistesoft.hunt.feature.quiz.FieldQuizScreen
import ca.kallistesoft.hunt.feature.quiz.QuizScreen
import ca.kallistesoft.hunt.feature.settings.ProfileEditScreen
import ca.kallistesoft.hunt.feature.settings.SeasonDateScreen
import ca.kallistesoft.hunt.feature.settings.SettingsScreen
import ca.kallistesoft.hunt.navigation.About
import ca.kallistesoft.hunt.navigation.EditProfile
import ca.kallistesoft.hunt.navigation.ExamTraining
import ca.kallistesoft.hunt.navigation.FieldQuiz
import ca.kallistesoft.hunt.navigation.FieldSkills
import ca.kallistesoft.hunt.navigation.Home
import ca.kallistesoft.hunt.navigation.PassedExam
import ca.kallistesoft.hunt.navigation.Provinces
import ca.kallistesoft.hunt.navigation.Quiz
import ca.kallistesoft.hunt.navigation.Recognition
import ca.kallistesoft.hunt.navigation.SeasonDate
import ca.kallistesoft.hunt.navigation.Settings

/** App navigation (Navigation 3). Each entry gets its own saved state and ViewModels. */
@Composable
fun BetterHunterApp(firstRun: Boolean, versionName: String) {
    val backStack = rememberNavBackStack(Home)
    val back: () -> Unit = { backStack.removeLastOrNull() }
    val startQuiz: (String, QuizMode) -> Unit = { examId, mode -> backStack.add(Quiz(examId, mode)) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Home> {
                if (firstRun) {
                    // First launch: saving the last answer ends onboarding and turns this entry into the dashboard.
                    OnboardingScreen(onSetLanguage = ::setAppLanguage)
                } else {
                    HomeScreen(
                        onOpenExams = { backStack.add(ExamTraining) },
                        onOpenRecognition = { backStack.add(Recognition) },
                        onOpenAbout = { backStack.add(About) },
                        onOpenSettings = { backStack.add(Settings) },
                        onOpenProvinces = { backStack.add(Provinces) },
                        onStartQuiz = startQuiz,
                        onSetLanguage = ::setAppLanguage,
                        onOpenFieldSkills = { backStack.add(FieldSkills) },
                        onStartField = { run -> backStack.add(FieldQuiz(run)) },
                        onOpenSeasonDate = { backStack.add(SeasonDate) },
                    )
                }
            }
            entry<FieldSkills> {
                FieldSkillsScreen(onBack = back, onStart = { run, skill -> backStack.add(FieldQuiz(run, skill)) })
            }
            entry<FieldQuiz> { key ->
                FieldQuizScreen(
                    run = key.run,
                    skill = key.skill,
                    onClose = back,
                    // "Next: After the shot": the next skill's run replaces this one.
                    onOpenSkill = { skill ->
                        backStack.removeLastOrNull()
                        backStack.add(FieldQuiz(FieldRun.SKILL, skill))
                    },
                )
            }
            entry<SeasonDate> {
                SeasonDateScreen(onDone = back)
            }
            entry<ExamTraining> {
                ExamTrainingScreen(
                    onBack = back,
                    onOpenProvinces = { backStack.add(Provinces) },
                    onStartQuiz = startQuiz,
                    onPassedRealExam = { examId -> backStack.add(PassedExam(examId)) },
                )
            }
            entry<Provinces> {
                ProvincesScreen(onBack = back)
            }
            entry<PassedExam> { key ->
                PassedExamScreen(
                    examId = key.examId,
                    // The user is now a new hunter: straight back to Home, in its new-hunter version.
                    onConfirmed = { backStack.retainAll { it == Home } },
                    onBack = back,
                )
            }
            entry<Recognition> {
                RecognitionScreen(onBack = back, onStartQuiz = startQuiz)
            }
            entry<Quiz> { key ->
                QuizScreen(
                    examId = key.examId,
                    mode = key.mode,
                    onClose = back,
                    // Nothing left to review: practise the same exam instead, in place of the review.
                    onStartPractice = {
                        backStack.removeLastOrNull()
                        backStack.add(Quiz(key.examId, QuizMode.PRACTICE))
                    },
                )
            }
            entry<Settings> {
                SettingsScreen(
                    versionName = versionName,
                    onBack = back,
                    onEdit = { field -> backStack.add(EditProfile(field)) },
                    onOpenProvinces = { backStack.add(Provinces) },
                    onOpenAbout = { backStack.add(About) },
                    onSetLanguage = ::setAppLanguage,
                    onOpenSeasonDate = { backStack.add(SeasonDate) },
                )
            }
            entry<EditProfile> { key ->
                ProfileEditScreen(field = key.field, onDone = back)
            }
            entry<About> {
                AboutScreen(onBack = back)
            }
        },
    )
}

/** The FR/EN toggle: a per-app language, kept by AppCompat (or Android 13+) and shown in system settings. */
private fun setAppLanguage(lang: Lang) {
    val tag = if (lang == Lang.FR) "fr-CA" else "en-CA"
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
}
