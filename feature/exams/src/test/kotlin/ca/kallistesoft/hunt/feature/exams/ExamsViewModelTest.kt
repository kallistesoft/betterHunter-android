package ca.kallistesoft.hunt.feature.exams

import app.cash.turbine.test
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.testing.FakeContentRepository
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.MainDispatcherRule
import ca.kallistesoft.hunt.core.testing.TEST_DAY
import ca.kallistesoft.hunt.core.testing.awaitItemMatching
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExamsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userData = FakeUserDataRepository(UserProgress().withProfile(HunterType.WORKING_ON_IT, emptySet(), Province.QC))

    @Test
    fun `the user's provinces set their exams, the other provinces are offered`() = runTest {
        ExamsViewModel(FakeContentRepository(), userData).uiState.test {
            val state = awaitItemMatching { !it.loading }
            assertEquals(listOf("ICAF", "PAL"), state.mine.map { it.exam.id })
            assertEquals(listOf(Province.AB, Province.OTHER), state.otherProvinces)
            assertEquals(listOf("ID"), state.games.map { it.exam.id })

            userData.setProvince(Province.OTHER, true)
            val more = awaitItem()
            assertEquals(listOf("ICAF", "PAL"), more.mine.map { it.exam.id }) // the PAL once
            assertEquals(listOf(Province.AB), more.otherProvinces)
        }
    }

    @Test
    fun `only a hunter course not yet passed offers "I passed the real exam"`() = runTest {
        userData.setProvince(Province.OTHER, true)
        ExamsViewModel(FakeContentRepository(), userData).uiState.test {
            val state = awaitItemMatching { !it.loading }
            val (icaf, pal) = state.mine
            assertTrue(state.canMarkPassed(icaf))
            assertFalse(state.canMarkPassed(pal)) // the PAL is a licence, not a hunter course

            userData.markExamPassed("ICAF")
            val passed = awaitItem()
            assertEquals(setOf("ICAF"), passed.passed)
            assertFalse(passed.canMarkPassed(passed.mine.first()))
        }
    }

    @Test
    fun `readiness and mistakes follow the user's answers`() = runTest {
        ExamsViewModel(FakeContentRepository(), userData).uiState.test {
            awaitItemMatching { !it.loading }

            userData.progress.value = userData.progress.value
                .record("qc-1", correct = true, TEST_DAY)
                .record("qc-2", correct = false, TEST_DAY)
            val icaf = awaitItem().mine.first { it.exam.id == "ICAF" }
            assertEquals(1f / 3, icaf.readiness, 1e-6f)
            assertEquals(1, icaf.weakCount)
        }
    }
}
