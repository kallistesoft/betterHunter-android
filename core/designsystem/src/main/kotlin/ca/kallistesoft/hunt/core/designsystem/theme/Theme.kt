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

/** Colours Material 3 has no role for. */
@Immutable
data class ExtendedColors(
    /** Right answers and "Correct". */
    val success: Color,
    /** Filled part of a progress bar (readiness). */
    val meter: Color,
    /** The streak flame. */
    val streak: Color,
    /** Border of the orange-tinted cards ("Don't confuse it", warnings). */
    val warmBorder: Color,
    /** The current question in the progress segments. */
    val progressCurrent: Color,
    /** Dark fade under text over a photo, and the text on it (the same in light and dark). */
    val photoScrim: Color,
    val onPhoto: Color,
    val onPhotoMuted: Color,
    val onPhotoAccent: Color,
)

private val LightExtended = ExtendedColors(
    success = Color(0xFF2F6B3E),
    meter = Color(0xFF3F8A52),
    streak = Color(0xFFD95F16),
    warmBorder = Color(0xFFF3C9A6),
    progressCurrent = Color(0xFF17201A),
    photoScrim = Color(0xFF0E1611),
    onPhoto = Color(0xFFFFFFFF),
    onPhotoMuted = Color(0xFFE2E6DF),
    onPhotoAccent = Color(0xFFF7A06A),
)
private val DarkExtended = ExtendedColors(
    success = Color(0xFF8FD19E),
    meter = Color(0xFF8FD19E),
    streak = Color(0xFFF27B2B),
    warmBorder = Color(0xFF4A2F1B),
    progressCurrent = Color(0xFFF7F2EA),
    photoScrim = Color(0xFF0E1611),
    onPhoto = Color(0xFFEEF1EA),
    onPhotoMuted = Color(0xFFC4CEC5),
    onPhotoAccent = Color(0xFFF7A06A),
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtended }

// 10 dp letter and icon tiles, 14 dp buttons, 16 dp answers, 22 dp cards.
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/**
 * BetterHunter's theme, from the design mockup (design/README.md): fixed on purpose (no dynamic colour) so the app
 * always looks like its mockup and store listing.
 */
@Composable
fun BetterHunterTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalExtendedColors provides if (darkTheme) DarkExtended else LightExtended) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

object BetterHunterTheme {
    val extendedColors: ExtendedColors
        @Composable get() = LocalExtendedColors.current
}
