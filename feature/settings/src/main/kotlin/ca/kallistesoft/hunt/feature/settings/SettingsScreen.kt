package ca.kallistesoft.hunt.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import ca.kallistesoft.hunt.core.ui.nameRes
import ca.kallistesoft.hunt.core.ui.rememberLinkOpener

/** Settings, from the gear on Home: your profile, the app (language, daily goal) and About. */
@Composable
fun SettingsScreen(
    versionName: String,
    onBack: () -> Unit,
    onEdit: (ProfileField) -> Unit,
    onOpenProvinces: () -> Unit,
    onOpenAbout: () -> Unit,
    onSetLanguage: (Lang) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
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
        onContact = { links.email(CONTACT_EMAIL) },
        onPrivacy = { links.open(privacyUrl) },
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
) {
    Scaffold(topBar = { BackTopAppBar(stringResource(R.string.settings), onBack) }) { padding ->
        if (state.loading) return@Scaffold
        LazyColumn(contentPadding = padding.plus(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            item { SectionTitle(stringResource(R.string.your_profile)) }
            item { ProfileCard(state, onEdit, onOpenProvinces) }
            item { SectionTitle(stringResource(R.string.settings_app)) }
            item { AppCard(state.dailyGoal, onSetLanguage, onSetDailyGoal) }
            item { SectionTitle(stringResource(R.string.about)) }
            item {
                SettingsCard(
                    rows = listOf(
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
                    ),
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
private fun ProfileCard(state: SettingsUiState, onEdit: (ProfileField) -> Unit, onOpenProvinces: () -> Unit) {
    val weapons = state.weapons.sortedBy { it.ordinal }.map { stringResource(it.choiceOption().title) }.joinToString(", ")
    val provinces = Province.entries.filter { it in state.provinces }.map { stringResource(it.nameRes) }.joinToString(" · ")
    SettingsCard(
        rows = listOf(
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
        ),
    )
}

/** Language (the same per-app language as Home's toggle) and the daily goal. */
@Composable
private fun AppCard(dailyGoal: Int, onSetLanguage: (Lang) -> Unit, onSetDailyGoal: (Int) -> Unit) {
    val goals = UserProgress.DAILY_GOALS
    SettingsCard(
        rows = listOf(
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
            {
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
            },
        ),
    )
}
