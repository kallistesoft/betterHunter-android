package ca.kallistesoft.hunt.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.model.FieldRun
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Lang
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.core.model.SeasonStatus
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.Weapon
import ca.kallistesoft.hunt.core.testing.FakeContentRepository
import ca.kallistesoft.hunt.core.testing.FakeFeatureFlags
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import ca.kallistesoft.hunt.core.testing.TEST_DAY
import ca.kallistesoft.hunt.core.testing.TestContent
import ca.kallistesoft.hunt.core.ui.preview.PreviewData
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Home's taps reach the right callbacks, for each kind of user (a tall English phone, so the whole list shows). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK], qualifiers = "en-w411dp-h2400dp")
class HomeScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<String>()

    private fun setHome(state: HomeUiState) = compose.setContent {
        BetterHunterTheme {
            HomeContent(
                state,
                onOpenExams = { events += "exams" },
                onOpenRecognition = { events += "recognition" },
                onOpenAbout = { events += "about" },
                onOpenSettings = { events += "settings" },
                onOpenProvinces = { events += "provinces" },
                onStartQuiz = { exam, mode -> events += "quiz:$exam:$mode" },
                onSetLanguage = { events += "lang:$it" },
                onAnswerPhoto = { events += "photo:$it" },
                onDismissBanner = { events += "dismiss" },
                onOpenFieldSkills = { events += "skills" },
                onStartField = { events += "field:$it" },
                onOpenSeasonDate = { events += "season-date" },
            )
        }
    }

    @Test
    fun `working on it - practise the main exam, change exams, open the hubs and answer the photo`() {
        setHome(previewHomeState.copy(hasSampleContent = true, readiness = null, streak = 0))
        compose.onNodeWithTag("goal-text").assertIsDisplayed()
        compose.onNodeWithText("Hunter education with firearms (Québec)").assertIsDisplayed()
        compose.onNodeWithText("4 exams", substring = true).assertIsDisplayed()

        compose.onNodeWithTag("today-practice").performClick()
        compose.onNodeWithTag("today-change").performClick()
        compose.onNodeWithTag("hub-exams").performClick()
        compose.onNodeWithTag("hub-recognise").performClick()
        compose.onNodeWithTag("settings").performClick()
        compose.onNodeWithTag("pod-choice-2").performClick()
        compose.onNodeWithText("Learn more").performClick()
        compose.onNodeWithTag("toggle-FR").performClick()

        assertEquals(
            listOf("quiz:ICAF:${QuizMode.PRACTICE}", "provinces", "exams", "recognition", "settings", "photo:2", "about", "lang:${Lang.FR}"),
            events,
        )
    }

    @Test
    fun `a new hunter plays What is it, sees the passed exam and closes the welcome`() {
        setHome(
            previewHomeState.copy(
                hunter = true,
                newHunterBanner = true,
                passedExamId = "ICAF",
                photoOfDay = previewHomeState.photoOfDay?.copy(choice = 0),
            ),
        )
        compose.onNodeWithText("Hunter course passed. Welcome, new hunter!").assertIsDisplayed()
        compose.onNodeWithText("ICAF passed").assertIsDisplayed()
        compose.onNodeWithText("Reddish-brown deer", substring = true).assertIsDisplayed()

        compose.onNodeWithTag("today-play-id").performClick()
        compose.onNodeWithTag("banner-close").performClick()
        compose.onNodeWithTag("pod-play").performClick()

        val play = "quiz:${PreviewData.photoGame.id}:${QuizMode.PRACTICE}"
        assertEquals(listOf(play, "dismiss", play), events)
    }

    @Test
    fun `the goal reached shows a check instead of what's left`() {
        setHome(previewHomeState.copy(answeredToday = 20, photoOfDay = null))
        compose.onNodeWithText("Today's goal reached!").assertIsDisplayed()
        compose.onNodeWithTag("photo-of-day").assertDoesNotExist()
    }

    @Test
    fun `a field hunter starts the season check, today's questions and Field skills`() {
        setHome(
            previewHomeState.copy(
                fieldHunter = true,
                season = SeasonStatus.Countdown(TEST_DAY + 21, 21),
                daily = DailyState(0, 3, 0, 3),
                skillsReadiness = 0.25f,
                photoOfDay = null,
            ),
        )
        compose.onNodeWithText("Your season starts in 3 weeks").assertIsDisplayed()
        compose.onNodeWithText("Start today's questions").assertIsDisplayed()
        compose.onNodeWithTag("today-practice").assertDoesNotExist()
        compose.onNodeWithTag("season-set-date").assertDoesNotExist()

        compose.onNodeWithTag("season-card").performClick()
        compose.onNodeWithTag("daily-continue").performClick()
        compose.onNodeWithTag("daily-species").performClick()
        compose.onNodeWithTag("daily-general").performClick()
        compose.onNodeWithTag("hub-skills").performClick()
        compose.onNodeWithTag("hub-recognise").performClick()

        assertEquals(
            listOf("field:${FieldRun.SEASON_CHECK}") + List(3) { "field:${FieldRun.DAILY}" } + listOf("skills", "recognition"),
            events,
        )
    }

    @Test
    fun `without a season date, the card offers to set one`() {
        setHome(
            previewHomeState.copy(
                fieldHunter = true,
                season = SeasonStatus.Coming,
                daily = DailyState(3, 3, 1, 3),
                streak = 2,
                gameCount = 0,
            ),
        )
        compose.onNodeWithText("Hunting season is coming").assertIsDisplayed()
        compose.onNodeWithText("Continue: 2 general left").assertIsDisplayed()
        compose.onNodeWithTag("hub-recognise").assertDoesNotExist()

        compose.onNodeWithTag("season-set-date").performClick()
        assertEquals(listOf("season-date"), events)
    }

    @Test
    fun `today's questions continue with the species, then are done for the day`() {
        setHome(previewHomeState.copy(fieldHunter = true, daily = DailyState(1, 3, 0, 3)))
        compose.onNodeWithText("Continue: 2 species left").assertIsDisplayed()
    }

    @Test
    fun `all of today's questions answered`() {
        assertTrue(DailyState(3, 3, 3, 3).allDone)
        assertFalse(DailyState(3, 3, 2, 3).allDone)
        setHome(previewHomeState.copy(fieldHunter = true, daily = DailyState(3, 3, 3, 3)))
        compose.onNodeWithTag("daily-all-done").assertIsDisplayed()
        compose.onNodeWithTag("daily-continue").assertDoesNotExist()
    }

    @Test
    fun `the season countdown in weeks, days, tomorrow and today`() {
        val titles = mutableListOf<String>()
        compose.setContent { listOf(21, 5, 1, 0).forEach { titles += countdownTitle(it) } }
        compose.waitForIdle()
        assertEquals(
            listOf("Your season starts in 3 weeks", "Your season starts in 5 days", "Your season starts tomorrow", "Your season starts today"),
            titles.take(4),
        )
    }

    @Test
    fun `nothing shows while loading`() {
        setHome(HomeUiState())
        compose.onNodeWithTag("goal-text").assertDoesNotExist()
        compose.onNodeWithTag("settings").performClick()
        assertEquals(listOf("settings"), events)
    }

    @Test
    fun `the screen answers the photo of the day and closes the welcome through its ViewModel`() {
        val userData = FakeUserDataRepository(UserProgress().withProfile(HunterType.WORKING_ON_IT, setOf(Weapon.FIREARM), Province.QC))
        runBlocking {
            userData.setProvince(Province.QC, true)
            userData.markExamPassed("ICAF")
        }
        val viewModel = HomeViewModel(FakeContentRepository(), userData, Today { TEST_DAY }, FakeFeatureFlags())
        compose.setContent {
            BetterHunterTheme {
                HomeScreen(
                    onOpenExams = {}, onOpenRecognition = {}, onOpenAbout = {}, onOpenSettings = {}, onOpenProvinces = {},
                    onStartQuiz = { _, _ -> }, onSetLanguage = {}, onOpenFieldSkills = {}, onStartField = {}, onOpenSeasonDate = {},
                    viewModel = viewModel,
                )
            }
        }
        compose.onNodeWithTag("banner-close").performClick()
        compose.onNodeWithTag("new-hunter-banner").assertDoesNotExist()
        assertFalse(userData.progress.value.newHunterBanner)

        compose.onNodeWithTag("pod-choice-1").performClick()
        compose.onNodeWithTag("pod-play").assertExists()
        assertEquals(1, viewModel.uiState.value.answeredToday)
        assertEquals(TestContent.photoGame.id, viewModel.uiState.value.photoGameId)
    }
}
