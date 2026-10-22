package ca.kallistesoft.hunt.feature.exams

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
class ExamsScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun examTrainingLightEn() = compose.captureScreenshot("exam_training_light_en") { ExamTrainingPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun examTrainingDarkFr() = compose.captureScreenshot("exam_training_dark_fr") { ExamTrainingPreview() }

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun examPickerLightEn() = compose.captureScreenshot("exam_picker_light_en") { ExamPickerPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun examPickerDarkFr() = compose.captureScreenshot("exam_picker_dark_fr") { ExamPickerPreview() }

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun recognitionLightEn() = compose.captureScreenshot("recognition_light_en") { RecognitionPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun recognitionDarkFr() = compose.captureScreenshot("recognition_dark_fr") { RecognitionPreview() }
}
