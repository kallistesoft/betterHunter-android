package ca.kallistesoft.hunt.feature.exams

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.IconBadge
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.icon
import ca.kallistesoft.hunt.core.ui.nameRes
import ca.kallistesoft.hunt.core.ui.quizzesRes

/** "My provinces": one switch per province adds its quizzes to Exam training, or hides them. */
@Composable
fun ProvincesScreen(onBack: () -> Unit, viewModel: ProvincesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProvincesContent(state, onBack, viewModel::setEnabled)
}

@Composable
internal fun ProvincesContent(
    state: ProvincesUiState,
    onBack: () -> Unit,
    onSetEnabled: (Province, Boolean) -> Unit,
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Scaffold(
        topBar = { BackTopAppBar(stringResource(R.string.my_provinces), onBack) },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        if (state.loading) return@Scaffold
        LazyColumn(
            Modifier.consumeWindowInsets(padding),
            contentPadding = padding.plus(top = Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            item { Text(stringResource(R.string.my_provinces_sub), color = muted) }
            item {
                BhCard {
                    state.provinces.forEachIndexed { i, item ->
                        if (i > 0) {
                            HorizontalDivider(
                                Modifier.padding(start = Sizes.listDividerInset),
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                        ProvinceRow(item, onToggle = { onSetEnabled(item.province, it) })
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null, tint = muted, modifier = Modifier.size(Sizes.iconS))
                    Spacer(Modifier.width(Spacing.s))
                    Text(stringResource(R.string.more_provinces), style = MaterialTheme.typography.bodySmall, color = muted)
                }
            }
        }
    }
}

@Composable
private fun ProvinceRow(item: ProvinceItem, onToggle: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(item.province.nameRes), style = MaterialTheme.typography.titleMedium) },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(stringResource(item.province.quizzesRes))
                if (item.isHome) HomeChip()
            }
        },
        leadingContent = { IconBadge(item.province.icon) },
        trailingContent = {
            // The last province on is locked, but still reads as on.
            val scheme = MaterialTheme.colorScheme
            Switch(
                checked = item.enabled,
                onCheckedChange = null,
                enabled = item.canToggle,
                colors = SwitchDefaults.colors(
                    // Off: the secondary text colour for thumb and border, so it stands out on the card in dark mode.
                    uncheckedThumbColor = scheme.onSurfaceVariant,
                    uncheckedBorderColor = scheme.onSurfaceVariant,
                    uncheckedTrackColor = scheme.surfaceContainerHighest,
                    disabledCheckedTrackColor = scheme.primary.copy(alpha = 0.5f),
                    disabledCheckedThumbColor = scheme.onPrimary,
                ),
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        // The whole row is the switch, for screen readers and a larger touch target.
        modifier = Modifier
            .testTag("province-${item.province.name}")
            .toggleable(value = item.enabled, enabled = item.canToggle, role = Role.Switch, onValueChange = onToggle),
    )
}

/** "Where you hunt", on the home province. */
@Composable
private fun HomeChip() {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
        Text(
            stringResource(R.string.where_you_hunt),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.xxs),
        )
    }
}
