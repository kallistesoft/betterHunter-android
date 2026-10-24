package ca.kallistesoft.hunt.feature.home

import app.cash.turbine.test
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.flags.FeatureFlag
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.SeasonStatus
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.Weapon
import ca.kallistesoft.hunt.core.testing.FakeContentRepository
import ca.kallistesoft.hunt.core.testing.FakeFeatureFlags
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.MainDispatcherRule
import ca.kallistesoft.hunt.core.testing.TEST_DAY
import ca.kallistesoft.hunt.core.testing.TestContent
import ca.kallistesoft.hunt.core.testing.awaitItemMatching
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userData = FakeUserDataRepository(UserProgress().withProfile(HunterType.WORKING_ON_IT, emptySet(), Province.OTHER))
    private val flags = FakeFeatureFlags()
    private val viewModel by lazy { HomeViewModel(FakeContentRepository(), userData, Today { TEST_DAY }, flags) }

    @Test
    fun `the dashboard is built around the user's exams`() = runTest {
        viewModel.uiState.test {
            val state = awaitItemMatching { !it.loading }
            assertEquals(TestContent.pal, state.mainExam)
            assertEquals(2, state.trainingExamCount)
            assertEquals(0f, state.readiness)
            assertEquals(1, state.gameCount)
            assertEquals(3, state.animalCount)
            assertNull(assertNotNull(state.photoOfDay).choice)
            assertEquals(0, state.answeredToday)
            assertFalse(state.hunter)
        }
    }

    @Test
    fun `passing the real exam turns Home into the hunter's, with a welcome until closed`() = runTest {
        userData.setProvince(Province.QC, true)
        viewModel.uiState.test {
            assertFalse(awaitItemMatching { !it.loading }.hunter)

            userData.markExamPassed("ICAF")
            val hunter = awaitItem()
            assertTrue(hunter.hunter)
            assertTrue(hunter.newHunterBanner)
            assertEquals("ICAF", hunter.passedExam?.id)
            assertEquals(TestContent.photoGame.id, hunter.photoGameId)

            viewModel.dismissNewHunterBanner()
            assertFalse(awaitItem().newHunterBanner)
        }
    }

    @Test
    fun `answering the photo of the day counts toward the goal and the streak`() = runTest {
        viewModel.uiState.test {
            val photo = awaitItemMatching { !it.loading }.photoOfDay!!
            viewModel.answerPhotoOfDay(photo.question.answerIndex)

            val after = awaitItem()
            assertEquals(photo.question.answerIndex, after.photoOfDay?.choice)
            assertEquals(1, after.answeredToday)
            assertEquals(1, after.streak)

            viewModel.answerPhotoOfDay(0) // already answered today: ignored
            expectNoEvents()
        }
    }

    @Test
    fun `feature flags hide the recognition games and the photo of the day live`() = runTest {
        viewModel.uiState.test {
            assertEquals(1, awaitItemMatching { !it.loading }.gameCount)

            flags.set(FeatureFlag.RECOGNITION_GAMES, false)
            val noGames = awaitItem()
            assertEquals(0, noGames.gameCount)
            assertNotNull(noGames.photoOfDay)

            flags.set(FeatureFlag.PHOTO_OF_THE_DAY, false)
            assertNull(awaitItem().photoOfDay)
        }
    }

    private fun <T> assertNotNull(value: T?): T = value.also { org.junit.Assert.assertNotNull(it) }!!

    @Test
    fun `a hunter from onboarding gets today's questions, Field skills and the season card`() = runTest {
        userData.saveProfile(HunterType.HUNTER, setOf(Weapon.FIREARM), Province.QC)
        userData.setSeasonStart(TEST_DAY + 21)
        viewModel.uiState.test {
            val state = awaitItemMatching { !it.loading }
            assertTrue(state.hunter)
            assertEquals(DailyState(0, 3, 0, 3), state.daily)
            assertEquals(SeasonStatus.Countdown(TEST_DAY + 21, 21), state.season)

            userData.recordDaily("hc-l07-1", correct = true)
            assertEquals(0.125f, awaitItemMatching { it.skillsReadiness > 0f }.skillsReadiness, 1e-6f)
        }
    }

    @Test
    fun `a hunter who passed the exam in the app gets today's questions and keeps the passed exam`() = runTest {
        userData.setProvince(Province.QC, true)
        userData.markExamPassed("ICAF")
        viewModel.uiState.test {
            val state = awaitItemMatching { !it.loading }
            assertTrue(state.hunter)
            assertTrue(state.newHunterBanner)
            assertEquals("ICAF", state.passedExam?.id)
            assertNotNull(state.daily)
        }
    }
}
