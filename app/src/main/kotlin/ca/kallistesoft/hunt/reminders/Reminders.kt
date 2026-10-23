package ca.kallistesoft.hunt.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ca.kallistesoft.hunt.R
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.model.FieldRun
import ca.kallistesoft.hunt.core.model.Reminder
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.navigation.ReminderLink
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import ca.kallistesoft.hunt.core.ui.R as UiR

/**
 * Local reminders (DECISIONS.md, P11): nothing goes through a server. One unique WorkManager job waits for the next
 * reminder ([Reminder.next]); it's rescheduled whenever the progress changes and after each notification.
 */
object Reminders {
    private const val WORK = "reminder"
    private const val CHANNEL_DAILY = "daily"
    private const val CHANNEL_SEASON = "season"

    /** Plans the next reminder, or cancels the job when none is on. */
    fun schedule(context: Context, progress: UserProgress, now: LocalDateTime = LocalDateTime.now()) {
        val work = WorkManager.getInstance(context)
        val next = Reminder.next(progress, now.toLocalDate().toEpochDay(), now.toLocalTime().minuteOfDay())
        if (next == null) {
            work.cancelUniqueWork(WORK)
            return
        }
        val at = LocalDate.ofEpochDay(next.day).atTime(LocalTime.of(next.minute / 60, next.minute % 60))
        val delay = java.time.Duration.between(now, at).toMillis().coerceAtLeast(0)
        work.enqueueUniqueWork(
            WORK,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<ReminderWorker>().setInitialDelay(delay, TimeUnit.MILLISECONDS).build(),
        )
    }

    /** Shows [reminder] if the hunter allowed notifications; returns whether it was shown. */
    fun show(context: Context, reminder: Reminder, streak: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        createChannels(context)
        val res = context.resources
        val (channel, title, text) = when (reminder.kind) {
            Reminder.Kind.DAILY -> Triple(
                CHANNEL_DAILY,
                res.getString(UiR.string.notif_daily_title),
                if (streak > 0) res.getQuantityString(UiR.plurals.notif_daily_streak, streak, streak) else res.getString(UiR.string.notif_daily_text),
            )
            Reminder.Kind.SEASON -> {
                val start = reminder.seasonStart
                val left = reminder.daysLeft
                if (start == null || left == null) {
                    // No date for the coming season: late August.
                    Triple(
                        CHANNEL_SEASON,
                        res.getString(UiR.string.season_coming),
                        res.getString(UiR.string.season_coming_sub, FieldRun.SEASON_CHECK.size),
                    )
                } else {
                    val title = when {
                        left <= 1 -> res.getString(UiR.string.season_tomorrow)
                        left >= 14 -> res.getQuantityString(UiR.plurals.season_in_weeks, left / 7, left / 7)
                        else -> res.getQuantityString(UiR.plurals.season_in_days, left, left)
                    }
                    val date = LocalDate.ofEpochDay(start)
                        .format(DateTimeFormatter.ofPattern(if (Locale.getDefault().language == "fr") "d MMMM" else "MMMM d", Locale.getDefault()))
                    Triple(CHANNEL_SEASON, title, res.getString(UiR.string.notif_season_text, date))
                }
            }
        }
        // One requestCode per kind: each notification keeps its own intent (and its own screen, ReminderLink).
        val open = PendingIntent.getActivity(
            context,
            reminder.kind.ordinal,
            ReminderLink.intent(context, reminder.kind),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        @Suppress("MissingPermission") // Checked above.
        NotificationManagerCompat.from(context).notify(reminder.kind.ordinal, notification)
        return true
    }

    private fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(CHANNEL_DAILY, context.getString(UiR.string.notif_channel_daily), NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CHANNEL_SEASON, context.getString(UiR.string.notif_channel_season), NotificationManager.IMPORTANCE_DEFAULT),
            ),
        )
    }
}

private fun LocalTime.minuteOfDay(): Int = hour * 60 + minute

/** Wakes up at the planned time, shows the reminder if it's still due, then plans the next one. */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Deps {
        fun userData(): UserDataRepository
    }

    override suspend fun doWork(): Result {
        val userData = EntryPointAccessors.fromApplication(applicationContext, Deps::class.java).userData()
        val now = LocalDateTime.now(clock)
        val today = now.toLocalDate().toEpochDay()
        val progress = userData.progress.first()
        val due = Reminder.due(progress, today, now.toLocalTime().minuteOfDay())
        if (due != null && Reminders.show(applicationContext, due, progress.currentStreak(today))) {
            userData.reminderShown(due.kind, today)
        }
        // Planned from now, which is past this reminder's time: the next one is a later one.
        Reminders.schedule(applicationContext, userData.progress.first(), now)
        return Result.success()
    }

    companion object {
        /** The device clock; tests set a time of day, since WorkManager builds the worker itself. */
        @VisibleForTesting
        internal var clock: Clock = Clock.systemDefaultZone()
    }
}

/**
 * Plans the reminder again when the clock moves under it: a new time zone, the time set by hand, or a reboot. The
 * job waits a fixed delay, computed in local time.
 */
class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in RESCHEDULE_ACTIONS) return
        val pending = goAsync()
        val app = context.applicationContext
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val userData = EntryPointAccessors.fromApplication(app, ReminderWorker.Deps::class.java).userData()
                Reminders.schedule(app, userData.progress.first())
            } finally {
                pending.finish()
            }
        }
    }
}

private val RESCHEDULE_ACTIONS = setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_TIME_CHANGED)
