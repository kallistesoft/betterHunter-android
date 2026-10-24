package ca.kallistesoft.hunt.feature.exams

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.model.FieldRun
import ca.kallistesoft.hunt.core.model.FieldSkill
import ca.kallistesoft.hunt.core.model.FieldTraining
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.SkillSummary
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.Weapon
import ca.kallistesoft.hunt.core.testing.FakeContentRepository
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import ca.kallistesoft.hunt.core.testing.TEST_DAY
import ca.kallistesoft.hunt.core.ui.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK], qualifiers = ScreenshotDevice.LIGHT_EN)
class FieldSkillsScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<Pair<FieldRun, FieldSkill?>>()

    private fun show(state: FieldSkillsUiState) = compose.setContent {
        BetterHunterTheme { FieldSkillsContent(state, onBack = {}, onStart = { run, skill -> events += run to skill }) }
    }

    @Test
    fun `the mix, my mistakes and each skill start their run`() {
        val userData = FakeUserDataRepository(
            UserProgress().withProfile(HunterType.HUNTER, setOf(Weapon.FIREARM), Province.QC)
                .record("hc-l07-1", correct = false, TEST_DAY),
        )
        var back = false
        compose.setContent {
            BetterHunterTheme {
                FieldSkillsScreen(
                    onBack = { back = true },
                    onStart = { run, skill -> events += run to skill },
                    viewModel = FieldSkillsViewModel(FakeContentRepository(), userData, Today { TEST_DAY }),
                )
            }
        }
        // The state is built on Dispatchers.Default, which Compose doesn't wait for.
        compose.waitUntil { compose.onAllNodesWithTag("start-mix").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(string(R.string.skills_sub)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.mix_sub, FieldRun.MIX.size)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.my_mistakes_count, 1)).assertIsDisplayed()
        compose.onNodeWithTag("start-mix").performClick()
        compose.onNodeWithTag("skills-mistakes").performClick()
        compose.onNodeWithTag("skill-${FieldSkill.AFTER_SHOT.name}").performClick()
        compose.onNodeWithContentDescription(string(R.string.back)).performClick()
        assertEquals(
            listOf(FieldRun.MIX to null, FieldRun.MISTAKES to null, FieldRun.SKILL to FieldSkill.AFTER_SHOT),
            events,
        )
        assertEquals(true, back)
    }

    @Test
    fun `skills show mastered, faded or their readiness, and the mix keeps mastered ones sharp`() {
        show(
            FieldSkillsUiState(
                loading = false,
                skills = listOf(
                    SkillSummary(FieldSkill.SHOT_PLACEMENT, questionCount = 2, readiness = 1f, expired = 0),
                    SkillSummary(FieldSkill.AFTER_SHOT, questionCount = 2, readiness = 0.5f, expired = 1),
                    SkillSummary(FieldSkill.RULES, questionCount = 4, readiness = 0.25f, expired = 0),
                ),
            ),
        )
        compose.onNodeWithText(string(R.string.mastered), ignoreCase = true).assertIsDisplayed() // an overline, in capitals
        compose.onNodeWithText(plural(R.plurals.was_mastered, 1, 1)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.percent, 25)).assertIsDisplayed()
        val kept = minOf(FieldTraining.KEEP_SHARP, 1)
        compose.onNodeWithText(string(R.string.mix_sub_kept, FieldRun.MIX.size - kept, kept)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.start_mix)).assertIsDisplayed()
    }

    @Test
    fun `every skill mastered shows Keep your edge`() {
        show(
            FieldSkillsUiState(
                loading = false,
                skills = listOf(SkillSummary(FieldSkill.SHOT_PLACEMENT, questionCount = 2, readiness = 1f, expired = 0)),
            ),
        )
        compose.onNodeWithText(string(R.string.skills_all_mastered_sub)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.keep_edge)).assertIsDisplayed()
        compose.onNodeWithText(string(R.string.keep_edge_sub, FieldRun.MIX.size)).assertIsDisplayed()
        compose.onNodeWithTag("start-mix").performClick()
        assertEquals(listOf(FieldRun.MIX to null), events)
    }

    @Test
    fun `nothing shows while loading`() {
        show(FieldSkillsUiState())
        compose.onNodeWithTag("mix-card").assertDoesNotExist()
    }
}
