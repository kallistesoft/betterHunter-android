package ca.kallistesoft.hunt.navigation

import android.content.Context
import android.content.Intent
import androidx.navigation3.runtime.NavKey
import ca.kallistesoft.hunt.MainActivity
import ca.kallistesoft.hunt.core.model.FieldRun
import ca.kallistesoft.hunt.core.model.Reminder

/**
 * A reminder's notification opens its screen over Home: today's questions for the daily reminder, the season date
 * for the season one. The intent names the reminder's kind in one extra; [MainActivity] is exported, so the extra is
 * checked against the known kinds and anything else opens the app as usual.
 */
object ReminderLink {
    private const val EXTRA_REMINDER = "ca.kallistesoft.hunt.extra.REMINDER"

    /** Opens [MainActivity] (the running one if any) on [kind]'s screen. */
    fun intent(context: Context, kind: Reminder.Kind): Intent =
        Intent(context, MainActivity::class.java)
            .putExtra(EXTRA_REMINDER, kind.name)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

    /**
     * The back stack [intent] asks for, or null for a normal launch. Reopening the task from Recents replays the
     * notification's intent: that's a normal launch too.
     */
    fun backStack(intent: Intent?): List<NavKey>? {
        if (intent == null || intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) return null
        // Another app can send anything: extras it can't unparcel are ignored like an unknown kind.
        return backStack(runCatching { intent.getStringExtra(EXTRA_REMINDER) }.getOrNull())
    }

    /** Home, then the screen of the reminder [kind] names; null when it names none. */
    internal fun backStack(kind: String?): List<NavKey>? =
        when (Reminder.Kind.entries.firstOrNull { it.name == kind }) {
            Reminder.Kind.DAILY -> listOf(Home, FieldQuiz(FieldRun.DAILY))
            Reminder.Kind.SEASON -> listOf(Home, SeasonDate)
            null -> null
        }
}
