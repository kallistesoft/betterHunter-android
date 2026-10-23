package ca.kallistesoft.hunt.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.Weapon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val hunterType: HunterType? = null,
    val weapons: Set<Weapon> = emptySet(),
    val homeProvince: Province? = null,
    val provinces: Set<Province> = emptySet(),
    val dailyGoal: Int = UserProgress.DEFAULT_DAILY_GOAL,
    val loading: Boolean = true,
) {
    /** Some provinces aren't added yet: My provinces invites to add their quizzes. */
    val canAddProvinces: Boolean get() = provinces.size < Province.entries.size
}

/** Settings: the profile from onboarding, the app's language and daily goal, and About. */
@HiltViewModel
class SettingsViewModel @Inject constructor(private val userData: UserDataRepository) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = userData.progress.map { p ->
        SettingsUiState(p.hunterType, p.weapons, p.homeProvince, p.provinces, p.dailyGoal, loading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setDailyGoal(goal: Int) {
        viewModelScope.launch { userData.setDailyGoal(goal) }
    }
}
