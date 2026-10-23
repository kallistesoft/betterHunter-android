package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing

/** What sits at the end of a [SettingRow]: an arrow (opens a screen), "open in new" (leaves the app), or nothing. */
enum class RowEnd { Chevron, External, None }

/**
 * One row of a settings-style card: tinted icon tile, title, optional value or description under it, and an end mark
 * or a [control] (toggle). With [onClick], the whole row is the touch target. [orange] tints the tile like the
 * current selection (My provinces).
 */
@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    end: RowEnd = if (onClick != null) RowEnd.Chevron else RowEnd.None,
    orange: Boolean = false,
    control: (@Composable () -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val clickable = if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier
    Row(
        modifier.then(clickable).heightIn(min = Sizes.settingRowMinHeight).padding(horizontal = Spacing.l, vertical = Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(
            icon,
            containerColor = if (orange) scheme.tertiaryContainer else scheme.secondaryContainer,
            contentColor = if (orange) scheme.tertiary else scheme.secondary,
        )
        Spacer(Modifier.width(Spacing.l))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.width(Spacing.s))
        if (control != null) {
            control()
        } else {
            when (end) {
                RowEnd.Chevron -> Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = scheme.onSurfaceVariant)
                RowEnd.External -> Icon(
                    Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(Sizes.iconM),
                )
                RowEnd.None -> Unit
            }
        }
    }
}

/** A card of [SettingRow]s separated by inset dividers: call [rows] with one lambda per row. */
@Composable
fun SettingsCard(modifier: Modifier = Modifier, rows: List<@Composable ColumnScope.() -> Unit>) {
    BhCard(modifier) {
        rows.forEachIndexed { i, row ->
            if (i > 0) {
                HorizontalDivider(Modifier.padding(start = Sizes.listDividerInset), color = MaterialTheme.colorScheme.outlineVariant)
            }
            row()
        }
    }
}
