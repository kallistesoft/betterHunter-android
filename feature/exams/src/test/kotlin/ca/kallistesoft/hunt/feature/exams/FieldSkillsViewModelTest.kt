package ca.kallistesoft.hunt.feature.exams

import app.cash.turbine.test
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.model.FieldSkill
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.Weapon
import ca.kallistesoft.hunt.core.testing.FakeContentRepository
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.MainDispatcherRule
import ca.kallistesoft.hunt.core.testing.TEST_DAY
import ca.kallistesoft.hunt.core.testing.awaitItemMatching
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FieldSkillsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userData = FakeUserDataRepository(
        UserProgress().withProfile(HunterType.HUNTER, setOf(Weapon.FIREARM), Province.QC),
    )

    @Test
    fun `skills show readiness, mastery and mistakes`() = runTest {
        val vm = FieldSkillsViewModel(FakeContentRepository(), userData, Today { TEST_DAY })
        vm.uiState.test {
            val start = awaitItemMatching { !it.loading }
            // The test content has no safe-handling questions: that skill isn't listed.
            assertEquals(listOf(FieldSkill.SHOT_PLACEMENT, FieldSkill.AFTER_SHOT, FieldSkill.RULES, FieldSkill.SURVIVAL), start.skills.map { it.skill })
            assertFalse(start.allMastered)

            userData.record("hc-l07-1", correct = true)
            userData.record("hc-l07-2", correct = true)
            userData.record("hc-l08-1", correct = false)
            val after = awaitItemMatching { it.mistakes == 1 }
            assertTrue(after.skills.first { it.skill == FieldSkill.SHOT_PLACEMENT }.mastered)
            assertEquals(1, after.keptInMix)
        }
    }
}
