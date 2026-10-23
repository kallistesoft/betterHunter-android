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
import ca.kallistesoft.hunt.feature.about.AboutScreen
import ca.kallistesoft.hunt.feature.exams.ExamPickerScreen
import ca.kallistesoft.hunt.feature.exams.ExamTrainingScreen
import ca.kallistesoft.hunt.feature.exams.RecognitionScreen
import ca.kallistesoft.hunt.feature.home.HomeScreen
import ca.kallistesoft.hunt.feature.quiz.QuizScreen
import ca.kallistesoft.hunt.navigation.About
import ca.kallistesoft.hunt.navigation.ExamPicker
import ca.kallistesoft.hunt.navigation.ExamTraining
import ca.kallistesoft.hunt.navigation.Home
import ca.kallistesoft.hunt.navigation.Quiz
import ca.kallistesoft.hunt.navigation.Recognition

/** App navigation (Navigation 3). Each entry gets its own saved state and ViewModels. */
@Composable
fun BetterHunterApp(firstRun: Boolean, versionName: String) {
    val backStack = rememberNavBackStack(Home)
    val back: () -> Unit = { backStack.removeLastOrNull() }

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
                    // First launch: choose exams; saving them turns this entry into the dashboard.
                    ExamPickerScreen(firstRun = true, onDone = {}, onBack = {})
                } else {
                    HomeScreen(
                        onOpenExams = { backStack.add(ExamTraining) },
                        onOpenRecognition = { backStack.add(Recognition) },
                        onOpenAbout = { backStack.add(About) },
                        onChangeExams = { backStack.add(ExamPicker) },
                        onStartQuiz = { examId, mode -> backStack.add(Quiz(examId, mode)) },
                        onSetLanguage = ::setAppLanguage,
                    )
                }
            }
            entry<ExamTraining> {
                ExamTrainingScreen(
                    onBack = back,
                    onChangeExams = { backStack.add(ExamPicker) },
                    onStartQuiz = { examId, mode -> backStack.add(Quiz(examId, mode)) },
                )
            }
            entry<ExamPicker> {
                ExamPickerScreen(firstRun = false, onDone = back, onBack = back)
            }
            entry<Recognition> {
                RecognitionScreen(onBack = back, onStartQuiz = { examId, mode -> backStack.add(Quiz(examId, mode)) })
            }
            entry<Quiz> { key ->
                QuizScreen(examId = key.examId, mode = key.mode, onClose = back)
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
