package ca.kallistesoft.hunt.feature.home

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
class HomeScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun homeLightEn() = compose.captureScreenshot("home_light_en") { HomePreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun homeDarkFr() = compose.captureScreenshot("home_dark_fr") { HomePreview() }

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun homeAnsweredLightEn() = compose.captureScreenshot("home_answered_light_en") { HomeAnsweredPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun homeAnsweredDarkFr() = compose.captureScreenshot("home_answered_dark_fr") { HomeAnsweredPreview() }

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun homeHunterPassedLightEn() = compose.captureScreenshot("home_hunter_passed_light_en") { HomeHunterPassedPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun homeHunterPassedDarkFr() = compose.captureScreenshot("home_hunter_passed_dark_fr") { HomeHunterPassedPreview() }

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun photoOfDayAnsweredLightEn() = compose.captureScreenshot("photo_of_day_answered_light_en") { PhotoOfDayAnsweredPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun photoOfDayAnsweredDarkFr() = compose.captureScreenshot("photo_of_day_answered_dark_fr") { PhotoOfDayAnsweredPreview() }
}
