package ca.kallistesoft.hunt.feature.quiz

import androidx.lifecycle.SavedStateHandle
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.model.FieldRun
import ca.kallistesoft.hunt.core.model.FieldSkill
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.Weapon
import ca.kallistesoft.hunt.core.testing.FakeContentRepository
import ca.kallistesoft.hunt.core.testing.FakeFeatureFlags
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.MainDispatcherRule
import ca.kallistesoft.hunt.core.testing.TEST_DAY
import ca.kallistesoft.hunt.core.testing.collecting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FieldQuizViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val userData = FakeUserDataRepository(
        UserProgress().withProfile(HunterType.NEW_HUNTER, setOf(Weapon.FIREARM), Province.QC),
    )

    /** A run on screen: its state is collected, which draws (or restores) the questions. */
    private fun TestScope.viewModel(run: FieldRun, skill: FieldSkill? = null) = FieldQuizViewModel(
        run, skill, FakeContentRepository(), userData, SavedStateHandle(), Today { TEST_DAY }, FakeFeatureFlags(),
    ).also { collecting(it.uiState) }

    /** Answers every question right (answer B in the test content). */
    private fun TestScope.answerAll(vm: FieldQuizViewModel) {
        while (vm.uiState.value is FieldQuizUiState.Running) {
            vm.choose(1)
            runCurrent()
            vm.next()
            runCurrent()
        }
    }

    @Test
    fun `a skill run shows the skill mastered and offers the next skill`() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel(FieldRun.SKILL, FieldSkill.SHOT_PLACEMENT)
        runCurrent()
        val running = vm.uiState.value as FieldQuizUiState.Running
        assertEquals(2, running.session.questions.size)
        answerAll(vm)
        val done = vm.uiState.value as FieldQuizUiState.Finished
        val change = done.changes.single()
        assertEquals(FieldSkill.SHOT_PLACEMENT, change.skill)
        assertEquals(0f, change.before, 0f)
        assertEquals(1f, change.after, 0f)
        assertTrue(change.mastered)
        assertEquals(FieldSkill.AFTER_SHOT, done.nextSkill)
    }

    @Test
    fun `today's questions end on Done for today, which offers reminders once`() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel(FieldRun.DAILY)
        runCurrent()
        val questions = (vm.uiState.value as FieldQuizUiState.Running).session.questions
        assertEquals(listOf(true, true, true, false, false, false), questions.map { it.image != null })
        answerAll(vm)
        val daily = (vm.uiState.value as FieldQuizUiState.Finished).daily!!
        assertEquals(6, daily.right)
        assertEquals(3, daily.speciesTotal)
        assertEquals(1, daily.streak)
        assertTrue(daily.askReminders)

        vm.turnOnDailyReminders(granted = true)
        runCurrent()
        assertTrue(userData.progress.value.dailyReminders)
        assertFalse((vm.uiState.value as FieldQuizUiState.Finished).daily!!.askReminders)

        // Coming back to today's questions: done, nothing asked again.
        val again = viewModel(FieldRun.DAILY)
        runCurrent()
        val summary = (again.uiState.value as FieldQuizUiState.Finished).daily!!
        assertEquals(6, summary.total)
        assertFalse(summary.askReminders)
    }

    @Test
    fun `today's questions resume where the hunter left off`() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel(FieldRun.DAILY)
        runCurrent()
        vm.choose(0) // wrong, still counts as answered
        runCurrent()
        val resumed = viewModel(FieldRun.DAILY)
        runCurrent()
        assertEquals(5, (resumed.uiState.value as FieldQuizUiState.Running).session.questions.size)
    }

    @Test
    fun `the season check covers every skill and shows each one's change`() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel(FieldRun.SEASON_CHECK)
        runCurrent()
        assertEquals(8, (vm.uiState.value as FieldQuizUiState.Running).session.questions.size)
        answerAll(vm)
        val done = vm.uiState.value as FieldQuizUiState.Finished
        assertEquals(4, done.changes.size)
        assertTrue(done.changes.all { it.mastered })
    }

    @Test
    fun `no mistakes, nothing to review`() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = viewModel(FieldRun.MISTAKES)
        runCurrent()
        assertEquals(FieldQuizUiState.Empty(FieldRun.MISTAKES), vm.uiState.value)
    }
}
