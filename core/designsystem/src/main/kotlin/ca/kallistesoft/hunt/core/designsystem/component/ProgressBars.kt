package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing

/** Thin rounded bar filled to [progress] (0..1). */
@Composable
fun MeterBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = BetterHunterTheme.extendedColors.meter,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    height: Dp = Sizes.meterBar,
) {
    val value = progress.coerceIn(0f, 1f)
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(trackColor)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..1f) },
    ) {
        Box(Modifier.fillMaxWidth(value).fillMaxHeight().clip(CircleShape).background(color))
    }
}

/** A bar with its label and value above it ("Readiness … 64 %"). */
@Composable
fun LabelledMeter(label: String, value: String, progress: Float, modifier: Modifier = Modifier) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier.clearAndSetSemantics { contentDescription = "$label $value" },
        verticalArrangement = Arrangement.spacedBy(Spacing.xs + Spacing.xxs),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Normal, color = muted)
            Text(value, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        }
        MeterBar(progress)
    }
}

/** How one question of a run is shown in [QuizProgress]. */
enum class SegmentState { Upcoming, Current, Right, Wrong }

/**
 * Progress through a quiz: one segment per question (answered ones green or red, the current one bright), or, when
 * [segments] is null (mock exams, where answers stay hidden), one bar filled to [progress].
 */
@Composable
fun QuizProgress(segments: List<SegmentState>?, progress: Float, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val extended = BetterHunterTheme.extendedColors
    if (segments == null) {
        MeterBar(progress, modifier, color = extended.progressCurrent, height = Sizes.progressBar)
        return
    }
    Row(
        modifier.fillMaxWidth().semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f) },
        horizontalArrangement = Arrangement.spacedBy(Sizes.segmentGap),
    ) {
        segments.forEach { state ->
            val color = when (state) {
                SegmentState.Upcoming -> scheme.surfaceContainerHighest
                SegmentState.Current -> extended.progressCurrent
                SegmentState.Right -> extended.success
                SegmentState.Wrong -> scheme.error
            }
            Box(Modifier.weight(1f).height(Sizes.progressBar).clip(CircleShape).background(color))
        }
    }
}
