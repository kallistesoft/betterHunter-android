package ca.kallistesoft.hunt.feature.exams

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.ExamCard
import ca.kallistesoft.hunt.core.ui.R

/** "Can you recognise them?": identification games (photo quiz today; tracks, calls… later). */
@Composable
fun RecognitionScreen(
    onBack: () -> Unit,
    onStartQuiz: (examId: String, mode: QuizMode) -> Unit,
    viewModel: ExamsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RecognitionContent(state, onBack, onStartQuiz)
}

@Composable
internal fun RecognitionContent(
    state: ExamsUiState,
    onBack: () -> Unit,
    onStartQuiz: (examId: String, mode: QuizMode) -> Unit,
) {
    Scaffold(
        topBar = { BackTopAppBar(stringResource(R.string.hub_id), onBack) },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        LazyColumn(Modifier.consumeWindowInsets(padding), contentPadding = padding.plus(top = Spacing.s)) {
            item {
                Text(
                    stringResource(R.string.hub_id_sub),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.l),
                )
            }
            items(state.games, key = { it.exam.id }) { summary ->
                ExamCard(
                    summary,
                    onPractice = { onStartQuiz(summary.exam.id, QuizMode.PRACTICE) },
                    onExam = { onStartQuiz(summary.exam.id, QuizMode.EXAM) },
                    onReview = { onStartQuiz(summary.exam.id, QuizMode.REVIEW) },
                    modifier = Modifier.padding(bottom = Spacing.m),
                )
            }
        }
    }
}
