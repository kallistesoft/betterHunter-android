package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing

/** How an answer is shown: not chosen, chosen (mock exam), or revealed as right or wrong. */
enum class AnswerState { Idle, Selected, Correct, Wrong }

/**
 * One answer to a question. With a [letter] it is a full-width row (quiz); without, a compact centred tile (two by
 * two on the dashboard). [onClick] null makes it read-only. [stateDescription] tells screen readers whether it's the
 * right answer once revealed.
 */
@Composable
fun AnswerChoice(
    text: String,
    state: AnswerState,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    letter: String? = null,
    stateDescription: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val success = BetterHunterTheme.extendedColors.success
    val (border, fill) = when (state) {
        AnswerState.Idle -> scheme.outlineVariant to Color.Transparent
        AnswerState.Selected -> scheme.primary to scheme.primaryContainer
        AnswerState.Correct -> success to success.copy(alpha = 0.12f)
        AnswerState.Wrong -> scheme.error to scheme.error.copy(alpha = 0.12f)
    }
    val described = if (stateDescription != null) {
        modifier.semantics { this.stateDescription = stateDescription }
    } else {
        modifier
    }
    val content: @Composable () -> Unit = {
        if (letter != null) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                IconBadge(size = BadgeSize.Small, containerColor = scheme.secondaryContainer) {
                    Text(letter, style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.width(Spacing.m))
                Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                when (state) {
                    AnswerState.Correct -> Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = border)
                    AnswerState.Wrong -> Icon(Icons.Rounded.Cancel, contentDescription = null, tint = border)
                    else -> Unit
                }
            }
        } else {
            Box(
                Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = Spacing.s),
                contentAlignment = Alignment.Center,
            ) {
                Text(text, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
    val shape = MaterialTheme.shapes.small
    val stroke = BorderStroke(if (state == AnswerState.Idle) 1.dp else 2.dp, border)
    if (onClick != null) {
        Surface(onClick = onClick, modifier = described, shape = shape, color = fill, border = stroke, content = content)
    } else {
        Surface(modifier = described, shape = shape, color = fill, border = stroke, content = content)
    }
}
