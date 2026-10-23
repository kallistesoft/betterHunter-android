package ca.kallistesoft.hunt.feature.exams

import app.cash.turbine.test
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.MainDispatcherRule
import ca.kallistesoft.hunt.core.testing.awaitItemMatching
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProvincesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userData = FakeUserDataRepository(UserProgress().withProfile(HunterType.WORKING_ON_IT, emptySet(), Province.AB))

    @Test
    fun `one switch per province, the last one on locked`() = runTest {
        val vm = ProvincesViewModel(userData)
        vm.uiState.test {
            val start = awaitItemMatching { !it.loading }.provinces
            assertEquals(Province.entries, start.map { it.province })
            assertEquals(listOf(false, true, false), start.map { it.enabled })
            assertEquals(listOf(false, true, false), start.map { it.isHome })
            assertEquals(listOf(true, false, true), start.map { it.canToggle })

            vm.setEnabled(Province.QC, true)
            val both = awaitItem().provinces
            assertEquals(listOf(true, true, false), both.map { it.enabled })
            assertEquals(listOf(true, true, true), both.map { it.canToggle })

            vm.setEnabled(Province.AB, false)
            val quebecOnly = awaitItem().provinces
            assertEquals(listOf(true, false, false), quebecOnly.map { it.enabled })
            assertEquals(listOf("ICAF"), userData.progress.value.myExamIds)
        }
    }
}
