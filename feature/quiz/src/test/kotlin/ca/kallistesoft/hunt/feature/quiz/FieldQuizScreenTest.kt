package ca.kallistesoft.hunt.feature.quiz

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import ca.kallistesoft.hunt.core.data.ContentRepository
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.model.Content
import ca.kallistesoft.hunt.core.model.FieldRun
import ca.kallistesoft.hunt.core.model.FieldSkill
import ca.kallistesoft.hunt.core.model.FieldTraining
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.Weapon
import ca.kallistesoft.hunt.core.testing.FakeContentRepository
import ca.kallistesoft.hunt.core.testing.FakeFeatureFlags
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import ca.kallistesoft.hunt.core.testing.TEST_DAY
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.nameRes
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** A new hunter's runs, driven by a real [FieldQuizViewModel] on the test content (two questions per skill, answer B). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK], qualifiers = ScreenshotDevice.LIGHT_EN)
class FieldQuizScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val userData = FakeUserDataRepository(
        UserProgress().withProfile(HunterType.NEW_HUNTER, setOf(Weapon.FIREARM), Province.QC),
    )
    private var closed = 0
    private val opened = mutableListOf<FieldSkill>()

    private fun show(run: FieldRun, skill: FieldSkill? = null, content: ContentRepository = FakeContentRepository()) {
        val vm = FieldQuizViewModel(run, skill, content, userData, SavedStateHandle(), Today { TEST_DAY }, FakeFeatureFlags())
        compose.setContent {
            BetterHunterTheme {
                FieldQuizScreen(run, skill, onClose = { closed++ }, onOpenSkill = { opened += it }, viewModel = vm)
            }
        }
    }

    /** Answers the questions left, [wrong] ones with A and the others with B, the right answer. */
    private fun answerAll(wrong: Set<Int> = emptySet()) {
        var i = 0
        while (compose.onAllNodesWithTag("next").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithTag(if (i in wrong) "choice-0" else "choice-1").performClick()
            compose.onNodeWithTag("next").performClick()
            i++
        }
    }

    private val shotPlacement get() = string(FieldSkill.SHOT_PLACEMENT.nameRes)

    @Test
    fun `a skill run, all right, masters the skill and offers the next one`() {
        show(FieldRun.SKILL, FieldSkill.SHOT_PLACEMENT)
        compose.onNodeWithText(string(R.string.field_overline, shotPlacement), ignoreCase = true).assertIsDisplayed()
        compose.onNodeWithTag("choice-1").performClick()
        compose.onNodeWithText(string(R.string.in_the_field), ignoreCase = true).assertIsDisplayed()
        compose.onNodeWithTag("next").performClick()
        answerAll()

        compose.onNodeWithText(string(R.string.percent, 100)).assertIsDisplayed()
        compose.onNodeWithTag("staying-mastered").assertIsDisplayed()
        compose.onNodeWithText(string(R.string.all_correct)).assertIsDisplayed()
        compose.onNodeWithTag("next-skill").performClick()
        compose.onNodeWithTag("field-skills").performClick()
        assertEquals(listOf(FieldSkill.AFTER_SHOT), opened)
        assertEquals(1, closed)
    }

    @Test
    fun `a skill run with a miss shows the skill getting stronger and the question to review`() {
        show(FieldRun.SKILL, FieldSkill.SHOT_PLACEMENT)
        answerAll(wrong = setOf(0))
        compose.onNodeWithTag("skill-change").assertIsDisplayed()
        compose.onNodeWithText(string(R.string.skill_stronger)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.percent_change, 0, 50)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.to_my_mistakes)).assertIsDisplayed()
    }

    @Test
    fun `the mix lists every skill it touched, and Again draws a new mix`() {
        show(FieldRun.MIX)
        answerAll()
        compose.onNodeWithText(string(R.string.your_skills), ignoreCase = true).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.todays_mix)).assertIsDisplayed()
        compose.onNodeWithTag("next-skill").assertDoesNotExist()
        compose.onNodeWithTag("again").performClick()
        compose.onNodeWithTag("choice-1").assertIsDisplayed()
        compose.onNodeWithTag("score").assertDoesNotExist()
    }

    @Test
    fun `today's questions, species first, end on Done for today, and Not now hides the nudge`() {
        show(FieldRun.DAILY)
        val species = string(R.string.daily_species, FieldTraining.DAILY_SPECIES)
        compose.onNodeWithText(string(R.string.daily_overline, species), ignoreCase = true).assertIsDisplayed()
        answerAll(wrong = setOf(3))

        compose.onNodeWithTag("daily-done").assertIsDisplayed()
        compose.onNodeWithText(plural(R.plurals.streak, 1, 1)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.daily_right, 5, 6)).assertIsDisplayed()
        compose.onNodeWithTag("daily-home").assertDoesNotExist()

        compose.onNodeWithTag("nudge-not-now").performClick()
        compose.onNodeWithTag("nudge").assertDoesNotExist()
        assertEquals(TEST_DAY, userData.progress.value.dailyReminderNotNowDay)
        assertFalse(userData.progress.value.dailyReminders)
        compose.onNodeWithTag("daily-home").performClick()
        assertEquals(1, closed)
    }

    @Test
    fun `after the species, today's general questions say so above them`() {
        show(FieldRun.DAILY)
        repeat(FieldTraining.DAILY_SPECIES) {
            compose.onNodeWithTag("choice-1").performClick()
            compose.onNodeWithTag("next").performClick()
        }
        val general = string(R.string.daily_general, FieldTraining.DAILY_GENERAL)
        compose.onNodeWithText(string(R.string.daily_overline, general), ignoreCase = true).assertIsDisplayed()
    }

    @Test
    fun `turning on reminders after today's questions, once allowed`() {
        shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        show(FieldRun.DAILY)
        answerAll()
        compose.onNodeWithTag("nudge-on").performClick()
        compose.onNodeWithTag("nudge").assertDoesNotExist()
        assertTrue(userData.progress.value.dailyReminders)
    }

    @Test
    fun `stopping a field run asks first, and says the answers are kept`() {
        show(FieldRun.SEASON_CHECK)
        compose.onNodeWithText(string(R.string.season_check_overline), ignoreCase = true).assertIsDisplayed()
        compose.onNodeWithContentDescription(string(R.string.back)).performClick()
        compose.onNodeWithText(string(R.string.quit_field_title)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.quit)).performClick()
        assertEquals(1, closed)
    }

    @Test
    fun `no mistakes, nothing to review`() {
        show(FieldRun.MISTAKES)
        compose.onNodeWithText(string(R.string.review_title)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.nothing_to_review)).assertIsDisplayed()
        compose.onNodeWithContentDescription(string(R.string.back)).performClick()
        assertEquals(1, closed)
    }

    @Test
    fun `a skill without questions says so`() {
        show(FieldRun.SKILL, FieldSkill.SAFE_HANDLING)
        compose.onNodeWithText(string(FieldSkill.SAFE_HANDLING.nameRes)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.no_questions)).assertIsDisplayed()
    }

    @Test
    fun `no content for today's questions says so`() {
        show(FieldRun.DAILY, content = FakeContentRepository(Content(exams = emptyList(), questionsByScope = emptyMap())))
        compose.onNodeWithText(string(R.string.today)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.no_questions)).assertIsDisplayed()
    }

    @Test
    fun `while the run loads, it can be closed`() {
        show(FieldRun.MIX, content = object : ContentRepository {
            override val content = MutableSharedFlow<Content>() // never loads
        })
        compose.onNodeWithTag("choice-0").assertDoesNotExist()
        compose.onNodeWithContentDescription(string(R.string.back)).performClick()
        assertEquals(1, closed)
    }
}
