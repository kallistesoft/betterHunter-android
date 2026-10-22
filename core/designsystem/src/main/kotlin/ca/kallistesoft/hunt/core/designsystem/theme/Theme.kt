package ca.kallistesoft.hunt.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Colours Material 3 has no role for: right answers and the streak flame. */
@Immutable
data class ExtendedColors(val success: Color, val streak: Color)

private val LightExtended = ExtendedColors(success = Color(0xFF2E7D32), streak = Color(0xFFE64A19))
private val DarkExtended = ExtendedColors(success = Color(0xFF81C784), streak = Color(0xFFFF8A65))

val LocalExtendedColors = staticCompositionLocalOf { LightExtended }

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
)

/**
 * BetterHunter's theme. The brand green is fixed on purpose (no dynamic colour) so the app always matches its icon
 * and store listing.
 */
@Composable
fun BetterHunterTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalExtendedColors provides if (darkTheme) DarkExtended else LightExtended) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            shapes = AppShapes,
            content = content,
        )
    }
}

object BetterHunterTheme {
    val extendedColors: ExtendedColors
        @Composable get() = LocalExtendedColors.current
}
