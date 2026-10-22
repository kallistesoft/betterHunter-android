package ca.kallistesoft.hunt.feature.home

import ca.kallistesoft.hunt.core.data.ContentRepository
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.flags.DefaultFeatureFlags
import ca.kallistesoft.hunt.core.flags.FeatureFlag
import ca.kallistesoft.hunt.core.model.Content
import ca.kallistesoft.hunt.core.model.Exam
import ca.kallistesoft.hunt.core.model.Lang
import ca.kallistesoft.hunt.core.model.PHOTO_ID_EXAM
import ca.kallistesoft.hunt.core.model.Question
import ca.kallistesoft.hunt.core.model.QuestionText
import ca.kallistesoft.hunt.core.model.UserProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val pal = Exam("PAL", mapOf(Lang.FR to "PAL"), listOf("federal"))
    private val photoGame = Exam(PHOTO_ID_EXAM, mapOf(Lang.FR to "ID"), listOf("wildlife-id"))
    private fun question(id: String, scope: String) =
        Question(id, scope, "c", mapOf(Lang.FR to QuestionText("?", listOf("A", "B", "C", "D"), "E")), answerIndex = 0)

    private val content = object : ContentRepository {
        override val content = flowOf(
            Content(
                listOf(pal, photoGame),
                mapOf("federal" to listOf(question("q1", "federal")), "wildlife-id" to listOf(question("id1", "wildlife-id"))),
            ),
        )
    }
    private val userData = object : UserDataRepository {
        override val progress = MutableStateFlow(UserProgress(myExams = listOf("PAL")))
        override suspend fun record(questionId: String, correct: Boolean) = Unit
        override suspend fun recordAll(results: List<Pair<String, Boolean>>) = Unit
        override suspend fun setMyExams(ids: List<String>) = Unit
        override suspend fun answerPhotoOfDay(questionId: String, choice: Int, correct: Boolean) = Unit
    }

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private suspend fun state(vararg off: FeatureFlag): HomeUiState {
        val flags = DefaultFeatureFlags(off.associateWith { false })
        val vm = HomeViewModel(content, userData, Today { 20_000L }, flags)
        return vm.uiState.first { !it.loading }
    }

    @Test
    fun `everything shows by default`() = runTest {
        val state = state()
        assertEquals(pal, state.mainExam)
        assertEquals(1, state.gameCount)
        assertNotNull(state.photoOfDay)
    }

    @Test
    fun `flags hide the recognition hub and the photo of the day`() = runTest {
        val noGames = state(FeatureFlag.RECOGNITION_GAMES)
        assertEquals(0, noGames.gameCount)
        assertNotNull(noGames.photoOfDay)

        val noPhoto = state(FeatureFlag.PHOTO_OF_THE_DAY)
        assertEquals(1, noPhoto.gameCount)
        assertNull(noPhoto.photoOfDay)
    }
}
