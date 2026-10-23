package ca.kallistesoft.hunt.feature.settings

import android.Manifest
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Lang
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.Weapon
import ca.kallistesoft.hunt.core.testing.FakeAdConsent
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import ca.kallistesoft.hunt.core.testing.TEST_DAY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Settings rows reach the right callbacks; the screen saves reminders and the goal and opens the links. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK], qualifiers = "en-w411dp-h2400dp")
class SettingsScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val events = mutableListOf<String>()

    private val workingOnIt = SettingsUiState(
        hunterType = HunterType.WORKING_ON_IT,
        weapons = setOf(Weapon.FIREARM, Weapon.BOW),
        homeProvince = Province.QC,
        provinces = setOf(Province.QC),
        loading = false,
    )

    private fun setSettings(state: SettingsUiState) = compose.setContent {
        BetterHunterTheme {
            SettingsContent(
                state,
                versionName = "1.2.3",
                onBack = { events += "back" },
                onEdit = { events += "edit:$it" },
                onOpenProvinces = { events += "provinces" },
                onOpenAbout = { events += "about" },
                onSetLanguage = { events += "lang:$it" },
                onSetDailyGoal = { events += "goal:$it" },
                onContact = { events += "contact" },
                onPrivacy = { events += "privacy" },
                onOpenSeasonDate = { events += "season-date" },
                onDailyReminders = { events += "daily:$it" },
                onSeasonReminders = { events += "season:$it" },
            )
        }
    }

    @Test
    fun `each profile row reopens its question, and the app rows do what they say`() {
        setSettings(workingOnIt)
        compose.onNodeWithTag("settings-version").assertIsDisplayed()
        compose.onNodeWithTag("settings-goal").assertIsDisplayed()
        compose.onNodeWithTag("settings-season").assertDoesNotExist()
        compose.onNodeWithTag("settings-daily-reminders").assertDoesNotExist()

        compose.onNodeWithTag("settings-hunter-type").performClick()
        compose.onNodeWithTag("settings-weapons").performClick()
        compose.onNodeWithTag("settings-home").performClick()
        compose.onNodeWithTag("settings-provinces").performClick()
        compose.onNodeWithTag("toggle-30").performClick()
        compose.onNodeWithTag("toggle-FR").performClick()
        compose.onNodeWithTag("settings-about").performClick()
        compose.onNodeWithText("Contact").performClick()
        compose.onNodeWithText("Privacy policy").performClick()
        compose.onNodeWithContentDescription("Back").performClick()

        assertEquals(
            listOf(
                "edit:${ProfileField.HUNTER_TYPE}", "edit:${ProfileField.WEAPONS}", "edit:${ProfileField.HOME_PROVINCE}",
                "provinces", "goal:30", "lang:${Lang.FR}", "about", "contact", "privacy", "back",
            ),
            events,
        )
    }

    @Test
    fun `a new hunter sets the season start and the reminders instead of a daily goal`() {
        setSettings(
            workingOnIt.copy(
                hunterType = HunterType.NEW_HUNTER,
                fieldHunter = true,
                seasonStart = TEST_DAY + 30,
                dailyReminders = true,
                provinces = Province.entries.toSet(),
            ),
        )
        compose.onNodeWithTag("settings-goal").assertDoesNotExist()
        compose.onNodeWithTag("settings-daily-reminders").assertIsOn()
        compose.onNodeWithTag("settings-season-reminders").assertIsOff()

        compose.onNodeWithTag("settings-season").performClick()
        compose.onNodeWithTag("settings-daily-reminders").performClick()
        compose.onNodeWithTag("settings-season-reminders").performClick()

        assertEquals(listOf("season-date", "daily:false", "season:true"), events)
    }

    @Test
    fun `without a season date, season reminders come in late August`() {
        setSettings(workingOnIt.copy(fieldHunter = true))
        compose.onNodeWithText("Not set").assertIsDisplayed()
        compose.onNodeWithText("Late August, until you set a date").assertIsDisplayed()
        compose.onNodeWithTag("settings-season-reminders").assertIsEnabled().assertIsOff()
    }

    @Test
    fun `nothing shows while loading`() {
        setSettings(SettingsUiState())
        compose.onNodeWithTag("settings-hunter-type").assertDoesNotExist()
    }

    @Test
    fun `the screen turns reminders on and off, changes the goal and opens contact and privacy`() {
        val userData = FakeUserDataRepository(
            UserProgress().withProfile(HunterType.NEW_HUNTER, setOf(Weapon.FIREARM), Province.QC).withSeasonStart(TEST_DAY + 30),
        )
        val viewModel = SettingsViewModel(userData, Today { TEST_DAY }, FakeAdConsent())
        shadowOf(compose.activity.application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        compose.setContent {
            BetterHunterTheme {
                SettingsScreen(
                    versionName = "1.2.3",
                    onBack = {}, onEdit = {}, onOpenProvinces = {}, onOpenAbout = {}, onSetLanguage = {},
                    onOpenSeasonDate = { events += "season-date" },
                    viewModel = viewModel,
                )
            }
        }

        compose.onNodeWithTag("settings-daily-reminders").performClick()
        compose.onNodeWithTag("settings-daily-reminders").assertIsOn()
        assertTrue(userData.progress.value.dailyReminders)
        compose.onNodeWithTag("settings-daily-reminders").performClick()
        compose.onNodeWithTag("settings-daily-reminders").assertIsOff()
        assertFalse(userData.progress.value.dailyReminders)

        compose.onNodeWithTag("settings-season-reminders").performClick()
        compose.onNodeWithTag("settings-season-reminders").assertIsOn()
        assertTrue(userData.progress.value.seasonReminders)
        compose.onNodeWithTag("settings-season-reminders").performClick()
        assertFalse(userData.progress.value.seasonReminders)

        compose.onNodeWithTag("settings-season").performClick()
        assertEquals(listOf("season-date"), events)

        val activity = shadowOf(compose.activity)
        compose.onNodeWithText("Contact").performClick()
        val email = activity.nextStartedActivity
        assertEquals(Intent.ACTION_SENDTO, email.action)
        assertEquals("mailto:contact@kallistesoft.ca", email.dataString)

        compose.onNodeWithText("Privacy policy").performClick()
        val privacy = activity.nextStartedActivity
        assertEquals(Intent.ACTION_VIEW, privacy.action)
        assertTrue(privacy.dataString!!.startsWith("http"))
    }

    @Test
    fun `the screen changes the daily goal`() {
        val userData = FakeUserDataRepository(UserProgress().withProfile(HunterType.WORKING_ON_IT, setOf(Weapon.FIREARM), Province.QC))
        val viewModel = SettingsViewModel(userData, Today { TEST_DAY }, FakeAdConsent())
        compose.setContent {
            BetterHunterTheme {
                SettingsScreen("1.2.3", {}, {}, {}, {}, {}, {}, viewModel)
            }
        }
        compose.onNodeWithTag("toggle-10").performClick()
        compose.waitForIdle()
        assertEquals(10, userData.progress.value.dailyGoal)
    }

    @Test
    fun `privacy options show only where the consent requires them, and open the consent form`() {
        val userData = FakeUserDataRepository(UserProgress().withProfile(HunterType.WORKING_ON_IT, setOf(Weapon.FIREARM), Province.QC))
        val consent = FakeAdConsent(privacyOptionsRequired = false)
        val viewModel = SettingsViewModel(userData, Today { TEST_DAY }, consent)
        compose.setContent {
            BetterHunterTheme {
                SettingsScreen("1.2.3", {}, {}, {}, {}, {}, {}, viewModel)
            }
        }
        compose.onNodeWithTag("settings-version").assertIsDisplayed()
        compose.onNodeWithTag("settings-privacy-options").assertDoesNotExist()

        consent.privacyOptionsRequired.value = true
        compose.onNodeWithText("Privacy options").assertIsDisplayed()
        compose.onNodeWithTag("settings-privacy-options").performClick()
        assertEquals(1, consent.privacyOptionsShown)
    }
}
