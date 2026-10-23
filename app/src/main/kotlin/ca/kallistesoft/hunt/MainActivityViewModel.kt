package ca.kallistesoft.hunt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.flags.FeatureFlags
import ca.kallistesoft.hunt.core.update.ForceUpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MainUiState {
    /** Keeps the splash screen up until the first screen is known. */
    data object Loading : MainUiState

    data class Ready(
        /** Onboarding not done yet: the app starts with it instead of the dashboard. */
        val firstRun: Boolean,
        /** This build is below the minimum version: only the update screen is shown. */
        val updateRequired: Boolean,
    ) : MainUiState
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userData: UserDataRepository,
    private val forceUpdate: ForceUpdateRepository,
    private val featureFlags: FeatureFlags,
) : ViewModel() {
    val uiState: StateFlow<MainUiState> = combine(userData.progress, forceUpdate.updateRequired) { progress, required ->
        MainUiState.Ready(firstRun = !progress.onboarded, updateRequired = required)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState.Loading)

    /** Fetches the latest minimum version and feature flags; called whenever the app comes to the foreground. */
    fun refreshRemoteConfig() {
        viewModelScope.launch { forceUpdate.refresh() }
        viewModelScope.launch { featureFlags.refresh() }
    }
}
