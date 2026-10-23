package ca.kallistesoft.hunt.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.designsystem.component.PillToggle
import ca.kallistesoft.hunt.core.designsystem.component.RowEnd
import ca.kallistesoft.hunt.core.designsystem.component.SectionTitle
import ca.kallistesoft.hunt.core.designsystem.component.SettingRow
import ca.kallistesoft.hunt.core.designsystem.component.SettingsCard
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.Lang
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.CONTACT_EMAIL
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.choiceOption
import ca.kallistesoft.hunt.core.ui.currentLang
import ca.kallistesoft.hunt.core.ui.icon
import ca.kallistesoft.hunt.core.ui.longDate
import ca.kallistesoft.hunt.core.ui.nameRes
import ca.kallistesoft.hunt.core.ui.rememberLinkOpener
import ca.kallistesoft.hunt.core.ui.notificationsEnabled
import ca.kallistesoft.hunt.core.ui.rememberNotificationPermission
import ca.kallistesoft.hunt.core.ui.rememberNotificationSettingsOpener
import ca.kallistesoft.hunt.core.ui.shortDate
import ca.kallistesoft.hunt.core.ui.timeOfDay

/** Settings, from the gear on Home: your profile, the app (language, daily goal) and About. */
@Composable
fun SettingsScreen(
    versionName: String,
    onBack: () -> Unit,
    onEdit: (ProfileField) -> Unit,
    onOpenProvinces: () -> Unit,
    onOpenAbout: () -> Unit,
    onSetLanguage: (Lang) -> Unit,
    onOpenSeasonDate: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val openNotificationSettings = rememberNotificationSettingsOpener()
    // Refused (Android stops asking after two refusals): the choice is kept, and the system settings can allow it.
    val askDaily = rememberNotificationPermission { granted ->
        viewModel.setDailyReminders(true)
        if (!granted) openNotificationSettings()
    }
    val askSeason = rememberNotificationPermission { granted ->
        viewModel.setSeasonReminders(true)
        if (!granted) openNotificationSettings()
    }
    val context = LocalContext.current
    // Notifications can be turned off in system settings while the app is away: check on each return.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.setNotificationsAllowed(notificationsEnabled(context)) }
    val links = rememberLinkOpener()
    val privacyUrl = stringResource(R.string.privacy_url)
    SettingsContent(
        state = state,
        versionName = versionName,
        onBack = onBack,
        onEdit = onEdit,
        onOpenProvinces = onOpenProvinces,
        onOpenAbout = onOpenAbout,
        onSetLanguage = onSetLanguage,
        onSetDailyGoal = viewModel::setDailyGoal,
        onOpenSeasonDate = onOpenSeasonDate,
        // Turning a reminder on asks for the notification permission first (or, if notifications were turned off
        // in system settings, opens them: asking again would be refused at once); off needs nothing.
        onDailyReminders = { on ->
            when {
                !on -> viewModel.setDailyReminders(false)
                state.notificationsBlocked -> openNotificationSettings()
                else -> askDaily()
            }
        },
        onSeasonReminders = { on ->
            when {
                !on -> viewModel.setSeasonReminders(false)
                state.notificationsBlocked -> openNotificationSettings()
                else -> askSeason()
            }
        },
        onOpenNotificationSettings = openNotificationSettings,
        onContact = { links.email(CONTACT_EMAIL) },
        onPrivacy = { links.open(privacyUrl) },
        onPrivacyOptions = viewModel::showPrivacyOptions,
    )
}

@Composable
internal fun SettingsContent(
    state: SettingsUiState,
    versionName: String,
    onBack: () -> Unit,
    onEdit: (ProfileField) -> Unit,
    onOpenProvinces: () -> Unit,
    onOpenAbout: () -> Unit,
    onSetLanguage: (Lang) -> Unit,
    onSetDailyGoal: (Int) -> Unit,
    onContact: () -> Unit,
    onPrivacy: () -> Unit,
    onOpenSeasonDate: () -> Unit = {},
    onDailyReminders: (Boolean) -> Unit = {},
    onSeasonReminders: (Boolean) -> Unit = {},
    onOpenNotificationSettings: () -> Unit = {},
    onPrivacyOptions: () -> Unit = {},
) {
    Scaffold(
        topBar = { BackTopAppBar(stringResource(R.string.settings), onBack) },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        if (state.loading) return@Scaffold
        LazyColumn(
            Modifier.consumeWindowInsets(padding),
            contentPadding = padding.plus(),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            item { SectionTitle(stringResource(R.string.your_profile)) }
            item { ProfileCard(state, onEdit, onOpenProvinces, onOpenSeasonDate) }
            if (state.fieldHunter) {
                item { SectionTitle(stringResource(R.string.reminders)) }
                item { RemindersCard(state, onDailyReminders, onSeasonReminders, onOpenNotificationSettings) }
                item {
                    Text(
                        stringResource(R.string.quiet_hours),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = Spacing.s),
                    )
                }
            }
            item { SectionTitle(stringResource(R.string.settings_app)) }
            item { AppCard(state.dailyGoal, onSetLanguage, onSetDailyGoal, showDailyGoal = !state.fieldHunter) }
            item { SectionTitle(stringResource(R.string.about)) }
            item {
                // Ads consent choices, only where the user's region requires a way back to them (UMP).
                val privacyOptionsRow: @Composable ColumnScope.() -> Unit = {
                    SettingRow(
                        Icons.Outlined.PrivacyTip,
                        stringResource(R.string.privacy_options),
                        subtitle = stringResource(R.string.privacy_options_sub),
                        onClick = onPrivacyOptions,
                        modifier = Modifier.testTag("settings-privacy-options"),
                    )
                }
                SettingsCard(
                    rows = listOf<@Composable ColumnScope.() -> Unit>(
                        {
                            SettingRow(
                                Icons.Outlined.Info,
                                stringResource(R.string.about_app),
                                subtitle = stringResource(R.string.about_app_sub),
                                onClick = onOpenAbout,
                                modifier = Modifier.testTag("settings-about"),
                            )
                        },
                        {
                            SettingRow(
                                Icons.Outlined.Email,
                                stringResource(R.string.contact),
                                subtitle = CONTACT_EMAIL,
                                onClick = onContact,
                                end = RowEnd.External,
                            )
                        },
                        {
                            SettingRow(Icons.Outlined.Lock, stringResource(R.string.privacy), onClick = onPrivacy, end = RowEnd.External)
                        },
                    ) + listOfNotNull(privacyOptionsRow.takeIf { state.privacyOptions }),
                )
            }
            item {
                Text(
                    stringResource(R.string.version_line, versionName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().testTag("settings-version"),
                )
            }
        }
    }
}

/** Hunter type, hunting with, where you hunt (each reopens its onboarding question), and My provinces. */
@Composable
private fun ProfileCard(state: SettingsUiState, onEdit: (ProfileField) -> Unit, onOpenProvinces: () -> Unit, onOpenSeasonDate: () -> Unit) {
    val weapons = state.weapons.sortedBy { it.ordinal }.map { stringResource(it.choiceOption().title) }.joinToString(", ")
    val provinces = Province.entries.filter { it in state.provinces }.map { stringResource(it.nameRes) }.joinToString(" · ")
    val seasonRow: @Composable ColumnScope.() -> Unit = {
        SettingRow(
            Icons.Outlined.Event,
            stringResource(R.string.season_start),
            subtitle = state.seasonStart?.let { stringResource(R.string.season_start_set, longDate(it)) }
                ?: stringResource(R.string.season_start_none),
            onClick = onOpenSeasonDate,
            orange = true,
            modifier = Modifier.testTag("settings-season"),
        )
    }
    SettingsCard(
        rows = listOf<@Composable ColumnScope.() -> Unit>(
            {
                SettingRow(
                    Icons.Outlined.PersonOutline,
                    stringResource(R.string.hunter_type),
                    subtitle = state.hunterType?.let { stringResource(it.choiceOption().title) },
                    onClick = { onEdit(ProfileField.HUNTER_TYPE) },
                    modifier = Modifier.testTag("settings-hunter-type"),
                )
            },
            {
                SettingRow(
                    Icons.Outlined.GpsFixed,
                    stringResource(R.string.hunting_with),
                    subtitle = weapons,
                    onClick = { onEdit(ProfileField.WEAPONS) },
                    modifier = Modifier.testTag("settings-weapons"),
                )
            },
            {
                SettingRow(
                    Icons.Outlined.Place,
                    stringResource(R.string.where_you_hunt_title),
                    subtitle = state.homeProvince?.let { stringResource(it.nameRes) },
                    onClick = { onEdit(ProfileField.HOME_PROVINCE) },
                    modifier = Modifier.testTag("settings-home"),
                )
            },
            {
                SettingRow(
                    state.homeProvince?.icon ?: Icons.Outlined.Place,
                    stringResource(R.string.my_provinces),
                    subtitle = if (state.canAddProvinces) stringResource(R.string.my_provinces_add, provinces) else provinces,
                    onClick = onOpenProvinces,
                    orange = true,
                    modifier = Modifier.testTag("settings-provinces"),
                )
            },
        ) + listOfNotNull(seasonRow.takeIf { state.fieldHunter }),
    )
}

/**
 * Daily questions and season reminders, each with its switch (on asks for the notification permission), under a
 * "Notifications are off" row when system settings block them.
 */
@Composable
private fun RemindersCard(
    state: SettingsUiState,
    onDaily: (Boolean) -> Unit,
    onSeason: (Boolean) -> Unit,
    onOpenNotificationSettings: () -> Unit,
) {
    // Reminders are on but notifications were turned off in system settings: say so, with the way back.
    val blockedRow: @Composable ColumnScope.() -> Unit = {
        SettingRow(
            Icons.Outlined.NotificationsOff,
            stringResource(R.string.notifications_blocked),
            subtitle = stringResource(R.string.notifications_blocked_sub),
            onClick = onOpenNotificationSettings,
            orange = true,
            modifier = Modifier.testTag("settings-notifications-blocked"),
        )
    }
    SettingsCard(
        rows = listOfNotNull<@Composable ColumnScope.() -> Unit>(
            blockedRow.takeIf { state.notificationsBlocked },
            {
                SettingRow(
                    Icons.Outlined.NotificationsActive,
                    stringResource(R.string.reminders_daily),
                    subtitle = stringResource(R.string.reminders_daily_sub, timeOfDay(state.reminderMinute)),
                    modifier = Modifier.testTag("settings-daily-reminders")
                        .toggleable(value = state.dailyReminders, role = Role.Switch, onValueChange = onDaily),
                ) { ReminderSwitch(state.dailyReminders) }
            },
            {
                SettingRow(
                    Icons.Outlined.Event,
                    stringResource(R.string.reminders_season),
                    subtitle = state.seasonStart?.let { stringResource(R.string.reminders_season_sub, shortDate(it)) }
                        ?: stringResource(R.string.reminders_season_no_date),
                    modifier = Modifier.testTag("settings-season-reminders")
                        .toggleable(value = state.seasonReminders, role = Role.Switch, onValueChange = onSeason),
                ) { ReminderSwitch(state.seasonReminders) }
            },
        ),
    )
}

@Composable
private fun ReminderSwitch(checked: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Switch(
        checked = checked,
        onCheckedChange = null,
        colors = SwitchDefaults.colors(
            uncheckedThumbColor = scheme.onSurfaceVariant,
            uncheckedBorderColor = scheme.onSurfaceVariant,
            uncheckedTrackColor = scheme.surfaceContainerHighest,
        ),
    )
}

/** Language (the same per-app language as Home's toggle) and the daily goal. */
@Composable
private fun AppCard(dailyGoal: Int, onSetLanguage: (Lang) -> Unit, onSetDailyGoal: (Int) -> Unit, showDailyGoal: Boolean) {
    val goals = UserProgress.DAILY_GOALS
    val goalRow: @Composable ColumnScope.() -> Unit = {
        SettingRow(Icons.Outlined.Schedule, stringResource(R.string.daily_goal), subtitle = stringResource(R.string.daily_goal_sub)) {
            PillToggle(
                options = goals.map { "$it" },
                selected = goals.indexOf(dailyGoal),
                onSelect = { onSetDailyGoal(goals[it]) },
                description = stringResource(R.string.daily_goal),
                orange = true,
                modifier = Modifier.testTag("settings-goal"),
            )
        }
    }
    SettingsCard(
        // New hunters have today's 6 questions instead of a daily goal (P11).
        rows = listOfNotNull<@Composable ColumnScope.() -> Unit>(
            {
                SettingRow(Icons.Outlined.Language, stringResource(R.string.language)) {
                    PillToggle(
                        options = Lang.entries.map { it.code.uppercase() },
                        selected = Lang.entries.indexOf(currentLang()),
                        onSelect = { onSetLanguage(Lang.entries[it]) },
                        description = stringResource(R.string.language),
                    )
                }
            },
            goalRow.takeIf { showDailyGoal },
        ),
    )
}
