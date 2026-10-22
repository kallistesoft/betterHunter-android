package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing

enum class NoticeTone { Warning, Info }

/** A short message in a tinted card: a warning (sample content) or information (disclaimer). */
@Composable
fun NoticeCard(text: String, tone: NoticeTone, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when (tone) {
        NoticeTone.Warning -> scheme.errorContainer to scheme.onErrorContainer
        NoticeTone.Info -> scheme.tertiaryContainer.copy(alpha = 0.5f) to scheme.onSurface
    }
    BhCard(modifier, containerColor = container, border = null) {
        Row(Modifier.padding(Spacing.l), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = content)
                Spacer(Modifier.width(Spacing.m))
            }
            Text(text, color = content)
        }
    }
}
