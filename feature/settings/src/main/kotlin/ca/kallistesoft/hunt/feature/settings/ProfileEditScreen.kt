package ca.kallistesoft.hunt.feature.settings

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.designsystem.component.BottomActionBar
import ca.kallistesoft.hunt.core.designsystem.component.ChoiceMode
import ca.kallistesoft.hunt.core.designsystem.component.PrimaryButton
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.Weapon
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.ChoiceQuestion
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.choiceOption

/** One onboarding question again, from Settings, with the saved answer selected and "Save" to change it. */
@Composable
fun ProfileEditScreen(
    field: ProfileField,
    onDone: () -> Unit,
    viewModel: ProfileEditViewModel = hiltViewModel<ProfileEditViewModel, ProfileEditViewModel.Factory>(
        creationCallback = { factory -> factory.create(field) },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileEditContent(
        state,
        onChooseHunterType = viewModel::chooseHunterType,
        onToggleWeapon = viewModel::toggleWeapon,
        onChooseProvince = viewModel::chooseProvince,
        onSave = { viewModel.save(onDone) },
        onBack = onDone,
    )
}

@Composable
internal fun ProfileEditContent(
    state: ProfileEditUiState,
    onChooseHunterType: (HunterType) -> Unit,
    onToggleWeapon: (Weapon) -> Unit,
    onChooseProvince: (Province) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = { BackTopAppBar("", onBack) },
        bottomBar = {
            BottomActionBar {
                PrimaryButton(
                    stringResource(R.string.save),
                    onClick = onSave,
                    enabled = state.canSave,
                    modifier = Modifier.weight(1f).testTag("profile-save"),
                )
            }
        },
    ) { padding ->
        if (state.loading) return@Scaffold
        when (state.field) {
            ProfileField.HUNTER_TYPE -> ChoiceQuestion(
                padding,
                overline = R.string.your_profile,
                title = R.string.hunter_type_title,
                subtitle = R.string.hunter_type_sub,
                options = HunterType.entries,
                selected = { it == state.hunterType },
                mode = ChoiceMode.Single,
                onClick = onChooseHunterType,
                option = HunterType::choiceOption,
            )
            ProfileField.WEAPONS -> ChoiceQuestion(
                padding,
                overline = R.string.your_profile,
                title = R.string.hunt_with_title,
                subtitle = R.string.hunt_with_sub,
                options = Weapon.entries,
                selected = { it in state.weapons },
                mode = ChoiceMode.Multiple,
                onClick = onToggleWeapon,
                option = Weapon::choiceOption,
            )
            ProfileField.HOME_PROVINCE -> ChoiceQuestion(
                padding,
                overline = R.string.your_profile,
                title = R.string.hunt_where_title,
                subtitle = R.string.hunt_where_sub,
                options = Province.entries,
                selected = { it == state.province },
                mode = ChoiceMode.Single,
                onClick = onChooseProvince,
                option = Province::choiceOption,
            )
        }
    }
}
