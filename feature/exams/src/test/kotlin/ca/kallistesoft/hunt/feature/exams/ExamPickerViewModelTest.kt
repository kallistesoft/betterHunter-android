package ca.kallistesoft.hunt.feature.exams

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import ca.kallistesoft.hunt.core.testing.FakeContentRepository
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.MainDispatcherRule
import ca.kallistesoft.hunt.core.testing.awaitItemMatching
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExamPickerViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userData = FakeUserDataRepository() // first launch: no exams chosen yet
    private val savedState = SavedStateHandle()
    private fun viewModel() = ExamPickerViewModel(FakeContentRepository(), userData, savedState)

    @Test
    fun `offers the training exams only, and saves the choice in display order`() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            val start = awaitItemMatching { it.exams.isNotEmpty() }
            assertEquals(listOf("PAL", "ICAF"), start.exams.map { it.exam.id }) // no photo game
            assertTrue(start.chosen.isEmpty())

            vm.toggle("ICAF")
            assertEquals(setOf("ICAF"), awaitItem().chosen)
            vm.toggle("PAL")
            assertEquals(setOf("ICAF", "PAL"), awaitItem().chosen)
            vm.toggle("ICAF")
            assertEquals(setOf("PAL"), awaitItem().chosen)
            vm.toggle("ICAF")
            awaitItem()
        }
        var saved = false
        vm.save { saved = true }
        assertTrue(saved)
        assertEquals(listOf("PAL", "ICAF"), userData.progress.value.myExams)
    }

    @Test
    fun `skip picks every exam`() = runTest {
        val vm = viewModel()
        vm.uiState.test { awaitItemMatching { it.exams.isNotEmpty() } }
        vm.save(all = true) {}
        assertEquals(listOf("PAL", "ICAF"), userData.progress.value.myExams)
    }

    @Test
    fun `the selection survives process death`() = runTest {
        viewModel().let { vm ->
            vm.uiState.test {
                awaitItemMatching { it.exams.isNotEmpty() }
                vm.toggle("PAL")
                awaitItem()
            }
        }
        viewModel().uiState.test {
            assertEquals(setOf("PAL"), awaitItemMatching { it.exams.isNotEmpty() }.chosen)
        }
    }
}
