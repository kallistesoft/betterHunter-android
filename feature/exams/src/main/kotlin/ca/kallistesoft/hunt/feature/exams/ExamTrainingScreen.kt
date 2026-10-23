package ca.kallistesoft.hunt.feature.exams

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.IconBadge
import ca.kallistesoft.hunt.core.designsystem.component.LinkButton
import ca.kallistesoft.hunt.core.designsystem.component.SectionTitle
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.ExamCard
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.nameRes

/**
 * "Exam training": the user's exams as full cards (their provinces set them), then "Other provinces" to add more.
 * A hunter course's card offers "I passed the real exam".
 */
@Composable
fun ExamTrainingScreen(
    onBack: () -> Unit,
    onOpenProvinces: () -> Unit,
    onStartQuiz: (examId: String, mode: QuizMode) -> Unit,
    onPassedRealExam: (examId: String) -> Unit,
    viewModel: ExamsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ExamTrainingContent(state, onBack, onOpenProvinces, onStartQuiz, onPassedRealExam)
}

@Composable
internal fun ExamTrainingContent(
    state: ExamsUiState,
    onBack: () -> Unit,
    onOpenProvinces: () -> Unit,
    onStartQuiz: (examId: String, mode: QuizMode) -> Unit,
    onPassedRealExam: (examId: String) -> Unit,
) {
    Scaffold(
        topBar = { BackTopAppBar(stringResource(R.string.hub_exams), onBack) },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        LazyColumn(Modifier.consumeWindowInsets(padding), contentPadding = padding.plus()) {
            item {
                SectionTitle(stringResource(R.string.my_exams)) {
                    LinkButton(onClick = onOpenProvinces, modifier = Modifier.testTag("change-exams")) {
                        Icon(Icons.Rounded.Tune, contentDescription = null, Modifier.size(Sizes.iconM))
                        Spacer(Modifier.width(Spacing.s))
                        Text(stringResource(R.string.action_change))
                    }
                }
            }
            items(state.mine, key = { it.exam.id }, contentType = { "exam" }) { summary ->
                val id = summary.exam.id
                ExamCard(
                    summary,
                    onPractice = { onStartQuiz(id, QuizMode.PRACTICE) },
                    onExam = { onStartQuiz(id, QuizMode.EXAM) },
                    onReview = { onStartQuiz(id, QuizMode.REVIEW) },
                    modifier = Modifier.padding(bottom = Spacing.m),
                    onPassedRealExam = { onPassedRealExam(id) }.takeIf { state.canMarkPassed(summary) },
                    passed = id in state.passed,
                )
            }
            item { SectionTitle(stringResource(R.string.add_more)) }
            item { OtherProvincesRow(state.otherProvinces, onOpenProvinces) }
        }
    }
}

/** "Other provinces": the ones not added yet, opening My provinces. */
@Composable
private fun OtherProvincesRow(others: List<Province>, onClick: () -> Unit) {
    BhCard(Modifier.testTag("other-provinces"), onClick = onClick) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.other_provinces), fontWeight = FontWeight.SemiBold) },
            supportingContent = {
                Text(
                    if (others.isEmpty()) {
                        stringResource(R.string.all_provinces_added)
                    } else {
                        others.map { stringResource(it.nameRes) }.joinToString(", ")
                    },
                )
            },
            leadingContent = { IconBadge(Icons.Outlined.Place) },
            trailingContent = { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}
