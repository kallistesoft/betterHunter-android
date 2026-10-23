package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing

/** How an answer is shown: not chosen, chosen (mock exam), or revealed as right or wrong. */
enum class AnswerState { Idle, Selected, Correct, Wrong }

/** Colours of an answer in one state: its fill and border, and its letter tile. */
private data class AnswerColors(val fill: Color, val border: Color, val tile: Color, val onTile: Color)

@Composable
private fun answerColors(state: AnswerState): AnswerColors {
    val scheme = MaterialTheme.colorScheme
    val success = BetterHunterTheme.extendedColors.success
    return when (state) {
        AnswerState.Idle -> AnswerColors(scheme.surfaceContainerLow, scheme.outline, scheme.surfaceContainerHighest, scheme.onSurfaceVariant)
        AnswerState.Selected -> AnswerColors(scheme.primaryContainer, scheme.primary, scheme.primary, scheme.onPrimary)
        AnswerState.Correct -> AnswerColors(scheme.secondaryContainer, success, success, scheme.onSecondary)
        AnswerState.Wrong -> AnswerColors(scheme.errorContainer, scheme.error, scheme.error, scheme.onError)
    }
}

/**
 * One answer to a question. With a [letter] it is a full-width row with a letter tile (quiz); without, a tile for a
 * two-by-two grid ("What is it?"), with an optional [subtitle] (the name in the other language). [onClick] null makes
 * it read-only. [stateDescription] tells screen readers whether it's the right answer once revealed.
 */
@Composable
fun AnswerChoice(
    text: String,
    state: AnswerState,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    letter: String? = null,
    subtitle: String? = null,
    stateDescription: String? = null,
) {
    val colors = answerColors(state)
    val described = if (stateDescription != null) {
        modifier.semantics { this.stateDescription = stateDescription }
    } else {
        modifier
    }
    val content: @Composable () -> Unit = {
        if (letter != null) {
            Row(
                Modifier
                    .heightIn(min = Sizes.answerRowMinHeight)
                    .padding(horizontal = Sizes.answerPadding, vertical = Spacing.s + Spacing.xxs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconBadge(size = BadgeSize.Small, containerColor = colors.tile, contentColor = colors.onTile) {
                    Text(letter, style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.width(Spacing.m + Spacing.xxs))
                Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                when (state) {
                    AnswerState.Correct -> Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = colors.border)
                    AnswerState.Wrong -> Icon(Icons.Rounded.Cancel, contentDescription = null, tint = colors.border)
                    else -> Unit
                }
            }
        } else {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = Sizes.answerTileMinHeight)
                    .padding(horizontal = Sizes.answerPadding, vertical = Spacing.m),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs, Alignment.CenterVertically),
            ) {
                Text(
                    text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
    val shape = MaterialTheme.shapes.medium
    val stroke = BorderStroke(Sizes.borderEmphasis, colors.border)
    val textColor = MaterialTheme.colorScheme.onSurface
    if (onClick != null) {
        Surface(onClick = onClick, modifier = described, shape = shape, color = colors.fill, contentColor = textColor, border = stroke, content = content)
    } else {
        Surface(modifier = described, shape = shape, color = colors.fill, contentColor = textColor, border = stroke, content = content)
    }
}
