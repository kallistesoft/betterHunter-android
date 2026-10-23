package ca.kallistesoft.hunt.feature.exams

import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PassedExamViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userData = FakeUserDataRepository()

    @Test
    fun `"Yes, I passed" saves the exam as passed, then calls back once`() {
        val vm = PassedExamViewModel("ICAF", userData)
        var saved = 0
        vm.confirm { saved++ }
        assertEquals(setOf("ICAF"), userData.progress.value.passedExams)
        assertEquals(1, saved)

        vm.confirm { saved++ } // a second tap while leaving the screen: ignored
        assertEquals(1, saved)
    }
}
