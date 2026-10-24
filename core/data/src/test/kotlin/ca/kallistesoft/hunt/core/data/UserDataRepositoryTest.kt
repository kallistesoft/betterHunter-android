package ca.kallistesoft.hunt.core.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStoreFactory
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.Reminder
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.Weapon
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.LocalDate

class UserDataRepositoryTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val day = 20_000L

    @Test
    fun `answers count toward today, the weak spots and the playing time`() = runTest {
        val repo = repository()
        repo.record("q1", correct = false)
        repo.recordAll(listOf("q2" to true, "q3" to false))
        val p = repo.progress.first()
        assertEquals(mapOf("q1" to false, "q2" to true, "q3" to false), p.lastResult)
        assertEquals(setOf("q1", "q3"), p.weak.keys)
        assertEquals(3, p.answeredToday(day))
        assertEquals(1, p.currentStreak(day))
        assertNotNull("learned when the hunter plays", p.playMinutes[day])
    }

    @Test
    fun `an empty batch of answers changes nothing`() = runTest {
        val repo = repository()
        repo.recordAll(emptyList())
        assertEquals(UserProgress(), repo.progress.first())
    }

    @Test
    fun `onboarding answers and settings are saved`() = runTest {
        val repo = repository()
        repo.saveProfile(HunterType.WORKING_ON_IT, setOf(Weapon.BOW), Province.QC)
        repo.setProvince(Province.AB, enabled = true)
        repo.setHunterType(HunterType.HUNTER)
        repo.setWeapons(setOf(Weapon.FIREARM, Weapon.CROSSBOW))
        repo.setHomeProvince(Province.OTHER)
        repo.setDailyGoal(30)
        val p = repo.progress.first()
        assertEquals(HunterType.HUNTER, p.hunterType)
        assertEquals(setOf(Weapon.FIREARM, Weapon.CROSSBOW), p.weapons)
        assertEquals(Province.OTHER, p.homeProvince)
        assertEquals(setOf(Province.QC, Province.AB, Province.OTHER), p.provinces)
        assertEquals(30, p.dailyGoal)
    }

    @Test
    fun `passing the real exam welcomes a hunter until the banner is closed`() = runTest {
        val repo = repository()
        repo.markExamPassed("ICAF")
        assertTrue(repo.progress.first().newHunterBanner)
        assertEquals(HunterType.HUNTER, repo.progress.first().hunterType)
        repo.dismissNewHunterBanner()
        assertFalse(repo.progress.first().newHunterBanner)
        assertEquals(setOf("ICAF"), repo.progress.first().passedExams)
    }

    @Test
    fun `the photo of the day is answered for today and counts as an answer`() = runTest {
        val repo = repository()
        repo.answerPhotoOfDay("id-1", choice = 2, correct = true)
        val p = repo.progress.first()
        assertEquals(2, p.photoOfDayChoice(day))
        assertNull(p.photoOfDayChoice(day + 1))
        assertEquals(mapOf("id-1" to true), p.lastResult)
        assertEquals(1, p.answeredToday(day))
    }

    @Test
    fun `hunter answers, season and reminders are saved`() = runTest {
        val repo = repository()
        repo.recordDaily("hc-1", correct = true)
        repo.setSeasonStart(day + 30)
        repo.setSeasonReminders(true)
        var p = repo.progress.first()
        assertEquals(mapOf("hc-1" to true), p.dailyResults(day))
        assertEquals(day + 30, p.seasonStart)
        assertTrue(p.seasonReminders)
        assertFalse(p.dailyReminderAsked)

        repo.notNowDailyReminders()
        assertEquals(day, repo.progress.first().dailyReminderNotNowDay)
        repo.markDailyReminderAsked()
        assertTrue(repo.progress.first().dailyReminderAsked)
        repo.setDailyReminders(true)
        p = repo.progress.first()
        assertTrue(p.dailyReminders)

        repo.reminderShown(Reminder.Kind.DAILY, day)
        assertEquals(day, repo.progress.first().reminderDay)
        assertEquals(1, repo.progress.first().ignoredReminders)
        repo.appOpened()
        assertEquals(0, repo.progress.first().ignoredReminders)

        repo.setSeasonStart(null) // "I don't know yet"
        assertNull(repo.progress.first().seasonStart)
    }

    @Test
    fun `progress is written as JSON and read back`() = runTest {
        val progress = UserProgress(lastResult = mapOf("q1" to true), homeProvince = Province.AB, dailyGoal = 10)
        val out = ByteArrayOutputStream()
        UserProgressSerializer.writeTo(progress, out)
        assertTrue(out.toString().contains("\"q1\""))
        assertEquals(progress, UserProgressSerializer.readFrom(ByteArrayInputStream(out.toByteArray())))
        assertEquals(UserProgress(), UserProgressSerializer.defaultValue)
    }

    @Test
    fun `progress saved as a new or experienced hunter reads back as a hunter, answers kept`() = runTest {
        for (old in listOf("NEW_HUNTER", "EXPERIENCED")) {
            val saved = """{"lastResult":{"q1":true},"hunterType":"$old","homeProvince":"QC","newHunterBanner":true}"""
            val p = UserProgressSerializer.readFrom(ByteArrayInputStream(saved.toByteArray()))
            assertEquals(HunterType.HUNTER, p.hunterType)
            assertEquals(mapOf("q1" to true), p.lastResult)
            assertTrue(p.isHunter)
            assertTrue(p.newHunterBanner)
        }
    }

    @Test(expected = CorruptionException::class)
    fun `an unreadable progress file is reported as corrupt`() = runTest {
        UserProgressSerializer.readFrom(ByteArrayInputStream("not json".toByteArray()))
    }

    @Test
    fun `the system clock gives today's day number`() {
        val before = LocalDate.now().toEpochDay()
        val today = Today.System()
        assertTrue(today in before..LocalDate.now().toEpochDay())
    }

    private fun TestScope.repository() = DataStoreUserDataRepository(
        store = DataStoreFactory.create(UserProgressSerializer, scope = backgroundScope) {
            tmp.root.resolve("user_progress.json")
        },
        today = { day },
    )
}
