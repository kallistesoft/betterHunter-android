package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing

/**
 * Small pill with a few short options (FR / EN); the chosen one is filled, in green, or with [orange] in the
 * selection's blaze orange (a value the user picks, like the daily goal). [description] names it for screen readers.
 */
@Composable
fun PillToggle(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    description: String,
    modifier: Modifier = Modifier,
    orange: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier.semantics { contentDescription = description },
        shape = CircleShape,
        color = scheme.surfaceContainerLow,
        border = BorderStroke(Sizes.border, scheme.outline),
    ) {
        Row(Modifier.padding(Sizes.togglePadding).selectableGroup()) {
            options.forEachIndexed { i, label ->
                val isSelected = i == selected
                Box(
                    Modifier
                        .height(Sizes.toggleHeight)
                        .widthIn(min = Sizes.touchTarget)
                        .clip(CircleShape)
                        .background(
                            when {
                                !isSelected -> Color.Transparent
                                orange -> scheme.primaryContainer
                                else -> scheme.secondaryContainer
                            },
                        )
                        .then(if (isSelected && orange) Modifier.border(Sizes.border, scheme.primary, CircleShape) else Modifier)
                        .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(i) })
                        .padding(horizontal = Spacing.s)
                        .testTag("toggle-$label"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) scheme.onSurface else scheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
