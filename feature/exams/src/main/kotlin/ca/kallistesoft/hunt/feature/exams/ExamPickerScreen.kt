package ca.kallistesoft.hunt.feature.exams

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.designsystem.component.BadgeSize
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.SectionTitle
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.ExamGroup
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.ExamAvatar
import ca.kallistesoft.hunt.core.ui.ExamListItem
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.titleRes

/**
 * "Which exam are you preparing for?": shown on first launch ([firstRun]: no back arrow, "Skip" picks every exam)
 * and from "Change" in exam training.
 */
@Composable
fun ExamPickerScreen(
    firstRun: Boolean,
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: ExamPickerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ExamPickerContent(
        state = state,
        firstRun = firstRun,
        onToggle = viewModel::toggle,
        onContinue = { viewModel.save(onSaved = onDone) },
        onSkip = { viewModel.save(all = true, onSaved = onDone) },
        onBack = onBack,
    )
}

@Composable
internal fun ExamPickerContent(
    state: ExamPickerUiState,
    firstRun: Boolean,
    onToggle: (examId: String) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = { BackTopAppBar("", onBack.takeUnless { firstRun }) },
        bottomBar = {
            Row(
                Modifier.navigationBarsPadding().padding(Spacing.l).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                if (firstRun) {
                    TextButton(onClick = onSkip, Modifier.testTag("picker-skip")) {
                        Text(stringResource(R.string.action_skip))
                    }
                }
                Button(
                    onClick = onContinue,
                    enabled = state.chosen.isNotEmpty(),
                    modifier = Modifier.weight(1f).testTag("picker-continue"),
                ) {
                    Text(stringResource(R.string.action_continue))
                }
            }
        },
    ) { padding ->
        LazyColumn(contentPadding = padding.plus()) {
            item {
                Text(
                    stringResource(R.string.picker_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(stringResource(R.string.picker_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            for (group in ExamGroup.entries) {
                val exams = state.exams.filter { it.exam.group == group }
                if (exams.isEmpty()) continue
                item(key = group.name) { SectionTitle(stringResource(group.titleRes)) }
                items(exams, key = { it.exam.id }) { item ->
                    PickerChoice(item, selected = item.exam.id in state.chosen, onToggle = { onToggle(item.exam.id) })
                }
            }
        }
    }
}

@Composable
private fun PickerChoice(item: PickerExam, selected: Boolean, onToggle: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    BhCard(
        modifier = Modifier
            .padding(bottom = Spacing.s)
            .testTag("picker-${item.exam.id}")
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onToggle() }),
        containerColor = if (selected) scheme.primaryContainer else scheme.surfaceContainerLow,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) scheme.primary else scheme.outlineVariant),
    ) {
        ExamListItem(
            item.exam,
            item.questionCount,
            leading = {
                ExamAvatar(item.exam.id, size = BadgeSize.Medium, containerColor = scheme.surface, contentColor = scheme.primary)
            },
            trailing = {
                Icon(
                    if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (selected) scheme.primary else scheme.onSurfaceVariant,
                )
            },
        )
    }
}
