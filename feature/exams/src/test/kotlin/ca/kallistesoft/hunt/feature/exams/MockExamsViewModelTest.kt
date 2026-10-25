package ca.kallistesoft.hunt.feature.exams

import app.cash.turbine.test
import ca.kallistesoft.hunt.core.ads.RewardOutcome
import ca.kallistesoft.hunt.core.model.MOCK_COUNT
import ca.kallistesoft.hunt.core.model.MockResult
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.testing.FakeContentRepository
import ca.kallistesoft.hunt.core.testing.FakeRewardedAds
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.MainDispatcherRule
import ca.kallistesoft.hunt.core.testing.TestContent
import ca.kallistesoft.hunt.core.testing.awaitItemMatching
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MockExamsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val exam = TestContent.pal
    private val ads = FakeRewardedAds()

    private fun userData(done: Int = 0) = FakeUserDataRepository(
        (1..done).fold(UserProgress()) { p, n -> p.withMockResult(exam.id, n, if (n % 2 == 0) 90 else 60) },
    )

    private fun viewModel(userData: FakeUserDataRepository) = MockExamsViewModel(exam.id, FakeContentRepository(), userData, ads)

    @Test
    fun `ten mocks with their scores, and the rewarded ad loads as the list opens`() = runTest {
        val vm = viewModel(userData(done = 3))
        vm.uiState.test {
            val state = awaitItemMatching { !it.loading }
            assertEquals(exam, state.exam)
            assertEquals((1..MOCK_COUNT).toList(), state.mocks.map { it.number })
            assertEquals(MockResult(best = 90, last = 90), state.mocks[1].result)
            assertEquals(listOf(false, true, false), state.mocks.take(3).map { it.passed })
            assertNull(state.mocks[3].result)
            assertEquals(1, state.passedCount)
            assertFalse(state.allDone)
        }
        assertEquals(1, ads.preloads)
    }

    @Test
    fun `a watched ad unlocks the retake, which the screen then starts`() = runTest {
        val vm = viewModel(userData(done = 2))
        vm.uiState.test {
            awaitItemMatching { !it.loading }
            vm.unlock(MockUnlock.Retake(2))
            assertEquals(2, awaitItemMatching { it.start != null }.start)
            vm.started()
            assertNull(awaitItem().start)
        }
        assertEquals(1, ads.shown)
    }

    @Test
    fun `no ad available online, or ads off, still unlocks it`() = runTest {
        ads.outcome = RewardOutcome.NO_ADS
        val vm = viewModel(userData(done = 1))
        vm.uiState.test {
            awaitItemMatching { !it.loading }
            vm.unlock(MockUnlock.Retake(1))
            assertEquals(1, awaitItemMatching { it.start != null }.start)
        }
    }

    @Test
    fun `offline unlocks nothing and says why, until dismissed`() = runTest {
        ads.outcome = RewardOutcome.OFFLINE
        val userData = userData(done = MOCK_COUNT)
        val vm = viewModel(userData)
        vm.uiState.test {
            awaitItemMatching { !it.loading }
            vm.unlock(MockUnlock.Retake(4))
            val offline = awaitItemMatching { it.offline }
            assertNull(offline.start)
            assertFalse(offline.watchingAd)
            vm.unlock(MockUnlock.ResetAll)
            awaitItemMatching { it.offline && !it.watchingAd }
            assertEquals("nothing reset", MOCK_COUNT, userData.progress.value.mockResults(exam.id).size)
            vm.dismissOffline()
            assertFalse(awaitItem().offline)
        }
    }

    @Test
    fun `an ad closed early unlocks nothing`() = runTest {
        ads.outcome = RewardOutcome.NOT_EARNED
        val vm = viewModel(userData(done = 1))
        vm.uiState.test {
            awaitItemMatching { !it.loading }
            vm.unlock(MockUnlock.Retake(1))
            val after = awaitItemMatching { !it.watchingAd && ads.shown == 1 }
            assertNull(after.start)
            assertFalse(after.offline)
        }
    }

    @Test
    fun `with all ten done, a watched ad resets them all`() = runTest {
        val userData = userData(done = MOCK_COUNT)
        val vm = viewModel(userData)
        vm.uiState.test {
            assertTrue(awaitItemMatching { !it.loading }.allDone)
            vm.unlock(MockUnlock.ResetAll)
            val reset = awaitItemMatching { it.mocks.all { m -> m.result == null } }
            assertFalse(reset.allDone)
            assertEquals(0, reset.passedCount)
            assertNull("nothing to start", reset.start)
        }
    }

    @Test
    fun `a second tap while the ad is on its way is ignored`() = runTest {
        val vm = viewModel(userData(done = 1))
        vm.uiState.test {
            awaitItemMatching { !it.loading }
            ads.onShow = CompletableDeferred()
            vm.unlock(MockUnlock.Retake(1))
            assertTrue(awaitItemMatching { it.watchingAd }.watchingAd)
            vm.unlock(MockUnlock.Retake(1))
            ads.onShow?.complete(Unit)
            awaitItemMatching { it.start == 1 }
        }
        assertEquals(1, ads.shown)
    }
}
