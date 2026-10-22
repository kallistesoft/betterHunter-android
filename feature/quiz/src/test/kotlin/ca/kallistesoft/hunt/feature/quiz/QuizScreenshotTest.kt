package ca.kallistesoft.hunt.feature.quiz

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
class QuizScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun quizPracticeFeedbackLightEn() = compose.captureScreenshot("quiz_practice_feedback_light_en") { QuizPracticeFeedbackPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun quizPracticeFeedbackDarkFr() = compose.captureScreenshot("quiz_practice_feedback_dark_fr") { QuizPracticeFeedbackPreview() }

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun quizExamLightEn() = compose.captureScreenshot("quiz_exam_light_en") { QuizExamPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun quizExamDarkFr() = compose.captureScreenshot("quiz_exam_dark_fr") { QuizExamPreview() }

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun quizResultFailedLightEn() = compose.captureScreenshot("quiz_result_failed_light_en") { QuizResultFailedPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun quizResultFailedDarkFr() = compose.captureScreenshot("quiz_result_failed_dark_fr") { QuizResultFailedPreview() }

    @Test
    @Config(qualifiers = LIGHT_EN)
    fun quizResultPassedLightEn() = compose.captureScreenshot("quiz_result_passed_light_en") { QuizResultPassedPreview() }

    @Test
    @Config(qualifiers = DARK_FR)
    fun quizResultPassedDarkFr() = compose.captureScreenshot("quiz_result_passed_dark_fr") { QuizResultPassedPreview() }
}
