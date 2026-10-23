package ca.kallistesoft.hunt.feature.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.Weapon
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The onboarding answers Settings can change, each on its own screen. */
enum class ProfileField { HUNTER_TYPE, WEAPONS, HOME_PROVINCE }

data class ProfileEditUiState(
    val field: ProfileField,
    val hunterType: HunterType? = null,
    val weapons: Set<Weapon> = emptySet(),
    val province: Province? = null,
    val loading: Boolean = true,
) {
    val canSave: Boolean
        get() = !loading && when (this.field) {
            ProfileField.HUNTER_TYPE -> hunterType != null
            ProfileField.WEAPONS -> weapons.isNotEmpty()
            ProfileField.HOME_PROVINCE -> province != null
        }
}

/**
 * Changes one onboarding answer from Settings: starts from the saved answer, saved only on "Save". The selection
 * survives rotation and process death until then.
 */
@HiltViewModel(assistedFactory = ProfileEditViewModel.Factory::class)
class ProfileEditViewModel @AssistedInject constructor(
    @Assisted private val field: ProfileField,
    private val userData: UserDataRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(field: ProfileField): ProfileEditViewModel
    }

    private val _uiState = MutableStateFlow(ProfileEditUiState(field))
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    private var saving = false

    init {
        viewModelScope.launch {
            val p = userData.progress.first()
            _uiState.value = ProfileEditUiState(
                field = field,
                hunterType = savedState.get<String>(KEY_TYPE)?.let(HunterType::valueOf) ?: p.hunterType,
                weapons = savedState.get<ArrayList<String>>(KEY_WEAPONS)?.map(Weapon::valueOf)?.toSet() ?: p.weapons,
                province = savedState.get<String>(KEY_PROVINCE)?.let(Province::valueOf) ?: p.homeProvince,
                loading = false,
            )
        }
    }

    fun chooseHunterType(type: HunterType) = update { it.copy(hunterType = type) }

    fun toggleWeapon(weapon: Weapon) = update {
        it.copy(weapons = if (weapon in it.weapons) it.weapons - weapon else it.weapons + weapon)
    }

    fun chooseProvince(province: Province) = update { it.copy(province = province) }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        if (!state.canSave || saving) return
        saving = true
        viewModelScope.launch {
            when (field) {
                ProfileField.HUNTER_TYPE -> userData.setHunterType(state.hunterType!!)
                ProfileField.WEAPONS -> userData.setWeapons(state.weapons)
                ProfileField.HOME_PROVINCE -> userData.setHomeProvince(state.province!!)
            }
            onSaved()
        }
    }

    private fun update(transform: (ProfileEditUiState) -> ProfileEditUiState) {
        if (_uiState.value.loading) return
        _uiState.update(transform)
        val state = _uiState.value
        savedState[KEY_TYPE] = state.hunterType?.name
        savedState[KEY_WEAPONS] = ArrayList(state.weapons.map { it.name })
        savedState[KEY_PROVINCE] = state.province?.name
    }

    private companion object {
        const val KEY_TYPE = "hunterType"
        const val KEY_WEAPONS = "weapons"
        const val KEY_PROVINCE = "province"
    }
}
