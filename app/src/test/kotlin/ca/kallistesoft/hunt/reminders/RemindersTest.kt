package ca.kallistesoft.hunt.reminders

import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import ca.kallistesoft.hunt.MainActivity
import ca.kallistesoft.hunt.core.model.FieldRun
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.Reminder
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.Weapon
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.util.Locale
import java.util.concurrent.TimeUnit
import ca.kallistesoft.hunt.core.ui.R as UiR

/** Planning the reminder job, and the notifications it shows. A plain Application: no Hilt needed here. */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class RemindersTest {
    private val context: Application = ApplicationProvider.getApplicationContext()
    private val notifications get() = shadowOf(context.getSystemService(NotificationManager::class.java))
    private val defaultLocale = Locale.getDefault()

    @Before
    fun setUp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @After
    fun tearDown() = Locale.setDefault(defaultLocale)

    @Test
    fun aFieldHunterWithRemindersOnGetsTheJobPlanned() {
        Reminders.schedule(context, fieldHunter.copy(dailyReminders = true))

        val work = reminderWork()
        assertEquals(WorkInfo.State.ENQUEUED, work.state)
        // At the hunter's time today or tomorrow: never more than a day away.
        assertTrue(work.initialDelayMillis in 0..TimeUnit.DAYS.toMillis(1))
    }

    @Test
    fun turningRemindersOffCancelsTheJob() {
        Reminders.schedule(context, fieldHunter.copy(dailyReminders = true))
        Reminders.schedule(context, fieldHunter)

        assertEquals(WorkInfo.State.CANCELLED, reminderWork().state)
    }

    @Test
    fun nothingIsShownWithoutTheNotificationPermission() {
        assertFalse(Reminders.show(context, Reminder(Reminder.Kind.DAILY, DAY, MINUTE), streak = 0))

        assertTrue(notifications.allNotifications.isEmpty())
    }

    @Test
    fun theDailyReminderOpensTheApp() {
        allowNotifications()
        Reminders.show(context, Reminder(Reminder.Kind.DAILY, DAY, MINUTE), streak = 0)

        val notification = notifications.getNotification(Reminder.Kind.DAILY.ordinal)
        assertEquals("daily", notification.channelId)
        assertEquals(context.getString(UiR.string.notif_daily_title), notification.title)
        assertEquals(context.getString(UiR.string.notif_daily_text), notification.text)
        assertEquals(MainActivity::class.java.name, shadowOf(notification.contentIntent).savedIntent.component?.className)
        assertNotNull(context.getSystemService(NotificationManager::class.java).getNotificationChannel("daily"))
        assertNotNull(context.getSystemService(NotificationManager::class.java).getNotificationChannel("season"))
    }

    @Test
    fun theDailyReminderMentionsTheStreak() {
        allowNotifications()
        Reminders.show(context, Reminder(Reminder.Kind.DAILY, DAY, MINUTE), streak = 4)

        val notification = notifications.getNotification(Reminder.Kind.DAILY.ordinal)
        assertEquals(context.resources.getQuantityString(UiR.plurals.notif_daily_streak, 4, 4), notification.text)
    }

    @Test
    fun seasonRemindersCountDownInWeeksThenDays() {
        allowNotifications()
        Locale.setDefault(Locale.CANADA)

        assertEquals(context.resources.getQuantityString(UiR.plurals.season_in_weeks, 3, 3), seasonTitle(daysLeft = 21))
        assertEquals(context.resources.getQuantityString(UiR.plurals.season_in_days, 5, 5), seasonTitle(daysLeft = 5))
        assertEquals(context.getString(UiR.string.season_tomorrow), seasonTitle(daysLeft = 1))
        val notification = notifications.getNotification(Reminder.Kind.SEASON.ordinal)
        assertEquals("season", notification.channelId)
        assertEquals(context.getString(UiR.string.notif_season_text, "March 15"), notification.text)
    }

    @Test
    fun theSeasonDateFollowsFrenchWordOrder() {
        allowNotifications()
        Locale.setDefault(Locale.CANADA_FRENCH)

        seasonTitle(daysLeft = 21)

        val notification = notifications.getNotification(Reminder.Kind.SEASON.ordinal)
        assertEquals(context.getString(UiR.string.notif_season_text, "15 mars"), notification.text)
    }

    @Test
    fun withoutADateLateAugustSaysTheSeasonIsComing() {
        allowNotifications()

        assertTrue(Reminders.show(context, Reminder(Reminder.Kind.SEASON, DAY, MINUTE), streak = 0))

        val notification = notifications.getNotification(Reminder.Kind.SEASON.ordinal)
        assertEquals(context.getString(UiR.string.season_coming), notification.title)
        assertEquals(context.getString(UiR.string.season_coming_sub, FieldRun.SEASON_CHECK.size), notification.text)
    }

    @Test
    fun theJobWaitsUntilTheReminderInLocalTime() {
        val now = LocalDate.ofEpochDay(DAY).atTime(9, 0)

        Reminders.schedule(context, fieldHunter.copy(dailyReminders = true), now)

        assertEquals(TimeUnit.HOURS.toMillis(10), reminderWork().initialDelayMillis)
    }

    /** Shows a season reminder [daysLeft] days before March 15 and returns its title. */
    private fun seasonTitle(daysLeft: Int): String {
        val start = LocalDate.of(2027, 3, 15).toEpochDay()
        Reminders.show(context, Reminder(Reminder.Kind.SEASON, start - daysLeft, MINUTE, seasonStart = start), streak = 0)
        return notifications.getNotification(Reminder.Kind.SEASON.ordinal).title
    }

    private fun allowNotifications() = shadowOf(context).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)

    private fun reminderWork(): WorkInfo = WorkManager.getInstance(context).getWorkInfosForUniqueWork("reminder").get().single()

    private val Notification.title get() = extras.getCharSequence(Notification.EXTRA_TITLE).toString()
    private val Notification.text get() = extras.getCharSequence(Notification.EXTRA_TEXT).toString()

    private companion object {
        const val DAY = 20_000L
        const val MINUTE = 19 * 60
        val fieldHunter = UserProgress().withProfile(HunterType.NEW_HUNTER, setOf(Weapon.FIREARM), Province.QC)
    }
}
