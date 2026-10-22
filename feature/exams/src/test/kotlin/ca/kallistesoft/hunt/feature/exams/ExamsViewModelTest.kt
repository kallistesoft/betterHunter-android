package ca.kallistesoft.hunt.feature.exams

import app.cash.turbine.test
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.testing.FakeContentRepository
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.MainDispatcherRule
import ca.kallistesoft.hunt.core.testing.TEST_DAY
import ca.kallistesoft.hunt.core.testing.awaitItemMatching
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ExamsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userData = FakeUserDataRepository(UserProgress(myExams = listOf("ICAF")))

    @Test
    fun `the user's exams come first, other exams and games apart`() = runTest {
        ExamsViewModel(FakeContentRepository(), userData).uiState.test {
            val state = awaitItemMatching { !it.loading }
            assertEquals(listOf("ICAF"), state.mine.map { it.exam.id })
            assertEquals(listOf("PAL"), state.others.map { it.exam.id })
            assertEquals(listOf("ID"), state.games.map { it.exam.id })

            userData.setMyExams(listOf("PAL", "ICAF"))
            assertEquals(listOf("PAL", "ICAF"), awaitItem().mine.map { it.exam.id })
        }
    }

    @Test
    fun `readiness and mistakes follow the user's answers`() = runTest {
        ExamsViewModel(FakeContentRepository(), userData).uiState.test {
            awaitItemMatching { !it.loading }

            userData.progress.value = userData.progress.value
                .record("qc-1", correct = true, TEST_DAY)
                .record("qc-2", correct = false, TEST_DAY)
            val icaf = awaitItem().mine.single()
            assertEquals(1f / 3, icaf.readiness, 1e-6f)
            assertEquals(1, icaf.weakCount)
        }
    }
}
