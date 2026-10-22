package ca.kallistesoft.hunt.feature.exams

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.SectionTitle
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.model.ExamGroup
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.currentLang
import ca.kallistesoft.hunt.core.ui.examIcon
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
    val scheme = MaterialTheme.colorScheme
    Scaffold(
        topBar = { BackTopAppBar("", onBack.takeUnless { firstRun }) },
        bottomBar = {
            Row(
                Modifier.navigationBarsPadding().padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (firstRun) {
                    TextButton(onClick = { viewModel.save(all = true, onDone) }, Modifier.testTag("picker-skip")) {
                        Text(stringResource(R.string.action_skip))
                    }
                }
                Button(
                    onClick = { viewModel.save(onSaved = onDone) },
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
                Text(stringResource(R.string.picker_subtitle), color = scheme.onSurfaceVariant)
            }
            for (group in ExamGroup.entries) {
                val exams = state.exams.filter { it.exam.group == group }
                if (exams.isEmpty()) continue
                item(key = group.name) { SectionTitle(stringResource(group.titleRes)) }
                items(exams, key = { it.exam.id }) { item ->
                    val selected = item.exam.id in state.chosen
                    PickerChoice(item, selected, onToggle = { viewModel.toggle(item.exam.id) })
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
            .padding(bottom = 8.dp)
            .testTag("picker-${item.exam.id}")
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onToggle() }),
        containerColor = if (selected) scheme.primaryContainer else scheme.surfaceContainerLow,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) scheme.primary else scheme.outlineVariant),
    ) {
        ListItem(
            headlineContent = { Text(item.exam.name(currentLang()), fontWeight = FontWeight.SemiBold) },
            supportingContent = {
                Text(
                    "${item.exam.region} · " +
                        pluralStringResource(R.plurals.questions_count, item.questionCount, item.questionCount),
                )
            },
            leadingContent = {
                Surface(Modifier.size(40.dp), shape = CircleShape, color = scheme.surface) {
                    Icon(examIcon(item.exam.id), contentDescription = null, Modifier.padding(8.dp), tint = scheme.primary)
                }
            },
            trailingContent = {
                Icon(
                    if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (selected) scheme.primary else scheme.onSurfaceVariant,
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}
