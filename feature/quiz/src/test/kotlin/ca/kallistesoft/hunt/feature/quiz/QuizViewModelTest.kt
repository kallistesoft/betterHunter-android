package ca.kallistesoft.hunt.feature.quiz

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.flags.FeatureFlag
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.testing.FakeContentRepository
import ca.kallistesoft.hunt.core.testing.FakeFeatureFlags
import ca.kallistesoft.hunt.core.testing.FakeInterstitialAds
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.MainDispatcherRule
import ca.kallistesoft.hunt.core.testing.TEST_DAY
import ca.kallistesoft.hunt.core.testing.TestContent
import kotlinx.coroutines.CompletableDeferred
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
    private val ads = FakeInterstitialAds()

    private fun viewModel(mode: QuizMode, saved: SavedStateHandle = SavedStateHandle()) =
        QuizViewModel(TestContent.pal.id, mode, FakeContentRepository(), userData, saved, flags, Today { TEST_DAY }, ads)

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
            assertEquals("the time left is kept for the results", 60, finished.secondsLeft)
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
            assertEquals(0, finished.secondsLeft)
        }
        assertEquals("time up also goes through the interstitial", 1, ads.shown)
    }

    @Test
    fun `the interstitial comes between the last answer and the results, never during a question`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val onScreen = CompletableDeferred<Unit>().also { ads.onScreen = it }
            val vm = viewModel(QuizMode.EXAM)
            vm.uiState.test {
                skipItems(1)
                runCurrent()
                skipItems(1)
                repeat(3) { i ->
                    vm.choose(1)
                    awaitItem()
                    if (i < 2) {
                        vm.next()
                        awaitItem()
                    }
                }
                assertEquals("no ad during the questions", 0, ads.shown)

                vm.next()
                runCurrent()
                assertEquals(1, ads.shown)
                vm.choose(0) // the ad is on screen: the last question takes no more input
                vm.next()
                advanceTimeBy(5_000) // nor does the clock end the exam a second time
                expectNoEvents()

                onScreen.complete(Unit)
                val finished = awaitItem() as QuizUiState.Finished
                assertEquals("the answers stay as they were", 1, finished.session.answers[2])
                assertEquals(1, ads.shown)
            }
        }

    @Test
    fun `try again after the results is a new run that can end with an ad again`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val vm = viewModel(QuizMode.PRACTICE)
            vm.uiState.test {
                skipItems(1)
                runCurrent()
                skipItems(1)
                repeat(3) {
                    vm.choose(1)
                    vm.next()
                }
                runCurrent()
                assertTrue(expectMostRecentItem() is QuizUiState.Finished)
                vm.restart()
                runCurrent()
                assertTrue(expectMostRecentItem() is QuizUiState.Running)
                repeat(3) {
                    vm.choose(1)
                    vm.next()
                }
                runCurrent()
                assertTrue(expectMostRecentItem() is QuizUiState.Finished)
            }
            assertEquals(2, ads.shown)
        }

    @Test
    fun `the run is drawn once, not again when the screen comes back`() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel(QuizMode.PRACTICE)
        lateinit var drawn: List<String>
        vm.uiState.test {
            skipItems(1)
            runCurrent()
            drawn = awaitItem().running.session.questions.map { it.id }
        }
        advanceTimeBy(10_000) // past WhileSubscribed's 5 s: the state stops being shared

        vm.uiState.test {
            runCurrent()
            assertEquals(drawn, expectMostRecentItem().running.session.questions.map { it.id })
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
            assertEquals(QuizUiState.Empty(QuizMode.REVIEW, TestContent.pal), awaitItem())
        }

        userData.progress.value = UserProgress().record("federal-2", correct = false, today = 1)
        viewModel(QuizMode.REVIEW).uiState.test {
            skipItems(1)
            runCurrent()
            assertEquals(listOf("federal-2"), awaitItem().running.session.questions.map { it.id })
        }
    }

    @Test
    fun `results say when the daily goal is reached`() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel(QuizMode.PRACTICE)
        vm.dailyGoalReached.test {
            assertFalse(awaitItem())
            userData.progress.value = (1..UserProgress.DEFAULT_DAILY_GOAL).fold(UserProgress()) { p, i -> p.record("q$i", true, TEST_DAY) }
            runCurrent()
            assertTrue(awaitItem())
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
