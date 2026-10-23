package ca.kallistesoft.hunt.core.ui

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import ca.kallistesoft.hunt.core.model.FieldSkill
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK], qualifiers = ScreenshotDevice.LIGHT_EN)
class PresentationTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val day = LocalDate.of(2026, 11, 14).toEpochDay()

    @Test
    fun `each exam has its own icon, others a generic one`() {
        val icons = listOf("PAL", "ICAF", "ABHE", "ID").map(::examIcon)
        assertEquals(4, icons.toSet().size)
        assertTrue(Icons.Outlined.School !in icons)
        assertEquals(Icons.Outlined.School, examIcon("BOW"))
    }

    @Test
    fun `each field skill has its own name, summary and icon`() {
        val context = compose.activity
        val skills = FieldSkill.entries
        assertEquals(skills.size, skills.map { it.icon }.toSet().size)
        assertEquals(skills.size, skills.map { it.subRes }.toSet().size)
        assertEquals(
            listOf("Shot placement", "After the shot", "Safe firearm handling", "Rules in the field", "Survival and first aid"),
            skills.map { context.getString(it.nameRes) },
        )
    }

    @Test
    fun `dates and times read naturally in English and French`() {
        assertEquals("Saturday, November 14", longDate(day, Locale.CANADA))
        assertEquals("Samedi 14 novembre", longDate(day, Locale.CANADA_FRENCH))
        assertEquals("Nov 14", shortDate(day, Locale.CANADA))
        assertEquals("14 nov.", shortDate(day, Locale.CANADA_FRENCH))
        assertEquals("19 h 00", timeOfDay(19 * 60, Locale.CANADA_FRENCH).replace(' ', ' ').replace(' ', ' '))
        assertTrue(timeOfDay(7 * 60 + 5, Locale.US).startsWith("7:05"))
    }

    @Test
    fun `dates and times follow the device's language by default`() {
        val locale = Locale.getDefault()
        assertEquals(longDate(day, locale), longDate(day))
        assertEquals(shortDate(day, locale), shortDate(day))
        assertEquals(timeOfDay(19 * 60, locale), timeOfDay(19 * 60))
    }

    @Test
    fun `notifications already allowed are reported at once`() {
        shadowOf(compose.activity.application).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        var granted: Boolean? = null
        lateinit var ask: () -> Unit
        compose.setContent { ask = rememberNotificationPermission { granted = it } }
        compose.runOnIdle { ask() }
        compose.runOnIdle { assertEquals(true, granted) }
    }
}
