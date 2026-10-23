package ca.kallistesoft.hunt.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.data.ContentRepository
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.flags.FeatureFlag
import ca.kallistesoft.hunt.core.flags.FeatureFlags
import ca.kallistesoft.hunt.core.model.Exam
import ca.kallistesoft.hunt.core.model.PHOTO_ID_EXAM
import ca.kallistesoft.hunt.core.model.Question
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.photoOfDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhotoOfDayState(val examId: String, val question: Question, val choice: Int?)

data class HomeUiState(
    val loading: Boolean = true,
    val hasSampleContent: Boolean = false,
    /** New or experienced hunter: Home is built around "What is it?"; otherwise around exam practice. */
    val hunter: Boolean = false,
    /** "Welcome, new hunter!" after passing a real exam, until closed. */
    val newHunterBanner: Boolean = false,
    val answeredToday: Int = 0,
    val streak: Int = 0,
    /** First of the user's exams (their home province's hunter course), for "Practise now"; null hides it. */
    val mainExam: Exam? = null,
    val trainingExamCount: Int = 0,
    /** Average readiness over the user's exams; null when they have none. */
    val readiness: Float? = null,
    /** A real exam the user passed ("ICAF passed" on the Exam training tile). */
    val passedExamId: String? = null,
    /** The photo game ("Play What is it?" for hunters) and the user's readiness on it; null when hidden. */
    val photoGameId: String? = null,
    val photoReadiness: Float = 0f,
    val gameCount: Int = 0,
    val animalCount: Int = 0,
    val photoOfDay: PhotoOfDayState? = null,
) {
    val dailyGoal: Int get() = UserProgress.DAILY_GOAL
}

/** Dashboard built around the exams the user is preparing for, or around "What is it?" for hunters. */
@HiltViewModel
class HomeViewModel @Inject constructor(
    contentRepository: ContentRepository,
    private val userData: UserDataRepository,
    private val today: Today,
    featureFlags: FeatureFlags,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        contentRepository.content,
        userData.progress,
        featureFlags.values,
    ) { content, progress, flags ->
        val day = today()
        val mine = progress.myExamIds.mapNotNull { id -> content.trainingExams.firstOrNull { it.id == id } }
        val games = if (flags.getValue(FeatureFlag.RECOGNITION_GAMES)) content.games else emptyList()
        val photoExam = content.games.firstOrNull { it.id == PHOTO_ID_EXAM }
        val photos = photoExam?.let(content::questionsFor).orEmpty()
        HomeUiState(
            loading = false,
            hasSampleContent = content.hasSampleContent,
            hunter = progress.isHunter,
            newHunterBanner = progress.newHunterBanner,
            answeredToday = progress.answeredToday(day),
            streak = progress.currentStreak(day),
            mainExam = mine.firstOrNull(),
            trainingExamCount = content.trainingExams.size,
            readiness = mine.takeIf { it.isNotEmpty() }
                ?.map { progress.readiness(content.questionsFor(it)) }?.average()?.toFloat(),
            passedExamId = mine.firstOrNull { it.id in progress.passedExams }?.id,
            photoGameId = photoExam?.id?.takeIf { photoExam in games },
            photoReadiness = progress.readiness(photos),
            gameCount = games.size,
            animalCount = if (games.isEmpty()) 0 else photos.size,
            photoOfDay = photoExam?.takeIf { flags.getValue(FeatureFlag.PHOTO_OF_THE_DAY) }?.let { exam ->
                photoOfDay(photos, day)?.let { PhotoOfDayState(exam.id, it, progress.photoOfDayChoice(day)) }
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun dismissNewHunterBanner() {
        viewModelScope.launch { userData.dismissNewHunterBanner() }
    }

    /** Answers the photo of the day; it counts toward the goal, the streak and weak spots. */
    fun answerPhotoOfDay(choice: Int) {
        val photo = uiState.value.photoOfDay ?: return
        if (photo.choice != null) return
        viewModelScope.launch {
            userData.answerPhotoOfDay(photo.question.id, choice, choice == photo.question.answerIndex)
        }
    }
}
