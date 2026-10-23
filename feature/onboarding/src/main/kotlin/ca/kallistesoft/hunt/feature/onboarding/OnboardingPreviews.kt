package ca.kallistesoft.hunt.feature.onboarding

import androidx.compose.runtime.Composable
import ca.kallistesoft.hunt.core.designsystem.component.BhPreviews
import ca.kallistesoft.hunt.core.designsystem.component.PreviewSurface
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province

@Composable
private fun Preview(state: OnboardingUiState) = PreviewSurface {
    OnboardingContent(state, {}, {}, {}, {}, {}, {})
}

@BhPreviews
@Composable
internal fun HunterTypePreview() = Preview(OnboardingUiState(hunterType = HunterType.WORKING_ON_IT))

@BhPreviews
@Composable
internal fun HuntWithPreview() = Preview(OnboardingUiState(OnboardingStep.WEAPONS, HunterType.WORKING_ON_IT))

@BhPreviews
@Composable
internal fun HuntWherePreview() =
    Preview(OnboardingUiState(OnboardingStep.PROVINCE, HunterType.WORKING_ON_IT, province = Province.QC))
