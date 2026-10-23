package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.BorderStroke
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
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing

enum class NoticeTone { Warning, Info }

/** A short message in a tinted card: a warning (orange, sample content) or information (green, disclaimer). */
@Composable
fun NoticeCard(text: String, tone: NoticeTone, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    val scheme = MaterialTheme.colorScheme
    val (container, border, iconTint) = when (tone) {
        NoticeTone.Warning -> Triple(scheme.primaryContainer, BetterHunterTheme.extendedColors.warmBorder, scheme.tertiary)
        NoticeTone.Info -> Triple(scheme.secondaryContainer, scheme.outlineVariant, scheme.secondary)
    }
    BhCard(modifier, containerColor = container, border = BorderStroke(Sizes.border, border), shape = MaterialTheme.shapes.medium) {
        Row(Modifier.padding(Spacing.l), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = iconTint)
                Spacer(Modifier.width(Spacing.m))
            }
            Text(text, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurface)
        }
    }
}
