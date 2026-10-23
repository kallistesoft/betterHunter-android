package ca.kallistesoft.hunt.feature.exams

import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PassedExamViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userData = FakeUserDataRepository()

    @Test
    fun `yes I passed saves the exam, then says so`() = runTest {
        val vm = PassedExamViewModel("ABHE", userData)
        assertFalse(vm.saved.value)
        vm.confirm()
        vm.confirm() // a double tap saves once
        assertEquals(setOf("ABHE"), userData.progress.value.passedExams)
        assertTrue(vm.saved.value)
    }
}
