package ca.kallistesoft.hunt.feature.exams

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.SectionTitle
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.ExamCard
import ca.kallistesoft.hunt.core.ui.ExamListItem
import ca.kallistesoft.hunt.core.ui.ExamSummary
import ca.kallistesoft.hunt.core.ui.R

/** "Exam training": the user's exams as full cards, the other exams as compact rows. */
@Composable
fun ExamTrainingScreen(
    onBack: () -> Unit,
    onChangeExams: () -> Unit,
    onStartQuiz: (examId: String, mode: QuizMode) -> Unit,
    viewModel: ExamsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ExamTrainingContent(state, onBack, onChangeExams, onStartQuiz)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExamTrainingContent(
    state: ExamsUiState,
    onBack: () -> Unit,
    onChangeExams: () -> Unit,
    onStartQuiz: (examId: String, mode: QuizMode) -> Unit,
) {
    var sheetExamId by rememberSaveable { mutableStateOf<String?>(null) }
    Scaffold(topBar = { BackTopAppBar(stringResource(R.string.hub_exams), onBack) }) { padding ->
        LazyColumn(contentPadding = padding.plus()) {
            item {
                SectionTitle(stringResource(R.string.my_exams)) {
                    TextButton(onClick = onChangeExams, modifier = Modifier.testTag("change-exams")) {
                        Icon(Icons.Rounded.Tune, contentDescription = null, Modifier.size(Sizes.iconM))
                        Spacer(Modifier.width(Spacing.s))
                        Text(stringResource(R.string.action_change))
                    }
                }
            }
            items(state.mine, key = { it.exam.id }) { summary ->
                ExamCard(
                    summary,
                    onPractice = { onStartQuiz(summary.exam.id, QuizMode.PRACTICE) },
                    onExam = { onStartQuiz(summary.exam.id, QuizMode.EXAM) },
                    onReview = { onStartQuiz(summary.exam.id, QuizMode.REVIEW) },
                    modifier = Modifier.padding(bottom = Spacing.m),
                )
            }
            if (state.others.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.other_tests)) }
                item {
                    BhCard {
                        state.others.forEachIndexed { i, summary ->
                            if (i > 0) HorizontalDivider(Modifier.padding(start = Sizes.listDividerInset))
                            OtherExamRow(summary, onClick = { sheetExamId = summary.exam.id })
                        }
                    }
                }
            }
        }
    }
    // An exam the user isn't preparing opens its full card in a sheet.
    state.others.firstOrNull { it.exam.id == sheetExamId }?.let { summary ->
        ModalBottomSheet(onDismissRequest = { sheetExamId = null }) {
            ExamCard(
                summary,
                onPractice = { sheetExamId = null; onStartQuiz(summary.exam.id, QuizMode.PRACTICE) },
                onExam = { sheetExamId = null; onStartQuiz(summary.exam.id, QuizMode.EXAM) },
                onReview = { sheetExamId = null; onStartQuiz(summary.exam.id, QuizMode.REVIEW) },
                modifier = Modifier.padding(start = Spacing.l, end = Spacing.l, bottom = Spacing.xxl),
            )
        }
    }
}

@Composable
private fun OtherExamRow(summary: ExamSummary, onClick: () -> Unit) {
    ExamListItem(
        summary.exam,
        summary.questionCount,
        modifier = Modifier.testTag("other-${summary.exam.id}").clickable(onClick = onClick),
        trailing = { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null) },
    )
}
