package ca.kallistesoft.hunt.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ca.kallistesoft.hunt.MainActivity
import ca.kallistesoft.hunt.R
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.model.Reminder
import ca.kallistesoft.hunt.core.model.UserProgress
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
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
    fun schedule(context: Context, progress: UserProgress) {
        val work = WorkManager.getInstance(context)
        val now = LocalDateTime.now()
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

    /** Shows [reminder] (if the hunter allowed notifications). */
    fun show(context: Context, reminder: Reminder, streak: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
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
                val left = reminder.daysLeft ?: 0
                val title = when {
                    left <= 1 -> res.getString(UiR.string.season_tomorrow)
                    left >= 14 -> res.getQuantityString(UiR.plurals.season_in_weeks, left / 7, left / 7)
                    else -> res.getQuantityString(UiR.plurals.season_in_days, left, left)
                }
                val date = LocalDate.ofEpochDay(reminder.seasonStart ?: reminder.day)
                    .format(DateTimeFormatter.ofPattern(if (Locale.getDefault().language == "fr") "d MMMM" else "MMMM d", Locale.getDefault()))
                Triple(CHANNEL_SEASON, title, res.getString(UiR.string.notif_season_text, date))
            }
        }
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
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

    private fun LocalTime.minuteOfDay(): Int = hour * 60 + minute
}

/** Wakes up at the planned time, shows the reminder if it's still due, then plans the next one. */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Deps {
        fun userData(): UserDataRepository
        fun today(): Today
    }

    override suspend fun doWork(): Result {
        val deps = EntryPointAccessors.fromApplication(applicationContext, Deps::class.java)
        val progress = deps.userData().progress.first()
        val today = deps.today()()
        val now = LocalTime.now()
        val nowMinute = now.hour * 60 + now.minute
        // Due now: today's reminder, even late (the job can be delayed by Doze), but never in the quiet hours.
        val due = Reminder.next(progress, today, nowMinute = 0)
        if (due != null && due.day == today && due.minute <= nowMinute && nowMinute < UserProgress.QUIET_FROM_MINUTE) {
            Reminders.show(applicationContext, due, progress.currentStreak(today))
        }
        // Planned from now, which is past this reminder's time: the next one is a later one.
        Reminders.schedule(applicationContext, progress)
        return Result.success()
    }
}
