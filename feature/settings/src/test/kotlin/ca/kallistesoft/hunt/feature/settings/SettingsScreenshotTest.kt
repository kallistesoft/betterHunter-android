package ca.kallistesoft.hunt.feature.settings

import androidx.compose.ui.test.junit4.createComposeRule
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice.DARK_FR
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice.LIGHT_EN
import ca.kallistesoft.hunt.core.testing.captureScreenshot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Screenshots of this module's previews, light in English and dark in French (see README, "Screenshot tests"). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [ScreenshotDevice.SDK])
class SettingsScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun settingsLightEn() = compose.captureScreenshot("settings_light_en") { SettingsPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun settingsDarkFr() = compose.captureScreenshot("settings_dark_fr") { SettingsPreview() }

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun settingsNotificationsBlockedLightEn() =
        compose.captureScreenshot("settings_notifications_blocked_light_en") { SettingsNotificationsBlockedPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun settingsNotificationsBlockedDarkFr() =
        compose.captureScreenshot("settings_notifications_blocked_dark_fr") { SettingsNotificationsBlockedPreview() }

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun profileEditLightEn() = compose.captureScreenshot("profile_edit_light_en") { ProfileEditPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun profileEditDarkFr() = compose.captureScreenshot("profile_edit_dark_fr") { ProfileEditPreview() }
}
