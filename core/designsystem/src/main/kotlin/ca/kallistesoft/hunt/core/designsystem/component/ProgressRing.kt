package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing

/** Circular progress (0..1) with content in the middle: readiness, daily goal, score. */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = Sizes.ringS,
    strokeWidth: Dp = Sizes.ringStrokeS,
    color: Color = ProgressIndicatorDefaults.circularColor,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    content: @Composable () -> Unit = {},
) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxSize(),
            color = color,
            strokeWidth = strokeWidth,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round,
            gapSize = Spacing.none,
        )
        content()
    }
}
