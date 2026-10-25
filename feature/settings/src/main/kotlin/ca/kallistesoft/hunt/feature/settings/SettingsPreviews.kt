package ca.kallistesoft.hunt.feature.settings

import androidx.compose.runtime.Composable
import ca.kallistesoft.hunt.core.billing.AdFreeProduct
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

/** A hunter whose reminders are on, with notifications turned off in system settings. */
@BhPreviews
@Composable
internal fun SettingsNotificationsBlockedPreview() = PreviewSurface {
    SettingsContent(
        SettingsUiState(
            hunterType = HunterType.HUNTER,
            weapons = setOf(Weapon.FIREARM),
            homeProvince = Province.QC,
            provinces = setOf(Province.QC),
            loading = false,
            hunter = true,
            notificationsBlocked = true,
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
        ProfileEditUiState(ProfileField.HUNTER_TYPE, hunterType = HunterType.HUNTER, loading = false),
        {}, {}, {}, {}, {},
    )
}

/** Remove ads (P15): both offers with Play's prices. */
@BhPreviews
@Composable
internal fun RemoveAdsPreview() = PreviewSurface {
    RemoveAdsContent(
        RemoveAdsUiState(prices = mapOf(AdFreeProduct.WEEK to "CA$1.99", AdFreeProduct.FOREVER to "CA$14.99"), loading = false),
        onBack = {}, onBuy = {}, onRestore = {}, onDismissMessage = {},
    )
}

/** A 7-day pass running, and a pending payment for forever. */
@BhPreviews
@Composable
internal fun RemoveAdsWeekPreview() = PreviewSurface {
    RemoveAdsContent(
        RemoveAdsUiState(
            prices = mapOf(AdFreeProduct.WEEK to "CA$1.99", AdFreeProduct.FOREVER to "CA$14.99"),
            untilDay = PREVIEW_DAY,
            message = RemoveAdsMessage.PENDING,
            loading = false,
        ),
        onBack = {}, onBuy = {}, onRestore = {}, onDismissMessage = {},
    )
}

/** Forever bought: thanks, and nothing more to sell. */
@BhPreviews
@Composable
internal fun RemoveAdsForeverPreview() = PreviewSurface {
    RemoveAdsContent(RemoveAdsUiState(forever = true, loading = false), onBack = {}, onBuy = {}, onRestore = {}, onDismissMessage = {})
}

/** November 1st, 2026: a fixed day so the screenshots don't change. */
private const val PREVIEW_DAY = 20_758L
