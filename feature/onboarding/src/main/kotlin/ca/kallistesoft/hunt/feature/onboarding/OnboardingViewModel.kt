package ca.kallistesoft.hunt.feature.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.Weapon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.Serializable
import javax.inject.Inject

/** The three onboarding questions, in order. */
enum class OnboardingStep { HUNTER_TYPE, WEAPONS, PROVINCE }

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.HUNTER_TYPE,
    val hunterType: HunterType? = null,
    /** Most hunters use a firearm: it starts ticked. */
    val weapons: Set<Weapon> = setOf(Weapon.FIREARM),
    val province: Province? = null,
) {
    val canContinue: Boolean
        get() = when (step) {
            OnboardingStep.HUNTER_TYPE -> hunterType != null
            OnboardingStep.WEAPONS -> weapons.isNotEmpty()
            OnboardingStep.PROVINCE -> province != null
        }
}

/** The answers so far. Serializable: it lives in the saved state, so it survives process death. */
private data class Draft(
    val step: OnboardingStep = OnboardingStep.HUNTER_TYPE,
    val hunterType: HunterType? = null,
    val weapons: Set<Weapon> = OnboardingUiState().weapons,
    val province: Province? = null,
) : Serializable

private fun Draft.toUiState() = OnboardingUiState(step, hunterType, weapons, province)

/**
 * First launch: what kind of hunter, what they hunt with, where. The answers are saved together after the last
 * question, which ends onboarding (the app then shows Home). They survive rotation, a language change and process
 * death until then.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userData: UserDataRepository,
    savedState: SavedStateHandle,
) : ViewModel() {
    private val draft = savedState.getMutableStateFlow(KEY_DRAFT, Draft())

    /** Nothing to load: the state starts from the (restored) answers, so the screen never flashes the first question. */
    val uiState: StateFlow<OnboardingUiState> = draft.map { it.toUiState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), draft.value.toUiState())

    private var saving = false

    fun chooseHunterType(type: HunterType) = draft.update { it.copy(hunterType = type) }

    fun toggleWeapon(weapon: Weapon) = draft.update {
        it.copy(weapons = if (weapon in it.weapons) it.weapons - weapon else it.weapons + weapon)
    }

    fun chooseProvince(province: Province) = draft.update { it.copy(province = province) }

    /** "Continue": the next question, or after the last one, saves the profile. */
    fun next() {
        val state = draft.value.toUiState()
        if (!state.canContinue) return
        val following = OnboardingStep.entries.getOrNull(state.step.ordinal + 1)
        if (following != null) {
            draft.update { it.copy(step = following) }
            return
        }
        if (saving) return
        saving = true
        viewModelScope.launch {
            userData.saveProfile(state.hunterType!!, state.weapons, state.province!!)
        }
    }

    /** Back to the previous question; false on the first one (the system then handles back). */
    fun back(): Boolean {
        val previous = OnboardingStep.entries.getOrNull(draft.value.step.ordinal - 1) ?: return false
        draft.update { it.copy(step = previous) }
        return true
    }

    private companion object {
        const val KEY_DRAFT = "draft"
    }
}
