package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing

/** "Correct!" or "Incorrect", with its icon, in green or red. */
@Composable
fun AnswerResult(
    correct: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
) {
    val color = if (correct) BetterHunterTheme.extendedColors.success else MaterialTheme.colorScheme.error
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(if (correct) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel, contentDescription = null, tint = color)
        Spacer(Modifier.width(Spacing.s))
        Text(text, style = style, fontWeight = FontWeight.Bold, color = color)
    }
}

/** Filled pill with "Correct" or "Incorrect", laid over the photo of an answered "What is it?" question. */
@Composable
fun ResultChip(correct: Boolean, text: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier,
        shape = CircleShape,
        color = if (correct) BetterHunterTheme.extendedColors.success else scheme.error,
        contentColor = if (correct) scheme.onSecondary else scheme.onError,
    ) {
        Row(
            Modifier
                .heightIn(min = Sizes.chipHeight)
                .padding(start = Spacing.s, end = Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(if (correct) Icons.Rounded.Check else Icons.Rounded.Close, contentDescription = null, Modifier.size(Sizes.iconM))
            Spacer(Modifier.width(Spacing.xs))
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}
