package ca.kallistesoft.hunt.feature.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Lang
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.Weapon
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The three onboarding questions answered on screen, with the top bar's back and the system back. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK], qualifiers = "en-w411dp-h2400dp")
class OnboardingScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val userData = FakeUserDataRepository()
    private val languages = mutableListOf<Lang>()

    private fun setScreen(): OnboardingViewModel {
        val viewModel = OnboardingViewModel(userData, SavedStateHandle())
        compose.setContent {
            BetterHunterTheme { OnboardingScreen(onSetLanguage = { languages += it }, viewModel = viewModel) }
        }
        return viewModel
    }

    private fun systemBack() {
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
    }

    @Test
    fun `answering the three questions saves the profile`() {
        val viewModel = setScreen()
        compose.onNodeWithTag("toggle-FR").performClick()
        assertEquals(listOf(Lang.FR), languages)

        compose.onNodeWithTag("onboarding-continue").assertIsNotEnabled()
        compose.onNodeWithTag("choice-${HunterType.NEW_HUNTER.name}").performClick().assertIsSelected()
        compose.onNodeWithTag("onboarding-continue").assertIsEnabled().performClick()

        compose.onNodeWithTag("choice-${Weapon.FIREARM.name}").assertIsOn()
        compose.onNodeWithTag("choice-${Weapon.BOW.name}").performClick()
        compose.onNodeWithTag("onboarding-continue").performClick()

        compose.onNodeWithTag("onboarding-continue").assertIsNotEnabled()
        compose.onNodeWithTag("choice-${Province.QC.name}").performClick()
        compose.onNodeWithTag("onboarding-continue").performClick()
        compose.waitForIdle()

        assertEquals(OnboardingStep.PROVINCE, viewModel.uiState.value.step)
        val saved = userData.progress.value
        assertEquals(HunterType.NEW_HUNTER, saved.hunterType)
        assertEquals(setOf(Weapon.FIREARM, Weapon.BOW), saved.weapons)
        assertEquals(Province.QC, saved.homeProvince)
    }

    @Test
    fun `back goes to the previous question, from the top bar or the system`() {
        val viewModel = setScreen()
        compose.onNodeWithTag("choice-${HunterType.WORKING_ON_IT.name}").performClick()
        compose.onNodeWithTag("onboarding-continue").performClick()
        compose.onNodeWithTag("onboarding-continue").performClick()
        assertEquals(OnboardingStep.PROVINCE, viewModel.uiState.value.step)

        compose.onNodeWithContentDescription("Back").performClick()
        compose.waitForIdle()
        assertEquals(OnboardingStep.WEAPONS, viewModel.uiState.value.step)

        systemBack()
        compose.waitForIdle()
        assertEquals(OnboardingStep.HUNTER_TYPE, viewModel.uiState.value.step)
        compose.onNodeWithTag("choice-${HunterType.WORKING_ON_IT.name}").assertIsSelected()
        assertNull(userData.progress.value.hunterType)
    }
}
