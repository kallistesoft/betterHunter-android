package ca.kallistesoft.hunt.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import ca.kallistesoft.hunt.core.designsystem.component.BottomActionBar
import ca.kallistesoft.hunt.core.designsystem.component.ChoiceMode
import ca.kallistesoft.hunt.core.designsystem.component.PrimaryButton
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Lang
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.Weapon
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.BrandTopAppBar
import ca.kallistesoft.hunt.core.ui.ChoiceQuestion
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.choiceOption

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
                OnboardingStep.HUNTER_TYPE -> ChoiceQuestion(
                    padding,
                    overline = R.string.onboarding_welcome,
                    title = R.string.hunter_type_title,
                    subtitle = R.string.hunter_type_sub,
                    options = HunterType.entries,
                    selected = { it == state.hunterType },
                    mode = ChoiceMode.Single,
                    onClick = onChooseHunterType,
                    option = HunterType::choiceOption,
                )
                OnboardingStep.WEAPONS -> ChoiceQuestion(
                    padding,
                    overline = R.string.onboarding_next_question,
                    title = R.string.hunt_with_title,
                    subtitle = R.string.hunt_with_sub,
                    options = Weapon.entries,
                    selected = { it in state.weapons },
                    mode = ChoiceMode.Multiple,
                    onClick = onToggleWeapon,
                    option = Weapon::choiceOption,
                )
                OnboardingStep.PROVINCE -> ChoiceQuestion(
                    padding,
                    overline = R.string.onboarding_last_question,
                    title = R.string.hunt_where_title,
                    subtitle = R.string.hunt_where_sub,
                    options = Province.entries,
                    selected = { it == state.province },
                    mode = ChoiceMode.Single,
                    onClick = onChooseProvince,
                    option = Province::choiceOption,
                    note = R.string.hunt_where_note,
                )
            }
        }
    }
}
