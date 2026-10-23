package ca.kallistesoft.hunt.feature.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.Weapon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

/**
 * First launch: what kind of hunter, what they hunt with, where. The answers are saved together after the last
 * question, which ends onboarding (the app then shows Home). They survive rotation, a language change and process
 * death until then.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userData: UserDataRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(restore())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var saving = false

    fun chooseHunterType(type: HunterType) = update { it.copy(hunterType = type) }

    fun toggleWeapon(weapon: Weapon) = update {
        it.copy(weapons = if (weapon in it.weapons) it.weapons - weapon else it.weapons + weapon)
    }

    fun chooseProvince(province: Province) = update { it.copy(province = province) }

    /** "Continue": the next question, or after the last one, saves the profile. */
    fun next() {
        val state = _uiState.value
        if (!state.canContinue) return
        val following = OnboardingStep.entries.getOrNull(state.step.ordinal + 1)
        if (following != null) {
            update { it.copy(step = following) }
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
        val previous = OnboardingStep.entries.getOrNull(_uiState.value.step.ordinal - 1) ?: return false
        update { it.copy(step = previous) }
        return true
    }

    private fun update(transform: (OnboardingUiState) -> OnboardingUiState) {
        _uiState.update(transform)
        val state = _uiState.value
        savedState[KEY_STEP] = state.step.name
        savedState[KEY_TYPE] = state.hunterType?.name
        savedState[KEY_WEAPONS] = ArrayList(state.weapons.map { it.name })
        savedState[KEY_PROVINCE] = state.province?.name
    }

    private fun restore(): OnboardingUiState {
        val default = OnboardingUiState()
        return OnboardingUiState(
            step = savedState.get<String>(KEY_STEP)?.let(OnboardingStep::valueOf) ?: default.step,
            hunterType = savedState.get<String>(KEY_TYPE)?.let(HunterType::valueOf),
            weapons = savedState.get<ArrayList<String>>(KEY_WEAPONS)?.map(Weapon::valueOf)?.toSet() ?: default.weapons,
            province = savedState.get<String>(KEY_PROVINCE)?.let(Province::valueOf),
        )
    }

    private companion object {
        const val KEY_STEP = "step"
        const val KEY_TYPE = "hunterType"
        const val KEY_WEAPONS = "weapons"
        const val KEY_PROVINCE = "province"
    }
}
