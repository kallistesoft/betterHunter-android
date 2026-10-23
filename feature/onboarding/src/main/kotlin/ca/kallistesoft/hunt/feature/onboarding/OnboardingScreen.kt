package ca.kallistesoft.hunt.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.designsystem.component.BottomActionBar
import ca.kallistesoft.hunt.core.designsystem.component.ChoiceCard
import ca.kallistesoft.hunt.core.designsystem.component.ChoiceMode
import ca.kallistesoft.hunt.core.designsystem.component.Overline
import ca.kallistesoft.hunt.core.designsystem.component.PrimaryButton
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Lang
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.Weapon
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.BrandTopAppBar
import ca.kallistesoft.hunt.core.ui.HuntIcons
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.icon
import ca.kallistesoft.hunt.core.ui.nameRes
import ca.kallistesoft.hunt.core.ui.quizzesRes

/**
 * First launch, after the splash: "What kind of hunter are you?", "What do you hunt with?", "Where do you hunt?".
 * Saving the last answer ends onboarding; the app then shows Home.
 */
@Composable
fun OnboardingScreen(onSetLanguage: (Lang) -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(enabled = state.step != OnboardingStep.HUNTER_TYPE) { viewModel.back() }
    OnboardingContent(
        state = state,
        onSetLanguage = onSetLanguage,
        onChooseHunterType = viewModel::chooseHunterType,
        onToggleWeapon = viewModel::toggleWeapon,
        onChooseProvince = viewModel::chooseProvince,
        onContinue = viewModel::next,
        onBack = { viewModel.back() },
    )
}

@Composable
internal fun OnboardingContent(
    state: OnboardingUiState,
    onSetLanguage: (Lang) -> Unit,
    onChooseHunterType: (HunterType) -> Unit,
    onToggleWeapon: (Weapon) -> Unit,
    onChooseProvince: (Province) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    val first = state.step == OnboardingStep.HUNTER_TYPE
    Scaffold(
        topBar = { if (first) BrandTopAppBar(onSetLanguage) else BackTopAppBar("", onBack) },
        bottomBar = {
            BottomActionBar(
                below = if (first) {
                    {
                        Text(
                            stringResource(R.string.onboarding_local),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    null
                },
            ) {
                PrimaryButton(
                    stringResource(R.string.action_continue),
                    onClick = onContinue,
                    enabled = state.canContinue,
                    modifier = Modifier.weight(1f).testTag("onboarding-continue"),
                )
            }
        },
    ) { padding ->
        // Forward slides in from the end, back from the start.
        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                slideInHorizontally { if (forward) it else -it } togetherWith slideOutHorizontally { if (forward) -it else it }
            },
            label = "onboarding step",
        ) { step ->
            when (step) {
                OnboardingStep.HUNTER_TYPE -> Question(
                    padding,
                    overline = R.string.onboarding_welcome,
                    title = R.string.hunter_type_title,
                    subtitle = R.string.hunter_type_sub,
                    options = HunterType.entries,
                    selected = { it == state.hunterType },
                    mode = ChoiceMode.Single,
                    onClick = onChooseHunterType,
                    option = HunterType::option,
                )
                OnboardingStep.WEAPONS -> Question(
                    padding,
                    overline = R.string.onboarding_next_question,
                    title = R.string.hunt_with_title,
                    subtitle = R.string.hunt_with_sub,
                    options = Weapon.entries,
                    selected = { it in state.weapons },
                    mode = ChoiceMode.Multiple,
                    onClick = onToggleWeapon,
                    option = Weapon::option,
                )
                OnboardingStep.PROVINCE -> Question(
                    padding,
                    overline = R.string.onboarding_last_question,
                    title = R.string.hunt_where_title,
                    subtitle = R.string.hunt_where_sub,
                    options = Province.entries,
                    selected = { it == state.province },
                    mode = ChoiceMode.Single,
                    onClick = onChooseProvince,
                    option = { Option(it.icon, it.nameRes, it.quizzesRes) },
                    note = R.string.hunt_where_note,
                )
            }
        }
    }
}

/** How a choice is shown: its icon, name and a line about it. */
private data class Option(val icon: ImageVector, @StringRes val title: Int, @StringRes val subtitle: Int)

private fun HunterType.option(): Option = when (this) {
    HunterType.WORKING_ON_IT -> Option(Icons.AutoMirrored.Outlined.MenuBook, R.string.hunter_working, R.string.hunter_working_sub)
    HunterType.NEW_HUNTER -> Option(Icons.Outlined.Eco, R.string.hunter_new, R.string.hunter_new_sub)
    HunterType.EXPERIENCED -> Option(HuntIcons.Antlers, R.string.hunter_experienced, R.string.hunter_experienced_sub)
}

private fun Weapon.option(): Option = when (this) {
    Weapon.FIREARM -> Option(Icons.Outlined.GpsFixed, R.string.weapon_firearm, R.string.weapon_firearm_sub)
    Weapon.BOW -> Option(HuntIcons.Bow, R.string.weapon_bow, R.string.weapon_bow_sub)
    Weapon.CROSSBOW -> Option(HuntIcons.Crossbow, R.string.weapon_crossbow, R.string.weapon_crossbow_sub)
}

/** One question: an overline, the question, a hint, the choices and an optional [note] under them. */
@Composable
private fun <T : Enum<T>> Question(
    padding: PaddingValues,
    @StringRes overline: Int,
    @StringRes title: Int,
    @StringRes subtitle: Int,
    options: List<T>,
    selected: (T) -> Boolean,
    mode: ChoiceMode,
    onClick: (T) -> Unit,
    option: (T) -> Option,
    @StringRes note: Int? = null,
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = padding.plus(top = Spacing.s),
        verticalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        item {
            Column(Modifier.padding(bottom = Spacing.xs), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                Overline(stringResource(overline))
                Text(
                    stringResource(title),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() },
                )
                Text(stringResource(subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Column(
                Modifier.then(if (mode == ChoiceMode.Single) Modifier.selectableGroup() else Modifier),
                verticalArrangement = Arrangement.spacedBy(Spacing.m),
            ) {
                options.forEach { value ->
                    val o = option(value)
                    ChoiceCard(
                        icon = o.icon,
                        title = stringResource(o.title),
                        subtitle = stringResource(o.subtitle),
                        selected = selected(value),
                        mode = mode,
                        onClick = { onClick(value) },
                        modifier = Modifier.fillMaxWidth().testTag("choice-${value.name}"),
                    )
                }
            }
        }
        if (note != null) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val muted = MaterialTheme.colorScheme.onSurfaceVariant
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = muted, modifier = Modifier.size(Sizes.iconS))
                    Spacer(Modifier.width(Spacing.s))
                    Text(stringResource(note), style = MaterialTheme.typography.bodySmall, color = muted)
                }
            }
        }
    }
}
