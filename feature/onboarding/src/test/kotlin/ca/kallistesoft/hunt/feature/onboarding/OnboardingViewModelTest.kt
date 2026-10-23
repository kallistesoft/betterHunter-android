package ca.kallistesoft.hunt.feature.onboarding

import androidx.lifecycle.SavedStateHandle
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.Weapon
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userData = FakeUserDataRepository()
    private val savedState = SavedStateHandle()
    private fun viewModel() = OnboardingViewModel(userData, savedState)

    @Test
    fun `each question needs an answer before continuing`() {
        val vm = viewModel()
        assertFalse(vm.uiState.value.canContinue)
        vm.next()
        assertEquals(OnboardingStep.HUNTER_TYPE, vm.uiState.value.step)

        vm.chooseHunterType(HunterType.WORKING_ON_IT)
        vm.next()
        assertEquals(OnboardingStep.WEAPONS, vm.uiState.value.step)
        assertTrue(vm.uiState.value.canContinue) // firearm starts ticked

        vm.toggleWeapon(Weapon.FIREARM)
        assertFalse(vm.uiState.value.canContinue)
        vm.toggleWeapon(Weapon.BOW)
        vm.next()
        assertEquals(OnboardingStep.PROVINCE, vm.uiState.value.step)
        assertFalse(vm.uiState.value.canContinue)
    }

    @Test
    fun `the last answer saves the profile with the province's quizzes`() = runTest {
        val vm = viewModel()
        vm.chooseHunterType(HunterType.WORKING_ON_IT)
        vm.next()
        vm.toggleWeapon(Weapon.CROSSBOW)
        vm.next()
        vm.chooseProvince(Province.AB)
        assertFalse(userData.progress.value.onboarded)
        vm.next()

        val saved = userData.progress.value
        assertTrue(saved.onboarded)
        assertEquals(HunterType.WORKING_ON_IT, saved.hunterType)
        assertEquals(setOf(Weapon.FIREARM, Weapon.CROSSBOW), saved.weapons)
        assertEquals(listOf("ABHE", "PAL"), saved.myExamIds)
    }

    @Test
    fun `back returns to the previous question, keeping the answers`() {
        val vm = viewModel()
        assertFalse(vm.back())
        vm.chooseHunterType(HunterType.NEW_HUNTER)
        vm.next()
        assertTrue(vm.back())
        assertEquals(OnboardingStep.HUNTER_TYPE, vm.uiState.value.step)
        assertEquals(HunterType.NEW_HUNTER, vm.uiState.value.hunterType)
    }

    @Test
    fun `the answers survive process death`() {
        viewModel().apply {
            chooseHunterType(HunterType.EXPERIENCED)
            next()
            toggleWeapon(Weapon.BOW)
        }
        val restored = viewModel().uiState.value
        assertEquals(OnboardingStep.WEAPONS, restored.step)
        assertEquals(HunterType.EXPERIENCED, restored.hunterType)
        assertEquals(setOf(Weapon.FIREARM, Weapon.BOW), restored.weapons)
    }
}
