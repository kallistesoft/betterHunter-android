package ca.kallistesoft.hunt.feature.onboarding

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
class OnboardingScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun hunterTypeLightEn() = compose.captureScreenshot("onboarding_hunter_type_light_en") { HunterTypePreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun hunterTypeDarkFr() = compose.captureScreenshot("onboarding_hunter_type_dark_fr") { HunterTypePreview() }

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun huntWithLightEn() = compose.captureScreenshot("onboarding_hunt_with_light_en") { HuntWithPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun huntWithDarkFr() = compose.captureScreenshot("onboarding_hunt_with_dark_fr") { HuntWithPreview() }

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun huntWhereLightEn() = compose.captureScreenshot("onboarding_hunt_where_light_en") { HuntWherePreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun huntWhereDarkFr() = compose.captureScreenshot("onboarding_hunt_where_dark_fr") { HuntWherePreview() }
}
