package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing

/** Big entry card for a section of the app: icon, title, description and a few facts ("4 exams · Readiness 60 %"). */
@Composable
fun HubCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    facts: List<String>,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BhCard(modifier, containerColor = containerColor, border = null, onClick = onClick) {
        Row(Modifier.padding(Spacing.xl), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(
                icon,
                size = BadgeSize.ExtraLarge,
                containerColor = contentColor.copy(alpha = 0.12f),
                contentColor = contentColor,
            )
            Spacer(Modifier.width(Spacing.l))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = contentColor)
                Spacer(Modifier.height(Spacing.xs))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = contentColor.copy(alpha = 0.85f))
                if (facts.isNotEmpty()) {
                    Spacer(Modifier.height(Spacing.s))
                    Text(
                        facts.joinToString(" · "),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor,
                    )
                }
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = contentColor)
        }
    }
}
