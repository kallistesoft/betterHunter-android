package ca.kallistesoft.hunt.feature.settings

import androidx.compose.runtime.Composable
import ca.kallistesoft.hunt.core.designsystem.component.BhPreviews
import ca.kallistesoft.hunt.core.designsystem.component.PreviewSurface
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.Weapon

@BhPreviews
@Composable
internal fun SettingsPreview() = PreviewSurface {
    SettingsContent(
        SettingsUiState(
            hunterType = HunterType.WORKING_ON_IT,
            weapons = setOf(Weapon.FIREARM),
            homeProvince = Province.QC,
            provinces = setOf(Province.QC),
            loading = false,
        ),
        versionName = "1.0.0",
        onBack = {},
        onEdit = {},
        onOpenProvinces = {},
        onOpenAbout = {},
        onSetLanguage = {},
        onSetDailyGoal = {},
        onContact = {},
        onPrivacy = {},
    )
}

/** Hunter type reopened from Settings, the saved answer selected. */
@BhPreviews
@Composable
internal fun ProfileEditPreview() = PreviewSurface {
    ProfileEditContent(
        ProfileEditUiState(ProfileField.HUNTER_TYPE, hunterType = HunterType.NEW_HUNTER, loading = false),
        {}, {}, {}, {}, {},
    )
}
