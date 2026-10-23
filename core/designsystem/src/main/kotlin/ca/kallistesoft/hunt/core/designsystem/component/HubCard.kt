package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing

/** Accent of a section tile: green (exams) or orange ("What is it?"). */
enum class HubTone { Green, Orange }

/**
 * Entry tile for a section of the app, two side by side on the dashboard: tinted icon, title, description, and a
 * [footer] pinned to the bottom (readiness bar, facts). Give tiles in a row `fillMaxHeight()` under an
 * `IntrinsicSize.Min` row so they share the same height.
 */
@Composable
fun HubCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tone: HubTone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when (tone) {
        HubTone.Green -> scheme.secondaryContainer to scheme.secondary
        HubTone.Orange -> scheme.tertiaryContainer to scheme.tertiary
    }
    BhCard(modifier, onClick = onClick) {
        Column(
            Modifier.fillMaxHeight().padding(Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.s + Spacing.xxs),
        ) {
            IconBadge(icon, containerColor = container, contentColor = content)
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Column(Modifier.fillMaxWidth(), content = footer)
        }
    }
}
