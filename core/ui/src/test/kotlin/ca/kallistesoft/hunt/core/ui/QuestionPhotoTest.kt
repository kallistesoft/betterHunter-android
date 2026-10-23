package ca.kallistesoft.hunt.core.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.model.ImageCredit
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import ca.kallistesoft.hunt.core.testing.TestContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** The photo as the app draws it (not in inspection mode, so Coil loads it; the test assets have no such file). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK], qualifiers = ScreenshotDevice.LIGHT_EN)
class QuestionPhotoTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val credit = ImageCredit("A. Photographer", "CC BY 4.0", "https://commons.example/deer")
    private val photo = TestContent.question("id-1", "wildlife-id", image = "wildlife/1.jpg")
        .copy(imageCredit = credit, imageAspect = 1.5f)

    @Test
    fun `the photo opens full screen and closes`() {
        compose.setContent { BetterHunterTheme { QuestionPhoto(photo) } }

        compose.onNodeWithTag("question-photo").performClick()
        val full = compose.onAllNodesWithContentDescription("id-1?").onLast()
        full.assertIsDisplayed()
        full.performTouchInput {
            pinch(center - Offset(10f, 0f), center - Offset(200f, 0f), center + Offset(10f, 0f), center + Offset(200f, 0f))
        }
        full.assertIsDisplayed()

        compose.onNodeWithContentDescription("Close").performClick()
        compose.onNodeWithContentDescription("Close").assertDoesNotExist()
    }

    @Test
    fun `the credit links to the photo's licence page`() {
        compose.setContent { BetterHunterTheme { QuestionPhoto(photo, zoomHint = false) } }

        compose.onNodeWithText("Photo: A. Photographer, CC BY 4.0").performClick()
        assertEquals(credit.page, shadowOf(compose.activity).nextStartedActivity.dataString)
    }

    @Test
    fun `the overlay caption is drawn over the photo`() {
        compose.setContent {
            BetterHunterTheme {
                Column {
                    QuestionPhoto(photo.copy(imageCredit = null, imageAspect = null)) {
                        PhotoCaption(overline = "RIGHT", title = "White-tailed deer", subtitle = "Cerf de Virginie")
                    }
                    QuestionPhoto(photo.copy(id = "no-photo", image = null)) { PhotoCaption("NONE", "Not drawn") }
                    PhotoThumb("wildlife/2.jpg")
                    PrefetchPhoto("wildlife/3.jpg")
                    PrefetchPhoto(null)
                }
            }
        }

        compose.onNodeWithText("RIGHT").assertIsDisplayed()
        compose.onNodeWithText("White-tailed deer").assertIsDisplayed()
        compose.onNodeWithText("Cerf de Virginie").assertIsDisplayed()
        compose.onNodeWithText("Not drawn").assertDoesNotExist()
        compose.onNodeWithText("Photo:", substring = true).assertDoesNotExist()
    }

    @Test
    fun `asset photos are loaded from the APK's assets`() {
        assertEquals("file:///android_asset/wildlife/1.jpg", assetPhoto("wildlife/1.jpg"))
    }
}
