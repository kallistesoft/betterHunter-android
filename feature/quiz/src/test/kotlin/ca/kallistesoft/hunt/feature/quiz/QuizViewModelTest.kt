package ca.kallistesoft.hunt.feature.quiz

import androidx.lifecycle.SavedStateHandle
import ca.kallistesoft.hunt.core.data.ContentRepository
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.flags.DefaultFeatureFlags
import ca.kallistesoft.hunt.core.model.Content
import ca.kallistesoft.hunt.core.model.Exam
import ca.kallistesoft.hunt.core.model.Lang
import ca.kallistesoft.hunt.core.model.Question
import ca.kallistesoft.hunt.core.model.QuestionText
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.core.model.UserProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {
    // Virtual time: the mock exam timer only moves with advanceTimeBy, so runCurrent() runs pending work without it.
    private val dispatcher = StandardTestDispatcher()
    private val exam = Exam("PAL", mapOf(Lang.FR to "PAL"), listOf("federal"), questionCount = 3, minutes = 1)
    private val questions = (1..3).map { i ->
        Question(
            id = "q$i",
            scope = "federal",
            chapter = "c",
            texts = mapOf(Lang.FR to QuestionText("Q$i?", listOf("A", "B", "C", "D"), "E$i")),
            answerIndex = 1,
        )
    }
    private val content = object : ContentRepository {
        override val content: Flow<Content> = flowOf(Content(listOf(exam), mapOf("federal" to questions)))
    }
    private val userData = FakeUserData()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.viewModel(mode: QuizMode, saved: SavedStateHandle = SavedStateHandle()) =
        QuizViewModel(exam.id, mode, content, userData, saved, DefaultFeatureFlags()).also { runCurrent() }

    private val QuizViewModel.running get() = uiState.value as QuizUiState.Running

    @Test
    fun `practice answers are recorded right away and the run ends after the last question`() = runTest(dispatcher) {
        val vm = viewModel(QuizMode.PRACTICE)
        repeat(3) {
            vm.choose(1)
            runCurrent()
            vm.next()
        }
        assertEquals(3, userData.recorded.size)
        assertTrue(userData.recorded.all { it.second })
        val finished = vm.uiState.value as QuizUiState.Finished
        assertEquals(3, finished.session.correctCount)
    }

    @Test
    fun `mock exam answers can change and are recorded at the end`() = runTest(dispatcher) {
        val vm = viewModel(QuizMode.EXAM)
        vm.choose(0)
        vm.choose(1) // changed
        vm.next()
        runCurrent()
        assertTrue(userData.recorded.isEmpty())
        vm.choose(0)
        vm.next()
        vm.choose(1)
        vm.next()
        runCurrent()
        assertEquals(listOf("q1", "q2", "q3").toSet(), userData.recorded.map { it.first }.toSet())
        assertEquals(2, (vm.uiState.value as QuizUiState.Finished).session.correctCount)
    }

    @Test
    fun `the mock exam ends when time is up`() = runTest(dispatcher) {
        val vm = viewModel(QuizMode.EXAM)
        assertEquals(60, vm.running.secondsLeft)
        advanceTimeBy(30_001)
        assertEquals(30, vm.running.secondsLeft)
        advanceTimeBy(30_000)
        val finished = vm.uiState.value as QuizUiState.Finished
        assertTrue(finished.timeUp)
    }

    @Test
    fun `a run is restored after process death`() = runTest(dispatcher) {
        val saved = SavedStateHandle()
        val first = viewModel(QuizMode.PRACTICE, saved)
        first.choose(1)
        first.next()
        val drawn = first.running.session.questions.map { it.id }

        val restored = viewModel(QuizMode.PRACTICE, saved).running.session
        assertEquals(drawn, restored.questions.map { it.id })
        assertEquals(1, restored.current)
        assertEquals(mapOf(0 to 1), restored.answers)
    }

    @Test
    fun `review without mistakes has nothing to ask`() = runTest(dispatcher) {
        assertEquals(QuizUiState.Empty(QuizMode.REVIEW), viewModel(QuizMode.REVIEW).uiState.value)
    }

    private class FakeUserData : UserDataRepository {
        val recorded = mutableListOf<Pair<String, Boolean>>()
        override val progress = MutableStateFlow(UserProgress())

        override suspend fun record(questionId: String, correct: Boolean) {
            recorded += questionId to correct
        }

        override suspend fun recordAll(results: List<Pair<String, Boolean>>) {
            recorded += results
        }

        override suspend fun setMyExams(ids: List<String>) = Unit

        override suspend fun answerPhotoOfDay(questionId: String, choice: Int, correct: Boolean) = Unit
    }
}
