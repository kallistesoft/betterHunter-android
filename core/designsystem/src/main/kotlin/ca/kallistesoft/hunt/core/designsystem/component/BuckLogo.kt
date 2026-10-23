package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes

// The "blaze buck" logo (design/mockup/Logos.dc.html, concept A) on its 64-unit grid. The app icon draws the same
// shapes (app/src/main/res/drawable/ic_launcher_foreground.xml); keep them in step.
private val Antlers = listOf(
    "M27 29C23 23 19.5 17 19.5 8", "M21.2 18.5L13.5 14", "M20 12.5L25.5 7.5",
    "M37 29C41 23 44.5 17 44.5 8", "M42.8 18.5L50.5 14", "M44 12.5L38.5 7.5",
)
private const val Ears = "M22.5 31.5L11 27.5L20 36.5Z M41.5 31.5L53 27.5L44 36.5Z"

/** The head, with the eyes and nose cut out so the background shows through. */
private const val Face = "M21 30H43L38.5 49C36.5 56.5 27.5 56.5 25.5 49Z " +
    "M25.6 38.5a1.9 1.9 0 1 0 3.8 0a1.9 1.9 0 1 0 -3.8 0Z " +
    "M34.6 38.5a1.9 1.9 0 1 0 3.8 0a1.9 1.9 0 1 0 -3.8 0Z " +
    "M29.2 51.5a2.8 2.2 0 1 0 5.6 0a2.8 2.2 0 1 0 -5.6 0Z"

private fun buckVector(face: Color, antlers: Color): ImageVector =
    ImageVector.Builder("Buck", defaultWidth = Sizes.logo, defaultHeight = Sizes.logo, viewportWidth = 64f, viewportHeight = 64f)
        .apply {
            for (d in Antlers) {
                addPath(
                    pathData = PathParser().parsePathString(d).toNodes(),
                    stroke = SolidColor(antlers),
                    strokeLineWidth = 3.6f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
            addPath(PathParser().parsePathString(Ears).toNodes(), fill = SolidColor(face))
            addPath(PathParser().parsePathString(Face).toNodes(), pathFillType = PathFillType.EvenOdd, fill = SolidColor(face))
        }
        .build()

/** BetterHunter's logo: the buck head in the text colour with blaze-orange antlers (dashboard header). */
@Composable
fun BuckLogo(
    modifier: Modifier = Modifier,
    face: Color = MaterialTheme.colorScheme.onSurface,
    antlers: Color = MaterialTheme.colorScheme.primary,
) {
    val vector = remember(face, antlers) { buckVector(face, antlers) }
    Image(vector, contentDescription = null, modifier = modifier.size(Sizes.logo))
}
