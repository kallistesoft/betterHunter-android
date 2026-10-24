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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
    /** The answer is saved: the screen closes. */
    val saved: Boolean = false,
) {
    val canSave: Boolean
        get() = !loading && when (this.field) {
            ProfileField.HUNTER_TYPE -> hunterType != null
            ProfileField.WEAPONS -> weapons.isNotEmpty()
            ProfileField.HOME_PROVINCE -> province != null
        }
}

/** What the user picked on the edit screen but hasn't saved; null fields show the saved answer. */
private data class Draft(val hunterType: HunterType? = null, val weapons: Set<Weapon>? = null, val province: Province? = null)

/**
 * Changes one onboarding answer from Settings: the saved answer combined with the user's unsaved pick, saved only on
 * "Save". The pick survives rotation and process death until then.
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

    private val draft = MutableStateFlow(
        Draft(
            hunterType = savedState.get<String>(KEY_TYPE)?.let(HunterType::fromName),
            weapons = savedState.get<ArrayList<String>>(KEY_WEAPONS)?.map(Weapon::valueOf)?.toSet(),
            province = savedState.get<String>(KEY_PROVINCE)?.let(Province::valueOf),
        ),
    )

    private val saved = MutableStateFlow(false)

    val uiState: StateFlow<ProfileEditUiState> = combine(userData.progress, draft, saved) { stored, pick, done ->
        ProfileEditUiState(
            field = field,
            hunterType = pick.hunterType ?: stored.hunterType,
            weapons = pick.weapons ?: stored.weapons,
            province = pick.province ?: stored.homeProvince,
            loading = false,
            saved = done,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileEditUiState(field))

    private var saving = false

    fun chooseHunterType(type: HunterType) = edit { it.copy(hunterType = type) }

    fun toggleWeapon(weapon: Weapon) = edit { d ->
        val current = uiState.value.weapons
        d.copy(weapons = if (weapon in current) current - weapon else current + weapon)
    }

    fun chooseProvince(province: Province) = edit { it.copy(province = province) }

    /** Saves the pick, then sets [ProfileEditUiState.saved] (the screen navigates on it, so a rotation can't lose it). */
    fun save() {
        val state = uiState.value
        if (!state.canSave || saving) return
        saving = true
        viewModelScope.launch {
            when (field) {
                ProfileField.HUNTER_TYPE -> userData.setHunterType(state.hunterType!!)
                ProfileField.WEAPONS -> userData.setWeapons(state.weapons)
                ProfileField.HOME_PROVINCE -> userData.setHomeProvince(state.province!!)
            }
            saved.value = true
        }
    }

    /** Ignored until the saved answer is shown: an edit always starts from it. */
    private fun edit(transform: (Draft) -> Draft) {
        if (uiState.value.loading) return
        draft.update(transform)
        val pick = draft.value
        savedState[KEY_TYPE] = pick.hunterType?.name
        savedState[KEY_WEAPONS] = pick.weapons?.let { w -> ArrayList(w.map { it.name }) }
        savedState[KEY_PROVINCE] = pick.province?.name
    }

    private companion object {
        const val KEY_TYPE = "hunterType"
        const val KEY_WEAPONS = "weapons"
        const val KEY_PROVINCE = "province"
    }
}
