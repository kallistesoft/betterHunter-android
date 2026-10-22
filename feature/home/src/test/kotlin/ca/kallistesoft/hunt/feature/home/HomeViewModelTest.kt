package ca.kallistesoft.hunt.feature.home

import app.cash.turbine.test
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.flags.FeatureFlag
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.testing.FakeContentRepository
import ca.kallistesoft.hunt.core.testing.FakeFeatureFlags
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.MainDispatcherRule
import ca.kallistesoft.hunt.core.testing.TEST_DAY
import ca.kallistesoft.hunt.core.testing.TestContent
import ca.kallistesoft.hunt.core.testing.awaitItemMatching
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userData = FakeUserDataRepository(UserProgress(myExams = listOf("PAL")))
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
}
