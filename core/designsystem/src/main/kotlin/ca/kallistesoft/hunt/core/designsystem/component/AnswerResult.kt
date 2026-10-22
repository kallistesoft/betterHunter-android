package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
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
