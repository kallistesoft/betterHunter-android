package ca.kallistesoft.hunt.core.designsystem.component

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
class DesignSystemScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun answerChoiceLightEn() = compose.captureScreenshot("answer_choice_light_en") { AnswerChoicePreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun answerChoiceDarkFr() = compose.captureScreenshot("answer_choice_dark_fr") { AnswerChoicePreview() }

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun cardsLightEn() = compose.captureScreenshot("cards_light_en") { CardsPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun cardsDarkFr() = compose.captureScreenshot("cards_dark_fr") { CardsPreview() }

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun smallComponentsLightEn() = compose.captureScreenshot("small_components_light_en") { SmallComponentsPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun smallComponentsDarkFr() = compose.captureScreenshot("small_components_dark_fr") { SmallComponentsPreview() }
}
