package ca.kallistesoft.hunt.feature.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.data.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeasonDateUiState(
    val loading: Boolean = true,
    val today: Long = 0,
    /** The day picked (or saved before), null until one is picked. */
    val picked: Long? = null,
    /** Saved: "Remind me before my season?" shows, unless season reminders are already on. */
    val askReminders: Boolean = false,
    /** Nothing left to do: the screen closes. */
    val done: Boolean = false,
)

/** "When does your season start?": the hunter's own first hunting day (the app doesn't know legal dates, P11). */
@HiltViewModel
class SeasonDateViewModel @Inject constructor(
    private val userData: UserDataRepository,
    private val today: Today,
    savedState: SavedStateHandle,
) : ViewModel() {
    /** Everything but the picked day, which is in [picked]. */
    private val _uiState = MutableStateFlow(SeasonDateUiState())

    /** The day shown as picked: in the saved state, so an unsaved pick survives process death. */
    private val picked = savedState.getMutableStateFlow<Long?>(KEY_PICKED, null)

    /** Today and the saved date are read when the screen starts collecting; picks then stay until saved. */
    val uiState: StateFlow<SeasonDateUiState> = combine(_uiState, picked) { state, day -> state.copy(picked = day) }
        .onStart { if (_uiState.value.loading) load() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SeasonDateUiState())

    private suspend fun load() {
        val day = today()
        val saved = userData.progress.first().seasonStart?.takeIf { it >= day }
        // A pick restored after process death wins over the saved date, unless it's now in the past.
        picked.update { pick -> pick?.takeIf { it >= day } ?: saved }
        _uiState.update { it.copy(loading = false, today = day) }
    }

    fun pick(day: Long) {
        val first = _uiState.value.today
        if (day >= first && day <= first + MAX_DAYS_AHEAD) picked.value = day
    }

    fun save() {
        val day = picked.value ?: return
        viewModelScope.launch {
            userData.setSeasonStart(day)
            val remindersOn = userData.progress.first().seasonReminders
            _uiState.update { if (remindersOn) it.copy(done = true) else it.copy(askReminders = true) }
        }
    }

    /** "I don't know yet": no date; Home says "Hunting season is coming" from late August. */
    fun dontKnow() {
        viewModelScope.launch {
            userData.setSeasonStart(null)
            _uiState.update { it.copy(done = true) }
        }
    }

    /** "Remind me" (after the system dialog) or "No thanks". */
    fun answerReminders(enabled: Boolean) {
        viewModelScope.launch {
            userData.setSeasonReminders(enabled)
            _uiState.update { it.copy(done = true) }
        }
    }

    companion object {
        /** The date can be picked up to a year ahead. */
        const val MAX_DAYS_AHEAD = 365L

        private const val KEY_PICKED = "picked"
    }
}
