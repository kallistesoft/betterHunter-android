package ca.kallistesoft.hunt.core.ui

import android.content.ActivityNotFoundException
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK])
class LinkOpenerTest {
    /** Records the intents started; [canHandle] decides which ones an installed app can open. */
    private class FakeContext(private val canHandle: (Intent) -> Boolean) : ContextWrapper(RuntimeEnvironment.getApplication()) {
        val started = mutableListOf<Intent>()

        override fun startActivity(intent: Intent) = startActivity(intent, null)

        override fun startActivity(intent: Intent, options: Bundle?) {
            if (!canHandle(intent)) throw ActivityNotFoundException()
            started += intent
        }
    }

    @Test
    fun `web pages open in a Custom Tab`() {
        val context = FakeContext { true }
        assertTrue(LinkOpener(context).open("https://example.org/page"))
        val intent = context.started.single()
        assertEquals("https://example.org/page", intent.dataString)
        assertTrue("Custom Tab", intent.hasExtra("android.support.customtabs.extra.SESSION"))
    }

    @Test
    fun `without Custom Tabs any app that views the link opens it`() {
        val context = FakeContext { !it.hasExtra("android.support.customtabs.extra.SESSION") }
        assertTrue(LinkOpener(context).open("https://example.org/page"))
        val intent = context.started.single()
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun `nothing to open the link with is not an error`() {
        val context = FakeContext { false }
        assertFalse(LinkOpener(context).open("https://example.org/page"))
        assertFalse(LinkOpener(context).email(CONTACT_EMAIL))
    }

    @Test
    fun `emails are pre-filled with spaces encoded as percent-20`() {
        val context = FakeContext { true }
        val opener = LinkOpener(context)
        assertTrue(opener.email(CONTACT_EMAIL, subject = "Bad question", body = "q1 & q2"))
        assertTrue(opener.email(CONTACT_EMAIL))

        val (filled, plain) = context.started
        assertEquals(Intent.ACTION_SENDTO, filled.action)
        assertEquals("mailto:contact@kallistesoft.ca?subject=Bad%20question&body=q1%20%26%20q2", filled.dataString)
        assertEquals("mailto:contact@kallistesoft.ca", plain.dataString)
    }
}
