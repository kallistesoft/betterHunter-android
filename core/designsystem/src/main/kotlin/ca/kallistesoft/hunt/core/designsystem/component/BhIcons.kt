package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes

/** Icons drawn for BetterHunter (the rest come from Material icons). */
object BhIcons {
    /** The antlers of the dashboard header (stroke icon; tint it with the main orange). */
    val Antlers: ImageVector by lazy {
        val paths = listOf(
            "M13 17c-3-1.5-6-3.5-7.5-7.5",
            "M9.5 13.5c-.5-2.5 0-4.5 1.5-6.5",
            "M19 17c3-1.5 6-3.5 7.5-7.5",
            "M22.5 13.5c.5-2.5 0-4.5-1.5-6.5",
            "M12.5 17.5c0-1.5 2-2 3.5-2s3.5.5 3.5 2c0 4-1.5 8-3 10.5-.3.5-.7.5-1 0-1.5-2.5-3-6.5-3-10.5z",
        )
        ImageVector.Builder("Antlers", defaultWidth = Sizes.logo, defaultHeight = Sizes.logo, viewportWidth = 32f, viewportHeight = 32f)
            .apply {
                for (d in paths) {
                    addPath(
                        pathData = PathParser().parsePathString(d).toNodes(),
                        fill = null,
                        stroke = SolidColor(Color.Black),
                        strokeLineWidth = 2.4f,
                        strokeLineCap = StrokeCap.Round,
                        strokeLineJoin = StrokeJoin.Round,
                    )
                }
            }
            .build()
    }
}
