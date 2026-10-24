package ca.kallistesoft.hunt.reminders

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.os.Looper
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** The reminder job, with the real app's progress: it shows what's due now, then plans the next one. */
@RunWith(RobolectricTestRunner::class)
class ReminderWorkerTest {
    private val app: BetterHunterApplication = ApplicationProvider.getApplicationContext()
    private val notifications get() = shadowOf(app.getSystemService(NotificationManager::class.java))

    @After
    fun tearDown() {
        ReminderWorker.clock = Clock.systemDefaultZone()
    }

    @Test
    fun todaysReminderIsShownOnceItsTimeHasCome() {
        allowNotifications()
        progress { dailyReminders() }

        // 7 pm until the hunter's usual time is learned.
        assertEquals(ListenableWorker.Result.success(), runWorker(at = LocalTime.of(19, 30)))

        val notification = notifications.getNotification(Reminder.Kind.DAILY.ordinal)
        assertNotNull(notification)
        assertEquals("daily", notification.channelId)
        val p = runBlocking { app.userData.progress.first() }
        assertEquals("one shown today", LocalDate.now().toEpochDay(), p.reminderDay)
        assertEquals(1, p.ignoredReminders)
        assertEquals(WorkInfo.State.ENQUEUED, reminderWork().state)
    }

    @Test
    fun nothingIsShownBeforeItsTimeOrAtNight() {
        allowNotifications()
        progress { dailyReminders() }

        runWorker(at = LocalTime.of(18, 59))
        runWorker(at = LocalTime.of(21, 30))

        assertTrue(notifications.allNotifications.isEmpty())
        assertEquals(WorkInfo.State.ENQUEUED, reminderWork().state)
    }

    @Test
    fun nothingIsShownOrCountedWithoutTheNotificationPermission() {
        progress { dailyReminders() }

        runWorker(at = LocalTime.of(19, 30))

        assertTrue(notifications.allNotifications.isEmpty())
        assertEquals(0, runBlocking { app.userData.progress.first() }.ignoredReminders)
    }

    @Test
    fun nothingIsShownOnceTodaysQuestionsAreDone() {
        allowNotifications()
        progress {
            dailyReminders()
            repeat(FieldTraining.DAILY_SPECIES + FieldTraining.DAILY_GENERAL) { recordDaily("q$it", true) }
        }

        assertEquals(ListenableWorker.Result.success(), runWorker(at = LocalTime.of(19, 30)))

        // Tomorrow's is planned instead.
        assertTrue(notifications.allNotifications.isEmpty())
        assertEquals(WorkInfo.State.ENQUEUED, reminderWork().state)
    }

    @Test
    fun nothingIsShownOrPlannedWithRemindersOff() {
        allowNotifications()
        progress { saveProfile(HunterType.HUNTER, setOf(Weapon.FIREARM), Province.QC) }

        assertEquals(ListenableWorker.Result.success(), runWorker(at = LocalTime.of(19, 30)))

        assertTrue(notifications.allNotifications.isEmpty())
        assertTrue(reminderWorks().all { it.state == WorkInfo.State.CANCELLED })
    }

    @Test
    fun aNewTimeZonePlansTheReminderAgain() {
        progress { dailyReminders() }
        cancelOncePlanned()

        app.sendBroadcast(Intent(Intent.ACTION_TIMEZONE_CHANGED).setPackage(app.packageName))
        shadowOf(Looper.getMainLooper()).idle()

        waitUntil { reminderWorks().any { it.state == WorkInfo.State.ENQUEUED } }
    }

    @Test
    fun otherBroadcastsAreIgnored() {
        progress { dailyReminders() }
        cancelOncePlanned()

        RescheduleReceiver().onReceive(app, Intent("ca.kallistesoft.hunt.OTHER"))
        Thread.sleep(200)

        assertTrue(reminderWorks().all { it.state == WorkInfo.State.CANCELLED })
    }

    private fun progress(block: suspend UserDataRepository.() -> Unit) = runBlocking { app.userData.block() }

    private suspend fun UserDataRepository.dailyReminders() {
        saveProfile(HunterType.HUNTER, setOf(Weapon.FIREARM), Province.QC)
        setDailyReminders(true)
    }

    private fun allowNotifications() = shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

    /** Runs the job today at [at], local time. */
    private fun runWorker(at: LocalTime): ListenableWorker.Result {
        val zone = ZoneId.systemDefault()
        ReminderWorker.clock = Clock.fixed(LocalDate.now().atTime(at).atZone(zone).toInstant(), zone)
        return runBlocking { TestListenableWorkerBuilder<ReminderWorker>(app).build().doWork() }
    }

    /** The app plans the job as the progress changes (in the background): waits for that, then cancels it. */
    private fun cancelOncePlanned() {
        waitUntil { reminderWorks().any { it.state == WorkInfo.State.ENQUEUED } }
        WorkManager.getInstance(app).cancelUniqueWork("reminder").result.get()
    }

    private fun waitUntil(condition: () -> Boolean) {
        val end = System.currentTimeMillis() + 5_000
        while (!condition()) {
            check(System.currentTimeMillis() < end) { "Timed out" }
            Thread.sleep(20)
        }
    }

    private fun reminderWorks(): List<WorkInfo> = WorkManager.getInstance(app).getWorkInfosForUniqueWork("reminder").get()

    private fun reminderWork(): WorkInfo = reminderWorks().single()
}
