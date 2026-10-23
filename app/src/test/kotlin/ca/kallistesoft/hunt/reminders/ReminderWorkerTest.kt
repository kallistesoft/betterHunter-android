package ca.kallistesoft.hunt.reminders

import android.Manifest
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import ca.kallistesoft.hunt.BetterHunterApplication
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.model.FieldTraining
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.Reminder
import ca.kallistesoft.hunt.core.model.Weapon
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.time.LocalTime

/** The reminder job, with the real app's progress: it shows what's due now, then plans the next one. */
@RunWith(RobolectricTestRunner::class)
class ReminderWorkerTest {
    private val app: BetterHunterApplication = ApplicationProvider.getApplicationContext()
    private val notifications get() = shadowOf(app.getSystemService(NotificationManager::class.java))

    @Before
    fun setUp() = shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

    @Test
    fun todaysReminderIsShownWhenItsTimeHasCome() {
        val now = LocalTime.now()
        // Answering sets the hunter's usual time to now; outside 8 am to 9 pm no reminder is ever shown.
        assumeTrue(now.hour in 8..20)
        progress {
            dailyReminders()
            record("q1", true)
        }

        assertEquals(ListenableWorker.Result.success(), runWorker())

        val notification = notifications.getNotification(Reminder.Kind.DAILY.ordinal)
        assertNotNull(notification)
        assertEquals("daily", notification.channelId)
        assertEquals(WorkInfo.State.ENQUEUED, reminderWork().state)
    }

    @Test
    fun nothingIsShownOnceTodaysQuestionsAreDone() {
        progress {
            dailyReminders()
            repeat(FieldTraining.DAILY_SPECIES + FieldTraining.DAILY_GENERAL) { recordDaily("q$it", true) }
        }

        assertEquals(ListenableWorker.Result.success(), runWorker())

        // Tomorrow's is planned instead.
        assertTrue(notifications.allNotifications.isEmpty())
        assertEquals(WorkInfo.State.ENQUEUED, reminderWork().state)
    }

    @Test
    fun nothingIsShownOrPlannedWithRemindersOff() {
        progress { saveProfile(HunterType.NEW_HUNTER, setOf(Weapon.FIREARM), Province.QC) }

        assertEquals(ListenableWorker.Result.success(), runWorker())

        assertTrue(notifications.allNotifications.isEmpty())
        assertTrue(reminderWorks().all { it.state == WorkInfo.State.CANCELLED })
    }

    private fun progress(block: suspend UserDataRepository.() -> Unit) = runBlocking { app.userData.block() }

    private suspend fun UserDataRepository.dailyReminders() {
        saveProfile(HunterType.NEW_HUNTER, setOf(Weapon.FIREARM), Province.QC)
        setDailyReminders(true)
    }

    private fun runWorker() = runBlocking { TestListenableWorkerBuilder<ReminderWorker>(app).build().doWork() }

    private fun reminderWorks(): List<WorkInfo> = WorkManager.getInstance(app).getWorkInfosForUniqueWork("reminder").get()

    private fun reminderWork(): WorkInfo = reminderWorks().single()
}
