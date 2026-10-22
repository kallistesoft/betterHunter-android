package ca.kallistesoft.hunt.feature.quiz

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import ca.kallistesoft.hunt.core.flags.FeatureFlag
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.testing.FakeContentRepository
import ca.kallistesoft.hunt.core.testing.FakeFeatureFlags
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.MainDispatcherRule
import ca.kallistesoft.hunt.core.testing.TestContent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Virtual time: the mock exam timer only moves with advanceTimeBy, and runCurrent() runs pending work (the first
 * load, recording answers) without moving it. Turbine sees every state the screen would.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val userData = FakeUserDataRepository()
    private val flags = FakeFeatureFlags()

    private fun viewModel(mode: QuizMode, saved: SavedStateHandle = SavedStateHandle()) =
        QuizViewModel(TestContent.pal.id, mode, FakeContentRepository(), userData, saved, flags)

    private val QuizUiState.running get() = this as QuizUiState.Running

    @Test
    fun `practice answers are recorded right away and the run ends after the last question`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = viewModel(QuizMode.PRACTICE)
            vm.uiState.test {
                assertEquals(QuizUiState.Loading, awaitItem())
                runCurrent()
                val start = awaitItem().running
                assertEquals(3, start.session.questions.size)
                assertNull("no timer in practice", start.secondsLeft)

                for (i in 0..2) {
                    vm.choose(1)
                    assertEquals(1, awaitItem().running.session.currentAnswer)
                    runCurrent()
                    assertEquals(i + 1, userData.recorded.size) // recorded before moving on
                    vm.next()
                    if (i < 2) assertEquals(i + 1, awaitItem().running.session.current)
                }

                val finished = awaitItem() as QuizUiState.Finished
                assertEquals(3, finished.session.correctCount)
                assertFalse(finished.timeUp)
            }
            assertTrue(userData.recorded.all { (_, correct) -> correct })
        }

    @Test
    fun `practice answers are final`() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel(QuizMode.PRACTICE)
        vm.uiState.test {
            skipItems(1)
            runCurrent()
            skipItems(1)
            vm.choose(0)
            assertEquals(0, awaitItem().running.session.currentAnswer)
            vm.choose(1) // ignored: no new state
            expectNoEvents()
        }
    }

    @Test
    fun `mock exam answers can change and are only recorded at the end`() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel(QuizMode.EXAM)
        vm.uiState.test {
            skipItems(1)
            runCurrent()
            assertEquals(60, awaitItem().running.secondsLeft)

            vm.choose(0)
            assertEquals(0, awaitItem().running.session.currentAnswer)
            vm.choose(1) // changed
            assertEquals(1, awaitItem().running.session.currentAnswer)
            vm.next()
            awaitItem()
            runCurrent()
            assertTrue("nothing recorded mid-exam", userData.recorded.isEmpty())

            vm.choose(0)
            awaitItem()
            vm.next()
            awaitItem()
            vm.choose(1)
            awaitItem()
            vm.next()

            val finished = awaitItem() as QuizUiState.Finished
            assertEquals(2, finished.session.correctCount)
            cancelAndIgnoreRemainingEvents()
        }
        runCurrent()
        assertEquals(3, userData.recorded.size)
    }

    @Test
    fun `the timer counts down and ends the mock exam`() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel(QuizMode.EXAM)
        vm.uiState.test {
            skipItems(1)
            runCurrent()
            assertEquals(60, awaitItem().running.secondsLeft)
            advanceTimeBy(1_001)
            assertEquals(59, awaitItem().running.secondsLeft)
            advanceTimeBy(59_000)
            val finished = expectMostRecentItem() as QuizUiState.Finished
            assertTrue(finished.timeUp)
        }
    }

    @Test
    fun `a run is restored after process death`() = runTest(mainDispatcherRule.testDispatcher) {
        val saved = SavedStateHandle()
        lateinit var drawn: List<String>
        viewModel(QuizMode.PRACTICE, saved).let { vm ->
            vm.uiState.test {
                skipItems(1)
                runCurrent()
                skipItems(1)
                vm.choose(1)
                awaitItem()
                vm.next()
                drawn = awaitItem().running.session.questions.map { it.id }
            }
        }

        viewModel(QuizMode.PRACTICE, saved).uiState.test {
            assertEquals(QuizUiState.Loading, awaitItem())
            runCurrent()
            val restored = awaitItem().running.session
            assertEquals(drawn, restored.questions.map { it.id })
            assertEquals(1, restored.current)
            assertEquals(mapOf(0 to 1), restored.answers)
        }
    }

    @Test
    fun `review only asks the user's mistakes, or has nothing to ask`() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel(QuizMode.REVIEW).uiState.test {
            skipItems(1)
            runCurrent()
            assertEquals(QuizUiState.Empty(QuizMode.REVIEW), awaitItem())
        }

        userData.progress.value = UserProgress().record("federal-2", correct = false, today = 1)
        viewModel(QuizMode.REVIEW).uiState.test {
            skipItems(1)
            runCurrent()
            assertEquals(listOf("federal-2"), awaitItem().running.session.questions.map { it.id })
        }
    }

    @Test
    fun `report button follows its feature flag`() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel(QuizMode.PRACTICE)
        vm.canReport.test {
            assertTrue(awaitItem())
            flags.set(FeatureFlag.REPORT_QUESTION, false)
            runCurrent()
            assertFalse(awaitItem())
        }
    }
}
