package ca.kallistesoft.hunt.feature.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.designsystem.component.AnswerChoice
import ca.kallistesoft.hunt.core.designsystem.component.AnswerResult
import ca.kallistesoft.hunt.core.designsystem.component.AnswerState
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.BottomActionBar
import ca.kallistesoft.hunt.core.designsystem.component.LinkButton
import ca.kallistesoft.hunt.core.designsystem.component.Overline
import ca.kallistesoft.hunt.core.designsystem.component.PrimaryButton
import ca.kallistesoft.hunt.core.designsystem.component.QuizProgress
import ca.kallistesoft.hunt.core.designsystem.component.ResultChip
import ca.kallistesoft.hunt.core.designsystem.component.SecondaryButton
import ca.kallistesoft.hunt.core.designsystem.component.SegmentState
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.Question
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.CONTACT_EMAIL
import ca.kallistesoft.hunt.core.ui.PrefetchPhoto
import ca.kallistesoft.hunt.core.ui.QuestionPhoto
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.SourceLinks
import ca.kallistesoft.hunt.core.ui.currentLang
import ca.kallistesoft.hunt.core.ui.otherLanguageChoice
import ca.kallistesoft.hunt.core.ui.rememberLinkOpener
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
        QuizUiState.Loading -> LoadingQuiz(onClose)
        is QuizUiState.Empty -> EmptyQuiz(state.mode, onClose)
        is QuizUiState.Running -> QuestionContent(
            state,
            canReport = viewModel.canReport.collectAsStateWithLifecycle().value,
            onChoose = viewModel::choose,
            onNext = viewModel::next,
            onClose = onClose,
        )
        is QuizUiState.Finished -> ResultContent(state, onHome = onClose, onAgain = viewModel::restart)
    }
}

@Composable
private fun LoadingQuiz(onClose: () -> Unit) {
    Scaffold(topBar = { BackTopAppBar("", onClose) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun EmptyQuiz(mode: QuizMode, onClose: () -> Unit) {
    Scaffold(topBar = { BackTopAppBar("", onClose) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).padding(Spacing.xxxl), contentAlignment = Alignment.Center) {
            Text(
                stringResource(if (mode == QuizMode.REVIEW) R.string.no_weak else R.string.no_questions),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
internal fun QuestionContent(
    state: QuizUiState.Running,
    canReport: Boolean,
    onChoose: (Int) -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
) {
    val s = state.session
    val q = s.question
    val lang = currentLang()
    val text = q.text(lang)
    val isExam = s.mode == QuizMode.EXAM
    val isPhoto = q.image != null
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

    // Mock exams hide right and wrong until the end: one plain bar. Otherwise one segment per question.
    val segments = if (isExam) {
        null
    } else {
        s.questions.indices.map { i ->
            val answer = s.answers[i]
            when {
                answer != null -> if (answer == s.questions[i].answerIndex) SegmentState.Right else SegmentState.Wrong
                i == s.current -> SegmentState.Current
                else -> SegmentState.Upcoming
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                BackTopAppBar(
                    title = stringResource(R.string.question_progress, s.current + 1, s.questions.size),
                    onBack = { confirmQuit = true },
                    overline = if (isExam) "${stringResource(R.string.exam_sim)} · ${s.exam.id}" else null,
                    closeIcon = isExam,
                    actions = {
                        if (canReport) IconButton(
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
                QuizProgress(
                    segments,
                    progress = (s.current + 1f) / s.questions.size,
                    modifier = Modifier.padding(horizontal = Spacing.screen, vertical = Spacing.xs),
                )
            }
        },
        bottomBar = {
            BottomActionBar {
                val factSheet = q.sourceUrl
                if (showFeedback && isPhoto && factSheet != null) {
                    SecondaryButton(
                        stringResource(R.string.fact_sheet),
                        onClick = { links.open(factSheet) },
                        modifier = Modifier.weight(1f).testTag("fact-sheet"),
                    )
                }
                PrimaryButton(
                    stringResource(if (s.isLast) R.string.finish else R.string.next),
                    onClick = onNext,
                    enabled = chosen != null,
                    modifier = Modifier.weight(2f).testTag("next"),
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            state = listState,
            contentPadding = padding.plus(top = Spacing.l, bottom = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            if (isPhoto) {
                item {
                    QuestionPhoto(q) {
                        if (showFeedback) {
                            val right = chosen == q.answerIndex
                            ResultChip(
                                correct = right,
                                text = stringResource(if (right) R.string.correct else R.string.wrong),
                                modifier = Modifier.align(Alignment.TopStart).padding(Spacing.m),
                            )
                        }
                    }
                }
            }
            if (isPhoto && showFeedback) {
                // Answered "What is it?": the animal's card replaces the choices, like a field guide page.
                item { AnimalName(q, chosen) }
                item { Feedback(q, chosen) }
                text.confuse?.let { item { ConfuseCard(it) } }
            } else {
                item {
                    Text(
                        text.q,
                        style = if (isPhoto) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                    )
                }
                item {
                    Choices(
                        q,
                        isPhoto = isPhoto,
                        stateOf = { i ->
                            when {
                                showFeedback && i == q.answerIndex -> AnswerState.Correct
                                showFeedback && i == chosen -> AnswerState.Wrong
                                isExam && i == chosen -> AnswerState.Selected
                                else -> AnswerState.Idle
                            }
                        },
                        onChoose = onChoose,
                    )
                }
                if (showFeedback) {
                    item { Feedback(q, chosen) }
                    text.confuse?.let { item { ConfuseCard(it) } }
                }
                if (isExam) item { ExamInfo((s.exam.passMark * 100).roundToInt()) }
            }
        }
    }

    if (confirmQuit) {
        AlertDialog(
            onDismissRequest = { confirmQuit = false },
            title = { Text(stringResource(R.string.quit_title)) },
            text = { Text(stringResource(R.string.quit_body)) },
            confirmButton = {
                PrimaryButton(stringResource(R.string.quit), onClick = { confirmQuit = false; onClose() })
            },
            dismissButton = {
                LinkButton(onClick = { confirmQuit = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

/** The four answers: lettered rows, or names two by two for "What is it?" photos (with the other language's name). */
@Composable
private fun Choices(q: Question, isPhoto: Boolean, stateOf: (Int) -> AnswerState, onChoose: (Int) -> Unit) {
    val gap = Spacing.s + Spacing.xxs
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        if (isPhoto) {
            q.text(currentLang()).choices.indices.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    row.forEach { i -> Choice(q, i, isPhoto = true, stateOf(i), onChoose, modifier = Modifier.weight(1f)) }
                }
            }
        } else {
            q.text(currentLang()).choices.indices.forEach { i ->
                Choice(q, i, isPhoto = false, stateOf(i), onChoose, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun Choice(q: Question, i: Int, isPhoto: Boolean, state: AnswerState, onChoose: (Int) -> Unit, modifier: Modifier = Modifier) {
    val lang = currentLang()
    AnswerChoice(
        text = q.text(lang).choices[i],
        state = state,
        onClick = { onChoose(i) },
        modifier = modifier.testTag("choice-$i"),
        letter = if (isPhoto) null else "ABCD"[i].toString(),
        subtitle = if (isPhoto) q.otherLanguageChoice(lang, i) else null,
        stateDescription = when (state) {
            AnswerState.Correct -> stringResource(R.string.correct)
            AnswerState.Wrong -> stringResource(R.string.wrong)
            else -> null
        },
    )
}

/** Timer of a mock exam, in red under five minutes. */
@Composable
private fun Countdown(secondsLeft: Int) {
    val minutes = secondsLeft / 60
    val label = "%d:%02d".format(minutes, secondsLeft % 60)
    val description = stringResource(R.string.time_left, label)
    val low = minutes < 5
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.padding(end = Spacing.s).clearAndSetSemantics { contentDescription = description },
        shape = CircleShape,
        color = scheme.surfaceContainerLow,
        border = BorderStroke(Sizes.border, scheme.outlineVariant),
    ) {
        Row(
            Modifier.heightIn(min = Sizes.chipHeight).padding(horizontal = Spacing.s + Spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.Timer,
                contentDescription = null,
                tint = if (low) scheme.error else scheme.primary,
                modifier = Modifier.size(Sizes.iconXs),
            )
            Spacer(Modifier.width(Spacing.xs + Spacing.xxs))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                color = if (low) scheme.error else scheme.onSurface,
            )
        }
    }
}

/** The answer of a "What is it?" photo: its name in both languages, and the user's pick when it was wrong. */
@Composable
private fun AnimalName(q: Question, chosen: Int?) {
    val lang = currentLang()
    val text = q.text(lang)
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        Text(text.choices[q.answerIndex], style = MaterialTheme.typography.headlineMedium)
        q.otherLanguageChoice(lang, q.answerIndex)?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = muted)
        }
        if (chosen != null && chosen != q.answerIndex) {
            Text(
                stringResource(R.string.your_answer, text.choices[chosen]),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

/** Right or wrong (photos show it on the photo), the explanation and, for animals, where they live. */
@Composable
private fun Feedback(q: Question, chosen: Int?) {
    val text = q.text(currentLang())
    val right = chosen == q.answerIndex
    val isPhoto = q.image != null
    BhCard(Modifier.testTag("feedback"), shape = MaterialTheme.shapes.medium) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            if (!isPhoto) AnswerResult(correct = right, text = stringResource(if (right) R.string.correct else R.string.wrong))
            FeedbackSection(stringResource(if (isPhoto) R.string.how_recognise else R.string.why), text.explanation)
            text.habitat?.let {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                FeedbackSection(stringResource(R.string.where_lives), it)
            }
            if (!isPhoto) SourceLinks(q)
        }
    }
}

@Composable
private fun FeedbackSection(label: String, text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Overline(label, color = MaterialTheme.colorScheme.secondary)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

/** "Don't confuse it": the look-alike to watch for, in an orange-tinted card. */
@Composable
private fun ConfuseCard(confuse: String) {
    BhCard(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(Sizes.border, BetterHunterTheme.extendedColors.warmBorder),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            Overline(stringResource(R.string.dont_confuse).trimEnd(':', ' ', '\u00A0'))
            Text(confuse.replaceFirstChar { it.titlecase() }, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Mock exams: answers stay hidden until the end, and the pass mark. */
@Composable
private fun ExamInfo(passMark: Int) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = muted, modifier = Modifier.size(Sizes.iconS))
        Spacer(Modifier.width(Spacing.s))
        Text(stringResource(R.string.exam_info, passMark), style = MaterialTheme.typography.bodySmall, color = muted)
    }
}
