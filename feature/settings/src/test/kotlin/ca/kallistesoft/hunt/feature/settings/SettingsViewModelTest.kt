package ca.kallistesoft.hunt.feature.settings

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.Weapon
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.MainDispatcherRule
import ca.kallistesoft.hunt.core.testing.awaitItemMatching
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userData = FakeUserDataRepository(
        UserProgress().withProfile(HunterType.WORKING_ON_IT, setOf(Weapon.FIREARM), Province.QC),
    )

    @Test
    fun `settings show the profile and change the daily goal`() = runTest {
        val vm = SettingsViewModel(userData)
        vm.uiState.test {
            val state = awaitItemMatching { !it.loading }
            assertEquals(HunterType.WORKING_ON_IT, state.hunterType)
            assertEquals(Province.QC, state.homeProvince)
            assertEquals(20, state.dailyGoal)
            assertTrue(state.canAddProvinces)

            vm.setDailyGoal(30)
            assertEquals(30, awaitItem().dailyGoal)
        }
    }

    @Test
    fun `an edit starts from the saved answer and is saved only on Save`() = runTest {
        val vm = ProfileEditViewModel(ProfileField.HOME_PROVINCE, userData, SavedStateHandle())
        assertEquals(Province.QC, vm.uiState.value.province)
        vm.chooseProvince(Province.AB)
        assertEquals(Province.QC, userData.progress.value.homeProvince)

        var done = false
        vm.save { done = true }
        assertTrue(done)
        assertEquals(Province.AB, userData.progress.value.homeProvince)
        assertEquals(listOf("ABHE", "PAL", "ICAF"), userData.progress.value.myExamIds)
    }

    @Test
    fun `weapons can't all be unticked`() = runTest {
        val vm = ProfileEditViewModel(ProfileField.WEAPONS, userData, SavedStateHandle())
        vm.toggleWeapon(Weapon.FIREARM)
        assertFalse(vm.uiState.value.canSave)
        vm.toggleWeapon(Weapon.BOW)
        vm.save {}
        assertEquals(setOf(Weapon.BOW), userData.progress.value.weapons)
    }

    @Test
    fun `the selection survives process death`() = runTest {
        val saved = SavedStateHandle()
        ProfileEditViewModel(ProfileField.HUNTER_TYPE, userData, saved).chooseHunterType(HunterType.EXPERIENCED)
        assertEquals(HunterType.EXPERIENCED, ProfileEditViewModel(ProfileField.HUNTER_TYPE, userData, saved).uiState.value.hunterType)
    }
}
