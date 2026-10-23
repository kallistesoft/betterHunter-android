package ca.kallistesoft.hunt.core.ui

import android.os.Build
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
import androidx.compose.ui.graphics.vector.ImageVector
import ca.kallistesoft.hunt.core.model.FieldSkill
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

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

/** "Saturday, November 14" / « samedi 14 novembre », for a day number. */
fun longDate(day: Long, locale: Locale = Locale.getDefault()): String =
    LocalDate.ofEpochDay(day).format(DateTimeFormatter.ofPattern(if (locale.language == "fr") "EEEE d MMMM" else "EEEE, MMMM d", locale))
        .replaceFirstChar { it.titlecase(locale) }

/** "Nov 14" / « 14 nov. », for a day number. */
fun shortDate(day: Long, locale: Locale = Locale.getDefault()): String =
    LocalDate.ofEpochDay(day).format(DateTimeFormatter.ofPattern(if (locale.language == "fr") "d MMM" else "MMM d", locale))

/** "7:00 PM" / « 19 h 00 », for minutes after midnight. */
fun timeOfDay(minute: Int, locale: Locale = Locale.getDefault()): String =
    java.time.LocalTime.of(minute / 60, minute % 60).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))

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
