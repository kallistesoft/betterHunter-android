package ca.kallistesoft.hunt.core.ui.preview

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
class UiScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun examCardLightEn() = compose.captureScreenshot("exam_card_light_en") { ExamCardPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun examCardDarkFr() = compose.captureScreenshot("exam_card_dark_fr") { ExamCardPreview() }

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun examListItemLightEn() = compose.captureScreenshot("exam_list_item_light_en") { ExamListItemPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun examListItemDarkFr() = compose.captureScreenshot("exam_list_item_dark_fr") { ExamListItemPreview() }

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun questionPhotoLightEn() = compose.captureScreenshot("question_photo_light_en") { QuestionPhotoPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun questionPhotoDarkFr() = compose.captureScreenshot("question_photo_dark_fr") { QuestionPhotoPreview() }
}
