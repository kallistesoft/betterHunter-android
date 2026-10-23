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
import ca.kallistesoft.hunt.core.model.FieldRun
import ca.kallistesoft.hunt.core.model.FieldSkill
import ca.kallistesoft.hunt.core.model.FieldTraining
import ca.kallistesoft.hunt.core.model.PHOTO_ID_EXAM
import ca.kallistesoft.hunt.core.model.Question
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.core.model.QuizSession
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.skillPools
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.random.Random

/** A skill's readiness before and after a run. */
data class SkillChange(val skill: FieldSkill, val before: Float, val after: Float, val mastered: Boolean)

/** Today's 6 questions, once all answered: right answers per part, the streak, and whether to offer reminders. */
data class DailySummary(
    val speciesRight: Int,
    val speciesTotal: Int,
    val generalRight: Int,
    val generalTotal: Int,
    val streak: Int,
    val askReminders: Boolean,
) {
    val right: Int get() = speciesRight + generalRight
    val total: Int get() = speciesTotal + generalTotal
}

sealed interface FieldQuizUiState {
    data object Loading : FieldQuizUiState

    /** Nothing to ask (no questions for this skill, or no mistakes left). */
    data class Empty(val run: FieldRun) : FieldQuizUiState

    data class Running(val run: FieldRun, val skill: FieldSkill?, val session: QuizSession) : FieldQuizUiState

    /** [changes]: the skill (or every skill) touched by the run; [nextSkill] is offered after a skill run. */
    data class Finished(
        val run: FieldRun,
        val skill: FieldSkill?,
        val session: QuizSession?,
        val changes: List<SkillChange> = emptyList(),
        val nextSkill: FieldSkill? = null,
        val daily: DailySummary? = null,
    ) : FieldQuizUiState
}

/**
 * A new hunter's run (P11): one skill, today's mix ("Keep your edge" once all are mastered), the season check, "My
 * mistakes", or today's 6 questions (resumed where the hunter left off). Answers are recorded right away.
 */
@HiltViewModel(assistedFactory = FieldQuizViewModel.Factory::class)
class FieldQuizViewModel @AssistedInject constructor(
    @Assisted private val run: FieldRun,
    @Assisted private val skill: FieldSkill?,
    private val contentRepository: ContentRepository,
    private val userData: UserDataRepository,
    private val savedState: SavedStateHandle,
    private val today: Today,
    featureFlags: FeatureFlags,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(run: FieldRun, skill: FieldSkill?): FieldQuizViewModel
    }

    private val _uiState = MutableStateFlow<FieldQuizUiState>(FieldQuizUiState.Loading)

    /** The run is drawn (or restored) when the screen starts collecting, not when the ViewModel is created. */
    val uiState: StateFlow<FieldQuizUiState> = _uiState.asStateFlow()
        .onStart { if (_uiState.value == FieldQuizUiState.Loading) load() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FieldQuizUiState.Loading)

    val canReport: StateFlow<Boolean> = featureFlags.isEnabled(FeatureFlag.REPORT_QUESTION)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeatureFlag.REPORT_QUESTION.defaultValue)

    /** Answers being saved: the results wait for them. */
    private val saving = mutableListOf<Job>()

    private var pools: Map<FieldSkill, List<Question>> = emptyMap()

    /** Readiness per skill when the run started, for "62% → 70%". */
    private var before: Map<FieldSkill, Float> = emptyMap()

    private suspend fun load() {
        val content = contentRepository.content.first()
        val progress = userData.progress.first()
        pools = content.skillPools(FieldSkill.forWeapons(progress.weapons), progress.provinces)
        before = savedState.get<FloatArray>(KEY_BEFORE)?.let { saved -> pools.keys.zip(saved.toList()).toMap() }
            ?: readiness(progress)
        val restored = restore(content)
        when {
            restored != null && savedState.get<Boolean>(KEY_FINISHED) == true -> finish(restored)
            restored != null -> show(FieldQuizUiState.Running(run, skill, restored))
            else -> start(content, progress)
        }
    }

    fun choose(choice: Int) {
        val state = _uiState.value as? FieldQuizUiState.Running ?: return
        val session = state.session
        if (session.currentAnswered) return
        val q = session.question
        val correct = choice == q.answerIndex
        show(state.copy(session = session.answer(choice)))
        saving += viewModelScope.launch {
            if (run == FieldRun.DAILY) userData.recordDaily(q.id, correct) else userData.record(q.id, correct)
        }
    }

    fun next() {
        val state = _uiState.value as? FieldQuizUiState.Running ?: return
        if (!state.session.currentAnswered) return
        if (state.session.isLast) {
            viewModelScope.launch { finish(state.session) }
        } else {
            show(state.copy(session = state.session.next()))
        }
    }

    /** "Try again": a new draw of the same run. */
    fun restart() {
        viewModelScope.launch {
            val progress = userData.progress.first()
            before = readiness(progress)
            start(contentRepository.content.first(), progress)
        }
    }

    fun turnOnDailyReminders(granted: Boolean) {
        viewModelScope.launch { if (granted) userData.setDailyReminders(true) else userData.markDailyReminderAsked() }
        hideNudge()
    }

    fun declineDailyReminders() {
        viewModelScope.launch { userData.markDailyReminderAsked() }
        hideNudge()
    }

    private fun hideNudge() {
        _uiState.update { s ->
            if (s is FieldQuizUiState.Finished && s.daily != null) s.copy(daily = s.daily.copy(askReminders = false)) else s
        }
    }

    private suspend fun start(content: Content, progress: UserProgress) {
        val day = today()
        val random = Random.Default
        val questions = when (run) {
            FieldRun.SKILL -> FieldTraining.skill(pools[skill].orEmpty(), progress, day, random)
            FieldRun.MIX -> FieldTraining.mix(pools, progress, day, random)
            FieldRun.SEASON_CHECK -> FieldTraining.seasonCheck(pools, progress, day, random)
            FieldRun.MISTAKES -> pools.values.flatten().distinctBy { it.id }.filter { it.id in progress.weak }
                .shuffled(random).take(run.size)
            FieldRun.DAILY -> {
                val done = progress.dailyResults(day)
                dailyQuestions(content, day).filter { it.id !in done }
            }
        }
        val session = QuizSession(FIELD_EXAM, QuizMode.PRACTICE, questions)
        when {
            !session.isEmpty -> show(FieldQuizUiState.Running(run, skill, session))
            // Today's questions all answered: straight to "Done for today".
            run == FieldRun.DAILY && dailyQuestions(content, day).isNotEmpty() -> finish(null)
            else -> show(FieldQuizUiState.Empty(run))
        }
    }

    private fun dailyQuestions(content: Content, day: Long): List<Question> {
        val species = content.exam(PHOTO_ID_EXAM)?.let(content::questionsFor).orEmpty()
        return FieldTraining.daily(species, pools.values.flatten(), day)
    }

    private suspend fun finish(session: QuizSession?) {
        saving.joinAll()
        val progress = userData.progress.first()
        val day = today()
        val after = readiness(progress)
        val touched = when (run) {
            FieldRun.SKILL -> listOfNotNull(skill)
            FieldRun.DAILY -> emptyList()
            else -> pools.keys.filter { s -> session?.questions?.any { q -> q in pools.getValue(s) } == true }
        }
        val changes = touched.map { s ->
            SkillChange(s, before[s] ?: 0f, after[s] ?: 0f, progress.skillSummary(s, pools.getValue(s), day).mastered)
        }
        val nextSkill = if (run == FieldRun.SKILL) {
            val order = pools.keys.toList()
            val rest = order.drop(order.indexOf(skill) + 1) + order.take(order.indexOf(skill))
            rest.firstOrNull { (after[it] ?: 0f) < 1f && pools.getValue(it).isNotEmpty() }
        } else {
            null
        }
        val daily = if (run == FieldRun.DAILY) {
            val content = contentRepository.content.first()
            val results = progress.dailyResults(day)
            val (species, general) = dailyQuestions(content, day).partition { it.image != null }
            DailySummary(
                speciesRight = species.count { results[it.id] == true },
                speciesTotal = species.size,
                generalRight = general.count { results[it.id] == true },
                generalTotal = general.size,
                streak = progress.currentStreak(day),
                askReminders = !progress.dailyReminders && !progress.dailyReminderAsked,
            )
        } else {
            null
        }
        show(FieldQuizUiState.Finished(run, skill, session, changes, nextSkill, daily))
    }

    private fun readiness(progress: UserProgress): Map<FieldSkill, Float> {
        val day = today()
        return pools.mapValues { (s, pool) -> progress.skillSummary(s, pool, day).readiness }
    }

    private fun show(state: FieldQuizUiState) {
        _uiState.update { state }
        val session = when (state) {
            is FieldQuizUiState.Running -> state.session
            is FieldQuizUiState.Finished -> state.session
            else -> null
        }
        savedState[KEY_IDS] = session?.questions?.map { it.id }?.let { ArrayList(it) }
        savedState[KEY_ANSWERS] = session?.let { s -> IntArray(s.questions.size) { s.answers[it] ?: -1 } }
        savedState[KEY_CURRENT] = session?.current
        savedState[KEY_FINISHED] = state is FieldQuizUiState.Finished
        savedState[KEY_BEFORE] = pools.keys.map { before[it] ?: 0f }.toFloatArray()
    }

    private fun restore(content: Content): QuizSession? {
        val ids = savedState.get<ArrayList<String>>(KEY_IDS) ?: return null
        val byId = (pools.values.flatten() + content.exam(PHOTO_ID_EXAM)?.let(content::questionsFor).orEmpty())
            .associateBy { it.id }
        val questions = ids.map { byId[it] ?: return null }
        val answers = savedState.get<IntArray>(KEY_ANSWERS)
            ?.withIndex()?.filter { it.value >= 0 }?.associate { it.index to it.value }.orEmpty()
        return QuizSession(FIELD_EXAM, QuizMode.PRACTICE, questions, answers, savedState.get<Int>(KEY_CURRENT) ?: 0)
    }

    private companion object {
        /** Field runs aren't exams: a stand-in with no pass mark shown. */
        val FIELD_EXAM = Exam(id = "FIELD", names = emptyMap(), scopes = emptyList())

        const val KEY_IDS = "ids"
        const val KEY_ANSWERS = "answers"
        const val KEY_CURRENT = "current"
        const val KEY_FINISHED = "finished"
        const val KEY_BEFORE = "before"
    }
}
