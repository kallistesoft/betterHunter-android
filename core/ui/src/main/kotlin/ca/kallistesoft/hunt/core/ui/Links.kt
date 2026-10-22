package ca.kallistesoft.hunt.core.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

const val CONTACT_EMAIL = "contact@kallistesoft.ca"

/** Opens external links (sources, emails); returns false instead of throwing when nothing can handle them. */
class LinkOpener(private val context: Context) {
    /** Web pages open in a Custom Tab, falling back to any app that can view the link. */
    fun open(url: String): Boolean {
        val uri = url.toUri()
        return try {
            CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, uri)
            true
        } catch (_: ActivityNotFoundException) {
            start(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    /** Opens a pre-filled email; no backend needed. */
    fun email(to: String, subject: String? = null, body: String? = null): Boolean {
        // Encoded by hand: some mail apps show '+' literally for spaces.
        val query = listOfNotNull(
            subject?.let { "subject=${Uri.encode(it)}" },
            body?.let { "body=${Uri.encode(it)}" },
        ).joinToString("&")
        val uri = "mailto:$to${if (query.isEmpty()) "" else "?$query"}".toUri()
        return start(Intent(Intent.ACTION_SENDTO, uri))
    }

    private fun start(intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

@Composable
fun rememberLinkOpener(): LinkOpener {
    val context = LocalContext.current
    return remember(context) { LinkOpener(context) }
}
