package ca.kallistesoft.hunt.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.HubCard
import ca.kallistesoft.hunt.core.designsystem.component.NoticeCard
import ca.kallistesoft.hunt.core.designsystem.component.NoticeTone
import ca.kallistesoft.hunt.core.designsystem.component.ProgressRing
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.Lang
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.currentLang
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    onOpenExams: () -> Unit,
    onOpenRecognition: () -> Unit,
    onOpenAbout: () -> Unit,
    onStartQuiz: (examId: String, mode: QuizMode) -> Unit,
    onSetLanguage: (Lang) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        state = state,
        onOpenExams = onOpenExams,
        onOpenRecognition = onOpenRecognition,
        onOpenAbout = onOpenAbout,
        onStartQuiz = onStartQuiz,
        onSetLanguage = onSetLanguage,
        onAnswerPhoto = viewModel::answerPhotoOfDay,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeContent(
    state: HomeUiState,
    onOpenExams: () -> Unit,
    onOpenRecognition: () -> Unit,
    onOpenAbout: () -> Unit,
    onStartQuiz: (examId: String, mode: QuizMode) -> Unit,
    onSetLanguage: (Lang) -> Unit,
    onAnswerPhoto: (Int) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_title), fontWeight = FontWeight.Bold) },
                actions = {
                    LanguageToggle(currentLang(), onSetLanguage)
                    IconButton(onClick = onOpenAbout, modifier = Modifier.testTag("about")) {
                        Icon(Icons.Outlined.Info, contentDescription = stringResource(R.string.about))
                    }
                },
            )
        },
    ) { padding ->
        if (state.loading) return@Scaffold
        LazyColumn(
            contentPadding = padding.plus(top = Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            if (state.hasSampleContent) {
                item {
                    NoticeCard(stringResource(R.string.sample_banner), NoticeTone.Warning, icon = Icons.Rounded.WarningAmber)
                }
            }
            item { TodayCard(state, onPractise = { exam -> onStartQuiz(exam, QuizMode.PRACTICE) }) }
            item {
                HubCard(
                    icon = Icons.Outlined.School,
                    title = stringResource(R.string.hub_exams),
                    subtitle = stringResource(R.string.hub_exams_sub),
                    facts = listOfNotNull(
                        pluralStringResource(R.plurals.exams_count, state.trainingExamCount, state.trainingExamCount),
                        state.readiness?.let { stringResource(R.string.readiness_value, (it * 100).roundToInt()) },
                    ),
                    containerColor = scheme.primaryContainer,
                    contentColor = scheme.onPrimaryContainer,
                    onClick = onOpenExams,
                    modifier = Modifier.testTag("hub-exams"),
                )
            }
            if (state.gameCount > 0) {
                item {
                    HubCard(
                        icon = Icons.Outlined.Visibility,
                        title = stringResource(R.string.hub_id),
                        subtitle = stringResource(R.string.hub_id_sub),
                        facts = listOfNotNull(
                            pluralStringResource(R.plurals.games_count, state.gameCount, state.gameCount),
                            state.animalCount.takeIf { it > 0 }
                                ?.let { pluralStringResource(R.plurals.animals_count, it, it) },
                        ),
                        containerColor = scheme.tertiaryContainer,
                        contentColor = scheme.onTertiaryContainer,
                        onClick = onOpenRecognition,
                        modifier = Modifier.testTag("hub-recognise"),
                    )
                }
            }
            state.photoOfDay?.let { photo ->
                item {
                    PhotoOfDayCard(
                        photo,
                        onAnswer = onAnswerPhoto,
                        onPlay = { onStartQuiz(photo.examId, QuizMode.PRACTICE) },
                    )
                }
            }
            item { Footer(onOpenAbout) }
        }
    }
}

@Composable
private fun LanguageToggle(lang: Lang, onSetLanguage: (Lang) -> Unit) {
    val description = stringResource(R.string.language)
    SingleChoiceSegmentedButtonRow(
        Modifier.testTag("lang-toggle").semantics { contentDescription = description },
    ) {
        Lang.entries.forEachIndexed { i, option ->
            SegmentedButton(
                selected = option == lang,
                onClick = { onSetLanguage(option) },
                shape = SegmentedButtonDefaults.itemShape(i, Lang.entries.size),
                icon = {},
            ) {
                Text(option.code.uppercase())
            }
        }
    }
}

/** Daily goal ring, streak, and a one-tap "Practise now" on the user's main exam. */
@Composable
private fun TodayCard(state: HomeUiState, onPractise: (examId: String) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val done = state.answeredToday
    val goal = state.dailyGoal
    BhCard(containerColor = scheme.primaryContainer.copy(alpha = 0.6f), border = null) {
        Column(Modifier.padding(Spacing.l)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(
                    done.toFloat() / goal,
                    size = 64.dp,
                    strokeWidth = 7.dp,
                    trackColor = scheme.surface,
                ) {
                    if (done >= goal) {
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = scheme.primary)
                    } else {
                        Text("$done", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(Spacing.l))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.today), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        if (done >= goal) stringResource(R.string.goal_done) else stringResource(R.string.goal_progress, done, goal),
                        modifier = Modifier.testTag("goal-text"),
                    )
                    if (state.streak > 0) {
                        Row(Modifier.padding(top = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                tint = BetterHunterTheme.extendedColors.streak,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(Spacing.xs))
                            Text(pluralStringResource(R.plurals.streak, state.streak, state.streak))
                        }
                    }
                }
            }
            state.mainExam?.let { exam ->
                Spacer(Modifier.size(14.dp))
                Text(
                    exam.name(currentLang()),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.size(6.dp))
                Button(onClick = { onPractise(exam.id) }, Modifier.fillMaxWidth().testTag("today-practice")) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(Spacing.s))
                    Text(stringResource(R.string.practise_now))
                }
            }
        }
    }
}

/** The three legal points, always visible, with a link to the full text in About. */
@Composable
private fun Footer(onOpenAbout: () -> Unit) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(Modifier.padding(top = Spacing.s), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = muted, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(Spacing.s))
        Text(
            stringResource(R.string.footer),
            style = MaterialTheme.typography.bodySmall,
            color = muted,
            modifier = Modifier.weight(1f).testTag("footer"),
        )
        TextButton(onClick = onOpenAbout) { Text(stringResource(R.string.learn_more)) }
    }
}
