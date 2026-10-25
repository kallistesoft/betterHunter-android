package ca.kallistesoft.hunt

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.core.os.LocaleListCompat
import ca.kallistesoft.hunt.core.data.ContentParser
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.Weapon
import ca.kallistesoft.hunt.core.ui.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** App navigation: every destination is reached from where the app offers it, and leads back where it should. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "en-w411dp-h891dp")
class BetterHunterAppTest {
    @get:Rule
    val rule = createEmptyComposeRule()
    private val app = AppRobot(rule)

    @After
    fun tearDown() {
        app.scenario.close()
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }

    @Test
    fun firstLaunchOnboardsThenShowsHome() {
        app.launch()
        app.clickTag("choice-WORKING_ON_IT")
        app.clickTag("onboarding-continue")
        app.waitFor(hasTextOf(R.string.hunt_with_title))
        app.clickTag("onboarding-continue")
        app.clickTag("choice-QC")
        app.clickTag("onboarding-continue")

        app.waitForTag("hub-exams")
        assertTrue(runBlocking { app.app.userData.progress.first().onboarded })
    }

    @Test
    fun settingsLanguageSetsThePerAppLanguage() {
        app.launch(student)
        app.clickTag("settings")

        app.clickTag("toggle-FR")
        assertEquals("fr-CA", AppCompatDelegate.getApplicationLocales().toLanguageTags())

        app.clickTag("toggle-EN")
        assertEquals("en-CA", AppCompatDelegate.getApplicationLocales().toLanguageTags())
    }

    @Test
    fun examTrainingLeadsToProvincesTheMockExamsAndBack() {
        app.launch(student)
        app.clickTag("hub-exams")
        app.clickTag("change-exams")
        app.waitForTag("province-AB")
        app.clickBackArrow()

        app.clickTag("exam-ICAF")
        app.clickTag("mock-start-1")
        app.waitForTag("choice-0")
        app.pressBack()
        app.clickText(app.str(R.string.quit))
        app.waitForTag("mock-start-1") // quit: Mock 1 is still to take, free
        app.clickBackArrow()
        app.waitForTag("exam-card-ICAF")

        app.clickBackArrow()
        app.waitForTag("hub-exams")
    }

    @Test
    fun passingTheRealExamGoesStraightHome() {
        app.launch(student)
        app.clickTag("hub-exams")
        app.clickTag("passed-ICAF")
        app.clickTag("passed-not-yet")
        app.waitForTag("exam-card-ICAF")

        app.clickTag("passed-ICAF")
        app.clickTag("passed-yes")

        // Exam training and the question are gone: Home, now the hunter's.
        app.waitForTag("new-hunter-banner")
        assertFalse(app.exists(hasTestTag("exam-card-ICAF")))
    }

    @Test
    fun homeStartsPracticeAndRecognitionGames() {
        app.launch(student)
        app.clickTag("today-practice")
        app.waitForTag("choice-0")
        app.clickBackArrow()
        app.clickText(app.str(R.string.quit))

        app.clickTag("hub-recognise")
        app.click(tagStartingWith("practice-"))
        app.waitForTag("choice-0")
        app.pressBack()
        app.clickText(app.str(R.string.quit))
        app.waitFor(tagStartingWith("practice-"))
    }

    @Test
    fun aReviewWithNothingLeftOffersPractice() {
        val question = icafQuestionId()
        app.launch {
            student()
            record(question, false)
        }
        app.clickTag("hub-exams")
        app.clickTag("review-ICAF")
        app.clickTag("choice-0")
        // Cleared meanwhile (two right answers in a row), so trying the review again finds nothing.
        runBlocking { repeat(2) { app.app.userData.record(question, true) } }
        app.clickTag("next")
        app.clickTag("again")

        app.clickTag("empty-practise")
        app.waitForTag("choice-0")
        assertFalse(app.exists(hasTestTag("empty-practise")))
    }

    @Test
    fun settingsOpensEachProfileScreen() {
        app.launch(student)
        app.clickTag("settings")
        app.clickTag("settings-hunter-type")
        app.clickTag("choice-HUNTER")
        app.clickTag("profile-save")
        app.waitFor(hasTextOf(R.string.hunter_hunter))

        app.clickTag("settings-provinces")
        app.waitForTag("province-AB")
        app.clickBackArrow()

        app.clickTag("settings-season")
        app.clickTag("season-dont-know")
        app.waitForTag("settings-season")

        app.clickTag("settings-about")
        app.waitForTag("disclaimer")
        app.clickBackArrow()
        app.waitForTag("settings-about")
    }

    @Test
    fun homeLinksToTheProvincesAndAbout() {
        app.launch(student)
        app.clickTag("today-change")
        app.waitForTag("province-AB")
        app.clickBackArrow()

        app.click(hasTextOf(R.string.learn_more))
        app.waitForTag("disclaimer")
    }

    @Test
    fun aHunterMovesFromOneSkillToTheNext() {
        app.launch(asHunter)
        app.clickTag("hub-skills")
        app.clickTag("skill-SHOT_PLACEMENT")
        app.answerAll(10)
        app.clickTag("next-skill")

        // "After the shot" replaces the finished run: back leads to Field skills.
        app.waitForTag("choice-0")
        app.pressBack()
        app.clickText(app.str(R.string.quit))
        app.waitForTag("mix-card")
        app.clickBackArrow()
        app.waitForTag("daily-card")
    }

    @Test
    fun aHunterStartsTodaysQuestionsFromHome() {
        app.launch(asHunter)
        app.clickTag("daily-continue")
        app.waitForTag("choice-0")
        app.pressBack()
        app.clickText(app.str(R.string.quit))

        app.clickTag("hub-recognise")
        app.waitFor(tagStartingWith("practice-"))
    }

    private fun hasTextOf(id: Int) = hasText(app.str(id))

    private fun tagStartingWith(prefix: String) = SemanticsMatcher("test tag starts with $prefix") {
        it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith(prefix) == true
    }

    /** A question of the ICAF exam, from the bundled content. */
    private fun icafQuestionId(): String {
        val assets = app.app.assets
        val content = ContentParser.content(assets.open("content/exams.json").reader().readText()) { scope ->
            runCatching { assets.open("content/questions/$scope.json").reader().readText() }.getOrNull()
        }
        return content.questionsFor(content.exam("ICAF")!!).first().id
    }

    private companion object {
        val student: suspend UserDataRepository.() -> Unit = {
            saveProfile(HunterType.WORKING_ON_IT, setOf(Weapon.FIREARM), Province.QC)
        }
        val asHunter: suspend UserDataRepository.() -> Unit = {
            saveProfile(HunterType.HUNTER, setOf(Weapon.FIREARM), Province.QC)
        }
    }
}
