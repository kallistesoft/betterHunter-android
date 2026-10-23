package ca.kallistesoft.hunt.core.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.compose.ui.graphics.vector.ImageVector
import ca.kallistesoft.hunt.core.model.FieldSkill
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

@get:StringRes
val FieldSkill.nameRes: Int
    get() = when (this) {
        FieldSkill.SHOT_PLACEMENT -> R.string.skill_shot
        FieldSkill.AFTER_SHOT -> R.string.skill_after
        FieldSkill.SAFE_HANDLING -> R.string.skill_handling
        FieldSkill.RULES -> R.string.skill_rules
        FieldSkill.SURVIVAL -> R.string.skill_survival
    }

/** What the skill covers, in a few words. */
@get:StringRes
val FieldSkill.subRes: Int
    get() = when (this) {
        FieldSkill.SHOT_PLACEMENT -> R.string.skill_shot_sub
        FieldSkill.AFTER_SHOT -> R.string.skill_after_sub
        FieldSkill.SAFE_HANDLING -> R.string.skill_handling_sub
        FieldSkill.RULES -> R.string.skill_rules_sub
        FieldSkill.SURVIVAL -> R.string.skill_survival_sub
    }

val FieldSkill.icon: ImageVector
    get() = when (this) {
        FieldSkill.SHOT_PLACEMENT -> Icons.Outlined.GpsFixed
        FieldSkill.AFTER_SHOT -> Icons.Outlined.Pets
        FieldSkill.SAFE_HANDLING -> Icons.Outlined.Shield
        FieldSkill.RULES -> Icons.Outlined.Gavel
        FieldSkill.SURVIVAL -> Icons.Outlined.HealthAndSafety
    }

// One formatter per locale, built on first use: the season calendar formats a date for each of its ~35 day cells.
private val longDates = ConcurrentHashMap<Locale, DateTimeFormatter>()
private val shortDates = ConcurrentHashMap<Locale, DateTimeFormatter>()
private val timesOfDay = ConcurrentHashMap<Locale, DateTimeFormatter>()

/** "Saturday, November 14" / « samedi 14 novembre », for a day number. */
fun longDate(day: Long, locale: Locale = Locale.getDefault()): String {
    val formatter = longDates.getOrPut(locale) {
        DateTimeFormatter.ofPattern(if (locale.language == "fr") "EEEE d MMMM" else "EEEE, MMMM d", locale)
    }
    return LocalDate.ofEpochDay(day).format(formatter).replaceFirstChar { it.titlecase(locale) }
}

/** "Nov 14" / « 14 nov. », for a day number. */
fun shortDate(day: Long, locale: Locale = Locale.getDefault()): String {
    val formatter = shortDates.getOrPut(locale) { DateTimeFormatter.ofPattern(if (locale.language == "fr") "d MMM" else "MMM d", locale) }
    return LocalDate.ofEpochDay(day).format(formatter)
}

/** "7:00 PM" / « 19 h 00 », for minutes after midnight. */
fun timeOfDay(minute: Int, locale: Locale = Locale.getDefault()): String {
    val formatter = timesOfDay.getOrPut(locale) { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale) }
    return java.time.LocalTime.of(minute / 60, minute % 60).format(formatter)
}

/**
 * Asks for the notification permission (Android 13+; granted before), then calls back with the answer. Reminders are
 * only asked for at the moments that explain them (P11), never at onboarding.
 */
@Composable
fun rememberNotificationPermission(onResult: (granted: Boolean) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission(), onResult)
    return {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onResult(true)
        }
    }
}

/** Whether the app may post notifications now: permission granted (Android 13+) and not turned off in system settings. */
fun notificationsEnabled(context: Context): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()

/** Opens the system settings for this app's notifications (where the hunter turned them off). */
@Composable
fun rememberNotificationSettingsOpener(): () -> Unit {
    val context = LocalContext.current
    return {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        }
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
