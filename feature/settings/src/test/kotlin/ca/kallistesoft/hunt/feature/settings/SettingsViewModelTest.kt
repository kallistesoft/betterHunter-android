package ca.kallistesoft.hunt.feature.settings

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.Weapon
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.MainDispatcherRule
import ca.kallistesoft.hunt.core.testing.TEST_DAY
import ca.kallistesoft.hunt.core.testing.awaitItemMatching
import ca.kallistesoft.hunt.core.testing.collecting
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
        val vm = SettingsViewModel(userData, Today { TEST_DAY })
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
        collecting(vm.uiState)
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
        collecting(vm.uiState)
        vm.toggleWeapon(Weapon.FIREARM)
        assertFalse(vm.uiState.value.canSave)
        vm.toggleWeapon(Weapon.BOW)
        vm.save {}
        assertEquals(setOf(Weapon.BOW), userData.progress.value.weapons)
    }

    @Test
    fun `the selection survives process death`() = runTest {
        val saved = SavedStateHandle()
        ProfileEditViewModel(ProfileField.HUNTER_TYPE, userData, saved).apply {
            collecting(uiState)
            chooseHunterType(HunterType.EXPERIENCED)
        }
        val restored = ProfileEditViewModel(ProfileField.HUNTER_TYPE, userData, saved)
        collecting(restored.uiState)
        assertEquals(HunterType.EXPERIENCED, restored.uiState.value.hunterType)
    }

    @Test
    fun `a new hunter sees the season start and the reminders`() = runTest {
        val hunter = FakeUserDataRepository(
            UserProgress().withProfile(HunterType.NEW_HUNTER, setOf(Weapon.FIREARM), Province.QC).withSeasonStart(TEST_DAY + 30),
        )
        val vm = SettingsViewModel(hunter, Today { TEST_DAY })
        vm.uiState.test {
            val state = awaitItemMatching { !it.loading }
            assertTrue(state.fieldHunter)
            assertEquals(TEST_DAY + 30, state.seasonStart)
            assertFalse(state.dailyReminders)
            assertEquals(UserProgress.DEFAULT_PLAY_MINUTE, state.reminderMinute)

            vm.setSeasonReminders(true)
            assertTrue(awaitItem().seasonReminders)
        }
    }

    @Test
    fun `saving a season date asks for season reminders`() = runTest {
        val vm = SeasonDateViewModel(userData, Today { TEST_DAY })
        collecting(vm.uiState)
        assertEquals(null, vm.uiState.value.picked)
        vm.pick(TEST_DAY - 1) // the past can't be picked
        assertEquals(null, vm.uiState.value.picked)
        vm.pick(TEST_DAY + 20)
        vm.save()
        assertEquals(TEST_DAY + 20, userData.progress.value.seasonStart)
        assertTrue(vm.uiState.value.askReminders)
        vm.answerReminders(true)
        assertTrue(userData.progress.value.seasonReminders)
        assertTrue(vm.uiState.value.done)
    }

    @Test
    fun `I don't know yet clears the date`() = runTest {
        userData.setSeasonStart(TEST_DAY + 5)
        val vm = SeasonDateViewModel(userData, Today { TEST_DAY })
        collecting(vm.uiState)
        assertEquals(TEST_DAY + 5, vm.uiState.value.picked)
        vm.dontKnow()
        assertEquals(null, userData.progress.value.seasonStart)
        assertTrue(vm.uiState.value.done)
    }
}
