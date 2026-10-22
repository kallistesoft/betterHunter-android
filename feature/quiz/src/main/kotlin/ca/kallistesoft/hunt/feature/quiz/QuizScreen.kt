package ca.kallistesoft.hunt.feature.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.model.Question
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.CONTACT_EMAIL
import ca.kallistesoft.hunt.core.ui.PrefetchPhoto
import ca.kallistesoft.hunt.core.ui.QuestionPhoto
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.SourceLinks
import ca.kallistesoft.hunt.core.ui.currentLang
import ca.kallistesoft.hunt.core.ui.rememberLinkOpener
import kotlinx.coroutines.launch

/** A quiz on [examId]: questions, then the results ("Try again" starts a new run in place). */
@Composable
fun QuizScreen(
    examId: String,
    mode: QuizMode,
    onClose: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel<QuizViewModel, QuizViewModel.Factory>(
        creationCallback = { factory -> factory.create(examId, mode) },
    ),
) {
    when (val state = viewModel.uiState.collectAsStateWithLifecycle().value) {
        QuizUiState.Loading -> Box(Modifier.fillMaxSize())
        is QuizUiState.Empty -> EmptyQuiz(state.mode, onClose)
        is QuizUiState.Running -> QuestionContent(state, viewModel::choose, viewModel::next, onClose)
        is QuizUiState.Finished -> ResultContent(state, onHome = onClose, onAgain = viewModel::restart)
    }
}

@Composable
private fun EmptyQuiz(mode: QuizMode, onClose: () -> Unit) {
    Scaffold(topBar = { BackTopAppBar("", onClose) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                stringResource(if (mode == QuizMode.REVIEW) R.string.no_weak else R.string.no_questions),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun QuestionContent(
    state: QuizUiState.Running,
    onChoose: (Int) -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
) {
    val s = state.session
    val q = s.question
    val text = q.text(currentLang())
    val isExam = s.mode == QuizMode.EXAM
    val chosen = s.currentAnswer
    val showFeedback = !isExam && chosen != null
    var confirmQuit by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val links = rememberLinkOpener()
    val listState = rememberLazyListState()

    BackHandler { confirmQuit = true }
    PrefetchPhoto(s.questions.getOrNull(s.current + 1)?.image)
    // Each question starts scrolled to the top.
    LaunchedEffect(s.current) { listState.scrollToItem(0) }

    val reportSubject = stringResource(R.string.report_subject, q.id)
    val reportBody = stringResource(R.string.report_body) + "---\n${q.id} (${q.sourceRef})\n${text.q}\n" +
        text.choices.mapIndexed { i, c -> "${"ABCD"[i]}. $c" }.joinToString("\n")
    val reportFallback = stringResource(R.string.report_fallback, CONTACT_EMAIL, q.id)

    Scaffold(
        topBar = {
            Column {
                BackTopAppBar(
                    title = stringResource(R.string.question_progress, s.current + 1, s.questions.size),
                    onBack = { confirmQuit = true },
                    actions = {
                        IconButton(
                            onClick = {
                                if (!links.email(CONTACT_EMAIL, reportSubject, reportBody)) {
                                    scope.launch { snackbar.showSnackbar(reportFallback) }
                                }
                            },
                            modifier = Modifier.testTag("report"),
                        ) {
                            Icon(Icons.Outlined.Flag, contentDescription = stringResource(R.string.report))
                        }
                        state.secondsLeft?.let { Countdown(it) }
                    },
                )
                LinearProgressIndicator(
                    progress = { (s.current + 1f) / s.questions.size },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        bottomBar = {
            Button(
                onClick = onNext,
                enabled = chosen != null,
                modifier = Modifier.navigationBarsPadding().padding(16.dp).fillMaxWidth().testTag("next"),
            ) {
                Text(stringResource(if (s.isLast) R.string.finish else R.string.next))
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(state = listState, contentPadding = PaddingValues(16.dp), modifier = Modifier.padding(padding)) {
            if (q.image != null) {
                item { QuestionPhoto(q) }
                // Photo questions: the animal's card goes right under the photo, where the eye already is.
                if (showFeedback) item { Feedback(q, chosen, Modifier.padding(bottom = 16.dp)) }
            }
            item {
                Text(
                    text.q,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 20.dp),
                )
            }
            itemsIndexed(text.choices) { i, choice ->
                val choiceState = when {
                    showFeedback && i == q.answerIndex -> ChoiceState.CORRECT
                    showFeedback && i == chosen -> ChoiceState.WRONG
                    isExam && i == chosen -> ChoiceState.SELECTED
                    else -> ChoiceState.IDLE
                }
                ChoiceTile("ABCD"[i].toString(), choice, choiceState, onClick = { onChoose(i) }, Modifier.testTag("choice-$i"))
            }
            if (showFeedback && q.image == null) item { Feedback(q, chosen, Modifier.padding(top = 8.dp)) }
        }
    }

    if (confirmQuit) {
        AlertDialog(
            onDismissRequest = { confirmQuit = false },
            title = { Text(stringResource(R.string.quit_title)) },
            text = { Text(stringResource(R.string.quit_body)) },
            confirmButton = {
                Button(onClick = { confirmQuit = false; onClose() }) { Text(stringResource(R.string.quit)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmQuit = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun Countdown(secondsLeft: Int) {
    val minutes = secondsLeft / 60
    val label = "%d:%02d".format(minutes, secondsLeft % 60)
    val description = stringResource(R.string.time_left, label)
    Text(
        label,
        style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
        color = if (minutes < 5) MaterialTheme.colorScheme.error else Color.Unspecified,
        modifier = Modifier.padding(end = 16.dp).semantics { contentDescription = description },
    )
}

private enum class ChoiceState { IDLE, SELECTED, CORRECT, WRONG }

@Composable
private fun ChoiceTile(letter: String, label: String, state: ChoiceState, onClick: () -> Unit, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    val success = BetterHunterTheme.extendedColors.success
    val (border, fill) = when (state) {
        ChoiceState.IDLE -> scheme.outlineVariant to Color.Transparent
        ChoiceState.SELECTED -> scheme.primary to scheme.primaryContainer
        ChoiceState.CORRECT -> success to success.copy(alpha = 0.12f)
        ChoiceState.WRONG -> scheme.error to scheme.error.copy(alpha = 0.12f)
    }
    Surface(
        onClick = onClick,
        modifier = modifier.padding(bottom = 10.dp).fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = fill,
        border = BorderStroke(if (state == ChoiceState.IDLE) 1.dp else 2.dp, border),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(28.dp), shape = CircleShape, color = scheme.secondaryContainer) {
                Box(contentAlignment = Alignment.Center) { Text(letter, style = MaterialTheme.typography.labelLarge) }
            }
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            when (state) {
                ChoiceState.CORRECT -> Icon(Icons.Rounded.CheckCircle, contentDescription = stringResource(R.string.correct), tint = border)
                ChoiceState.WRONG -> Icon(Icons.Rounded.Cancel, contentDescription = stringResource(R.string.wrong), tint = border)
                else -> Unit
            }
        }
    }
}

/** Right or wrong, with the explanation and, for animals, where they live and what not to confuse them with. */
@Composable
private fun Feedback(q: Question, chosen: Int?, modifier: Modifier = Modifier) {
    val text = q.text(currentLang())
    val right = chosen == q.answerIndex
    val color = if (right) BetterHunterTheme.extendedColors.success else MaterialTheme.colorScheme.error
    BhCard(modifier.testTag("feedback"), containerColor = color.copy(alpha = 0.08f), border = null) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (right) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel, contentDescription = null, tint = color)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(if (right) R.string.correct else R.string.wrong),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
            }
            Spacer(Modifier.size(8.dp))
            Text(
                stringResource(if (q.image != null) R.string.how_recognise else R.string.why),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(text.explanation)
            text.habitat?.let {
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.where_lives), style = MaterialTheme.typography.labelLarge)
                Text(it)
            }
            text.confuse?.let { confuse ->
                Spacer(Modifier.size(8.dp))
                Row {
                    Icon(
                        Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    val label = stringResource(R.string.dont_confuse)
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(label) }
                            append(confuse)
                        },
                    )
                }
            }
            Spacer(Modifier.size(4.dp))
            SourceLinks(q)
        }
    }
}
