package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing

/** One answer among several ([Single]: radio, e.g. "Where do you hunt?") or one of many ([Multiple]: checkbox). */
enum class ChoiceMode { Single, Multiple }

/**
 * A large selectable card for onboarding questions: icon tile, title, description and a radio or check mark. Selected,
 * it turns blaze orange (fill, border, icon tile and mark). The whole card is the touch target.
 */
@Composable
fun ChoiceCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    mode: ChoiceMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val selection = when (mode) {
        ChoiceMode.Single -> Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
        ChoiceMode.Multiple -> Modifier.toggleable(value = selected, role = Role.Checkbox, onValueChange = { onClick() })
    }
    Surface(
        modifier,
        shape = MaterialTheme.shapes.large,
        color = if (selected) scheme.primaryContainer else scheme.surfaceContainerLow,
        border = BorderStroke(if (selected) Sizes.borderEmphasis else Sizes.border, if (selected) scheme.primary else scheme.outlineVariant),
    ) {
        Row(
            selection.heightIn(min = Sizes.choiceMinHeight).padding(Spacing.l),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(
                icon,
                containerColor = if (selected) scheme.tertiaryContainer else scheme.secondaryContainer,
                contentColor = if (selected) scheme.tertiary else scheme.secondary,
            )
            Spacer(Modifier.width(Spacing.l))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(Spacing.m))
            SelectionMark(selected, if (mode == ChoiceMode.Single) CircleShape else RoundedCornerShape(Sizes.checkboxCorner))
        }
    }
}

/** The radio (round) or checkbox (square) mark: an outline, or filled orange with a check. */
@Composable
private fun SelectionMark(selected: Boolean, shape: Shape) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        Modifier.size(Sizes.selectionMark),
        shape = shape,
        color = if (selected) scheme.primary else scheme.surfaceContainerLow,
        contentColor = scheme.onPrimary,
        border = if (selected) null else BorderStroke(Sizes.borderEmphasis, scheme.onSurfaceVariant),
    ) {
        if (selected) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Check, contentDescription = null, Modifier.size(Sizes.iconS))
            }
        }
    }
}
