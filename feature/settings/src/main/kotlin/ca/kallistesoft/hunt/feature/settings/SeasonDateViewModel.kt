package ca.kallistesoft.hunt.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.data.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
) : ViewModel() {
    private val _uiState = MutableStateFlow(SeasonDateUiState())

    /** Today and the saved date are read when the screen starts collecting; picks then stay until saved. */
    val uiState: StateFlow<SeasonDateUiState> = _uiState.asStateFlow()
        .onStart { if (_uiState.value.loading) load() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SeasonDateUiState())

    private suspend fun load() {
        val day = today()
        val saved = userData.progress.first().seasonStart?.takeIf { it >= day }
        _uiState.update { it.copy(loading = false, today = day, picked = saved) }
    }

    fun pick(day: Long) {
        _uiState.update { if (day >= it.today && day <= it.today + MAX_DAYS_AHEAD) it.copy(picked = day) else it }
    }

    fun save() {
        val day = _uiState.value.picked ?: return
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
    }
}
