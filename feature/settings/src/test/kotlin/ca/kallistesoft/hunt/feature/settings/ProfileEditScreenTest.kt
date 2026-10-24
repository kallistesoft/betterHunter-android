package ca.kallistesoft.hunt.feature.settings

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.Weapon
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Each onboarding question reopened from Settings: the saved answer selected, saved only on "Save". */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK], qualifiers = "en-w411dp-h2400dp")
class ProfileEditScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val userData = FakeUserDataRepository(
        UserProgress().withProfile(HunterType.WORKING_ON_IT, setOf(Weapon.FIREARM), Province.QC),
    )
    private var done = 0

    private fun setScreen(field: ProfileField) {
        val viewModel = ProfileEditViewModel(field, userData, SavedStateHandle())
        compose.setContent {
            BetterHunterTheme { ProfileEditScreen(field, onDone = { done++ }, viewModel = viewModel) }
        }
    }

    @Test
    fun `hunter type`() {
        setScreen(ProfileField.HUNTER_TYPE)
        compose.onNodeWithTag("choice-${HunterType.WORKING_ON_IT.name}").assertIsSelected()
        compose.onNodeWithTag("choice-${HunterType.HUNTER.name}").performClick().assertIsSelected()
        assertEquals(HunterType.WORKING_ON_IT, userData.progress.value.hunterType)

        compose.onNodeWithTag("profile-save").performClick()
        compose.waitForIdle()
        assertEquals(HunterType.HUNTER, userData.progress.value.hunterType)
        assertEquals(1, done)
    }

    @Test
    fun `weapons, at least one`() {
        setScreen(ProfileField.WEAPONS)
        compose.onNodeWithTag("choice-${Weapon.FIREARM.name}").assertIsOn().performClick()
        compose.onNodeWithTag("profile-save").assertIsNotEnabled()
        compose.onNodeWithTag("choice-${Weapon.BOW.name}").performClick()
        compose.onNodeWithTag("profile-save").performClick()
        compose.waitForIdle()
        assertEquals(setOf(Weapon.BOW), userData.progress.value.weapons)
        assertEquals(1, done)
    }

    @Test
    fun `home province, or back without saving`() {
        setScreen(ProfileField.HOME_PROVINCE)
        compose.onNodeWithTag("choice-${Province.QC.name}").assertIsSelected()
        compose.onNodeWithTag("choice-${Province.AB.name}").performClick()
        compose.onNodeWithContentDescription("Back").performClick()
        assertEquals(Province.QC, userData.progress.value.homeProvince)
        assertEquals(1, done)

        compose.onNodeWithTag("profile-save").performClick()
        compose.waitForIdle()
        assertEquals(Province.AB, userData.progress.value.homeProvince)
        assertEquals(2, done)
    }

    @Test
    fun `nothing shows while loading`() {
        compose.setContent {
            BetterHunterTheme { ProfileEditContent(ProfileEditUiState(ProfileField.HUNTER_TYPE), {}, {}, {}, {}, {}) }
        }
        compose.onNodeWithTag("choice-${HunterType.HUNTER.name}").assertDoesNotExist()
        compose.onNodeWithTag("profile-save").assertIsNotEnabled()
    }
}
