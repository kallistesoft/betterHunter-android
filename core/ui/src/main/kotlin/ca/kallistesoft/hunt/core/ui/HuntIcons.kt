package ca.kallistesoft.hunt.core.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes

/**
 * Line icons Material Icons doesn't have, drawn from the design mockup's icon set (design/generator/screens.py) on its
 * 24-unit grid, 2-unit strokes. Tinted like any icon (`Icon(tint = …)`).
 */
object HuntIcons {
    val Bow: ImageVector by lazy {
        lineIcon("Bow", "M7 3c6 3.5 6 14.5 0 18", "M7 3v18", "M3 12h17", "M17 9l3 3-3 3")
    }

    val Crossbow: ImageVector by lazy {
        lineIcon("Crossbow", "M3 8c5-4 13-4 18 0", "M3 8l9 5 9-5", "M12 3v18", "M10 18h4")
    }

    /** "Elsewhere in Canada". */
    val Maple: ImageVector by lazy {
        lineIcon(
            "Maple",
            "M12 3l1.6 3.6 2.4-1-.6 4 3-1-1 3 2.6 1.4-4.2 3.2.6 2.3-3.4-.8V21h-2v-3.3l-3.4.8.6-2.3L4 13l2.6-1.4-1-3 3 1-.6-4 2.4 1z",
        )
    }

    /** "Experienced hunter": the logo's antlers. */
    val Antlers: ImageVector by lazy {
        lineIcon(
            "Antlers",
            "M10 21v-6C8.5 13 7 10.5 7 6", "M7.7 10L5 8.5", "M7.2 7.6L9 5.8",
            "M14 21v-6c1.5-2 3-4.5 3-9", "M16.3 10L19 8.5", "M16.8 7.6L15 5.8",
            "M10 15h4",
        )
    }

    private fun lineIcon(name: String, vararg paths: String): ImageVector =
        ImageVector.Builder(name, defaultWidth = Sizes.iconL, defaultHeight = Sizes.iconL, viewportWidth = 24f, viewportHeight = 24f)
            .apply {
                for (d in paths) {
                    addPath(
                        pathData = PathParser().parsePathString(d).toNodes(),
                        // Icon() replaces the colour with its tint.
                        stroke = SolidColor(Color.Black),
                        strokeLineWidth = 2f,
                        strokeLineCap = StrokeCap.Round,
                        strokeLineJoin = StrokeJoin.Round,
                    )
                }
            }
            .build()
}
