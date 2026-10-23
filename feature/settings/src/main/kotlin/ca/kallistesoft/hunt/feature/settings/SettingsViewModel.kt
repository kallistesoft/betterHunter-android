package ca.kallistesoft.hunt.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.ads.AdConsent
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.Weapon
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    /** New hunter from onboarding (P11): season start and reminders. */
    val fieldHunter: Boolean = false,
    val seasonStart: Long? = null,
    /** Each switch is on only while notifications can come (the saved choice and [notificationsBlocked]). */
    val dailyReminders: Boolean = false,
    val seasonReminders: Boolean = false,
    /** A reminder is on but notifications were turned off in system settings: none can come. */
    val notificationsBlocked: Boolean = false,
    /** When reminders come, in minutes after midnight. */
    val reminderMinute: Int = UserProgress.DEFAULT_PLAY_MINUTE,
    /** The ads consent can be changed (the user's region requires it): "Privacy options" shows. */
    val privacyOptions: Boolean = false,
) {
    /** Some provinces aren't added yet: My provinces invites to add their quizzes. */
    val canAddProvinces: Boolean get() = provinces.size < Province.entries.size
}

/** Settings: the profile from onboarding, the app's language and daily goal, and About. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userData: UserDataRepository,
    private val today: Today,
    private val adConsent: AdConsent,
) : ViewModel() {
    /** Whether notifications can come, checked by the screen each time it resumes (they can be turned off outside). */
    private val notificationsAllowed = MutableStateFlow(true)

    val uiState: StateFlow<SettingsUiState> = combine(
        userData.progress,
        notificationsAllowed,
        adConsent.privacyOptionsRequired,
    ) { p, allowed, privacyOptions ->
        SettingsUiState(
            p.hunterType, p.weapons, p.homeProvince, p.provinces, p.dailyGoal,
            loading = false,
            fieldHunter = p.isFieldHunter,
            seasonStart = p.seasonStart?.takeIf { it >= today() },
            dailyReminders = p.dailyReminders && allowed,
            seasonReminders = p.seasonReminders && allowed,
            notificationsBlocked = !allowed && (p.dailyReminders || p.seasonReminders),
            reminderMinute = p.reminderMinute,
            privacyOptions = privacyOptions,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setNotificationsAllowed(allowed: Boolean) {
        notificationsAllowed.value = allowed
    }

    fun setDailyReminders(enabled: Boolean) {
        viewModelScope.launch { userData.setDailyReminders(enabled) }
    }

    fun setSeasonReminders(enabled: Boolean) {
        viewModelScope.launch { userData.setSeasonReminders(enabled) }
    }

    /** The ads consent form again (UMP), over Settings. */
    fun showPrivacyOptions() = adConsent.showPrivacyOptions()

    fun setDailyGoal(goal: Int) {
        viewModelScope.launch { userData.setDailyGoal(goal) }
    }
}
