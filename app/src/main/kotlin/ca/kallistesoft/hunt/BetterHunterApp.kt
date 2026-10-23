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
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.feature.about.AboutScreen
import ca.kallistesoft.hunt.feature.exams.ExamTrainingScreen
import ca.kallistesoft.hunt.feature.exams.PassedExamScreen
import ca.kallistesoft.hunt.feature.exams.ProvincesScreen
import ca.kallistesoft.hunt.feature.exams.RecognitionScreen
import ca.kallistesoft.hunt.feature.home.HomeScreen
import ca.kallistesoft.hunt.feature.onboarding.OnboardingScreen
import ca.kallistesoft.hunt.feature.quiz.QuizScreen
import ca.kallistesoft.hunt.navigation.About
import ca.kallistesoft.hunt.navigation.ExamTraining
import ca.kallistesoft.hunt.navigation.Home
import ca.kallistesoft.hunt.navigation.PassedExam
import ca.kallistesoft.hunt.navigation.Provinces
import ca.kallistesoft.hunt.navigation.Quiz
import ca.kallistesoft.hunt.navigation.Recognition

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
                        onOpenProvinces = { backStack.add(Provinces) },
                        onStartQuiz = startQuiz,
                        onSetLanguage = ::setAppLanguage,
                    )
                }
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
            entry<About> {
                AboutScreen(versionName = versionName, onBack = back)
            }
        },
    )
}

/** The FR/EN toggle: a per-app language, kept by AppCompat (or Android 13+) and shown in system settings. */
private fun setAppLanguage(lang: Lang) {
    val tag = if (lang == Lang.FR) "fr-CA" else "en-CA"
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
}
