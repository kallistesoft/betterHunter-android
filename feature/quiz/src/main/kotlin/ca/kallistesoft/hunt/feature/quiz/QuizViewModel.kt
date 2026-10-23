package ca.kallistesoft.hunt.feature.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.data.ContentRepository
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.flags.FeatureFlag
import ca.kallistesoft.hunt.core.flags.FeatureFlags
import ca.kallistesoft.hunt.core.model.Content
import ca.kallistesoft.hunt.core.model.Exam
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.core.model.QuizSession
import ca.kallistesoft.hunt.core.model.UserProgress
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface QuizUiState {
    data object Loading : QuizUiState

    /** Nothing to ask: no questions, or no mistakes left to review ([exam] null when it doesn't exist). */
    data class Empty(val mode: QuizMode, val exam: Exam? = null) : QuizUiState

    /** [secondsLeft] counts down in mock exams; null otherwise. */
    data class Running(val session: QuizSession, val secondsLeft: Int?) : QuizUiState

    /** [secondsLeft]: what was left on a mock exam's clock when it ended (0 when time ran out). */
    data class Finished(val session: QuizSession, val timeUp: Boolean, val secondsLeft: Int? = null) : QuizUiState
}

/**
 * One quiz run on an exam. Practice and review answers are recorded right away; mock exam answers can change until
 * the end, so they are recorded when it finishes. The run survives configuration changes and process death.
 */
@HiltViewModel(assistedFactory = QuizViewModel.Factory::class)
class QuizViewModel @AssistedInject constructor(
    @Assisted private val examId: String,
    @Assisted private val mode: QuizMode,
    private val contentRepository: ContentRepository,
    private val userData: UserDataRepository,
    private val savedState: SavedStateHandle,
    featureFlags: FeatureFlags,
    today: Today,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(examId: String, mode: QuizMode): QuizViewModel
    }

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    /** "Report a problem" on each question, behind a feature flag. */
    val canReport: StateFlow<Boolean> = featureFlags.isEnabled(FeatureFlag.REPORT_QUESTION)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeatureFlag.REPORT_QUESTION.defaultValue)

    /** "Daily goal reached" on the results, counting every answer given today. */
    val dailyGoalReached: StateFlow<Boolean> = userData.progress
        .map { it.answeredToday(today()) >= UserProgress.DAILY_GOAL }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private var timer: Job? = null

    init {
        viewModelScope.launch {
            val content = contentRepository.content.first()
            val restored = restore(content)
            if (restored != null) show(restored) else start(content)
        }
    }

    fun choose(choice: Int) {
        val state = _uiState.value as? QuizUiState.Running ?: return
        val session = state.session
        if (session.mode != QuizMode.EXAM && session.currentAnswered) return
        val answered = session.answer(choice)
        update(state.copy(session = answered))
        if (session.mode != QuizMode.EXAM) {
            viewModelScope.launch { userData.record(session.question.id, choice == session.question.answerIndex) }
        }
    }

    fun next() {
        val state = _uiState.value as? QuizUiState.Running ?: return
        if (!state.session.currentAnswered) return
        if (state.session.isLast) finish(timeUp = false) else update(state.copy(session = state.session.next()))
    }

    /** "Try again": a new run on the same exam and mode. */
    fun restart() {
        viewModelScope.launch { start(contentRepository.content.first()) }
    }

    private suspend fun start(content: Content) {
        val exam = content.exam(examId)
        if (exam == null) {
            show(QuizUiState.Empty(mode))
            return
        }
        val pool = content.questionsFor(exam)
        val weak = if (mode == QuizMode.REVIEW) userData.progress.first().weakIds(pool) else null
        val session = QuizSession.start(exam, pool, mode, onlyIds = weak)
        show(
            if (session.isEmpty) {
                QuizUiState.Empty(mode, exam)
            } else {
                QuizUiState.Running(session, secondsLeft = if (mode == QuizMode.EXAM) exam.minutes * 60 else null)
            },
        )
    }

    private fun finish(timeUp: Boolean) {
        val state = _uiState.value as? QuizUiState.Running ?: return
        val session = state.session
        if (session.mode == QuizMode.EXAM) {
            val results = session.answers.map { (i, choice) ->
                session.questions[i].id to (choice == session.questions[i].answerIndex)
            }
            viewModelScope.launch { userData.recordAll(results) }
        }
        show(QuizUiState.Finished(session, timeUp, secondsLeft = if (timeUp) 0 else state.secondsLeft))
    }

    private fun show(state: QuizUiState) {
        update(state)
        timer?.cancel()
        if (state is QuizUiState.Running && state.secondsLeft != null) {
            timer = viewModelScope.launch {
                while (true) {
                    delay(1_000)
                    val running = _uiState.value as? QuizUiState.Running ?: break
                    val left = (running.secondsLeft ?: break) - 1
                    if (left <= 0) {
                        finish(timeUp = true)
                        break
                    }
                    update(running.copy(secondsLeft = left))
                }
            }
        }
    }

    private fun update(state: QuizUiState) {
        _uiState.update { state }
        save(state)
    }

    // Saved state: the questions drawn, the answers given and where the user is.

    private fun save(state: QuizUiState) {
        val session = when (state) {
            is QuizUiState.Running -> state.session
            is QuizUiState.Finished -> state.session
            else -> null
        }
        savedState[KEY_IDS] = session?.questions?.map { it.id }?.let { ArrayList(it) }
        savedState[KEY_ANSWERS] = session?.let { s -> IntArray(s.questions.size) { s.answers[it] ?: -1 } }
        savedState[KEY_CURRENT] = session?.current
        savedState[KEY_SECONDS] = when (state) {
            is QuizUiState.Running -> state.secondsLeft
            is QuizUiState.Finished -> state.secondsLeft
            else -> null
        }
        savedState[KEY_FINISHED] = state is QuizUiState.Finished
        savedState[KEY_TIME_UP] = (state as? QuizUiState.Finished)?.timeUp
    }

    private fun restore(content: Content): QuizUiState? {
        val ids = savedState.get<ArrayList<String>>(KEY_IDS) ?: return null
        val exam = content.exam(examId) ?: return null
        val byId = content.questionsFor(exam).associateBy { it.id }
        val questions = ids.map { byId[it] ?: return null }
        val answers = savedState.get<IntArray>(KEY_ANSWERS)
            ?.withIndex()?.filter { it.value >= 0 }?.associate { it.index to it.value }.orEmpty()
        val session = QuizSession(exam, mode, questions, answers, savedState.get<Int>(KEY_CURRENT) ?: 0)
        return if (savedState.get<Boolean>(KEY_FINISHED) == true) {
            QuizUiState.Finished(session, savedState.get<Boolean>(KEY_TIME_UP) == true, savedState.get<Int>(KEY_SECONDS))
        } else {
            QuizUiState.Running(session, savedState.get<Int>(KEY_SECONDS))
        }
    }

    private companion object {
        const val KEY_IDS = "ids"
        const val KEY_ANSWERS = "answers"
        const val KEY_CURRENT = "current"
        const val KEY_SECONDS = "secondsLeft"
        const val KEY_FINISHED = "finished"
        const val KEY_TIME_UP = "timeUp"
    }
}
