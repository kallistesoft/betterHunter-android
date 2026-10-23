package ca.kallistesoft.hunt.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.HubCard
import ca.kallistesoft.hunt.core.designsystem.component.IconBadge
import ca.kallistesoft.hunt.core.designsystem.component.HubTone
import ca.kallistesoft.hunt.core.designsystem.component.LabelledMeter
import ca.kallistesoft.hunt.core.designsystem.component.LinkButton
import ca.kallistesoft.hunt.core.designsystem.component.NoticeCard
import ca.kallistesoft.hunt.core.designsystem.component.NoticeTone
import ca.kallistesoft.hunt.core.designsystem.component.Overline
import ca.kallistesoft.hunt.core.designsystem.component.PrimaryButton
import ca.kallistesoft.hunt.core.designsystem.component.ProgressRing
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.designsystem.theme.BhTextStyles
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.Lang
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.core.ui.BrandTopAppBar
import ca.kallistesoft.hunt.core.ui.ExamPassed
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.currentLang
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    onOpenExams: () -> Unit,
    onOpenRecognition: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProvinces: () -> Unit,
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
        onOpenSettings = onOpenSettings,
        onOpenProvinces = onOpenProvinces,
        onStartQuiz = onStartQuiz,
        onSetLanguage = onSetLanguage,
        onAnswerPhoto = viewModel::answerPhotoOfDay,
        onDismissBanner = viewModel::dismissNewHunterBanner,
    )
}

/**
 * The dashboard. Working on it: the daily goal with "Practise now" on the main exam, then Exam training and
 * "What is it?". Hunters (new or experienced): the goal played on "What is it?", which comes first, and Exam training
 * kept for refreshers.
 */
@Composable
internal fun HomeContent(
    state: HomeUiState,
    onOpenExams: () -> Unit,
    onOpenRecognition: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProvinces: () -> Unit,
    onStartQuiz: (examId: String, mode: QuizMode) -> Unit,
    onSetLanguage: (Lang) -> Unit,
    onAnswerPhoto: (Int) -> Unit,
    onDismissBanner: () -> Unit,
) {
    Scaffold(
        topBar = {
            BrandTopAppBar(onSetLanguage) {
                IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("settings")) {
                    Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings))
                }
            }
        },
    ) { padding ->
        if (state.loading) return@Scaffold
        LazyColumn(
            contentPadding = padding.plus(top = Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.m + Spacing.xxs),
        ) {
            if (state.hasSampleContent) {
                item {
                    NoticeCard(stringResource(R.string.sample_banner), NoticeTone.Warning, icon = Icons.Rounded.WarningAmber)
                }
            }
            if (state.hunter && state.newHunterBanner) {
                item { NewHunterBanner(onDismissBanner) }
            }
            item {
                TodayCard(
                    state,
                    onPlay = { exam -> onStartQuiz(exam, QuizMode.PRACTICE) },
                    onChangeExams = onOpenProvinces,
                )
            }
            item { Hubs(state, onOpenExams, onOpenRecognition) }
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

/** "Welcome, new hunter!" after "Yes, I passed", until closed. */
@Composable
private fun NewHunterBanner(onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    BhCard(
        Modifier.testTag("new-hunter-banner"),
        containerColor = scheme.primaryContainer,
        border = BorderStroke(Sizes.border, BetterHunterTheme.extendedColors.warmBorder),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(Modifier.padding(start = Spacing.l, top = Spacing.m, bottom = Spacing.m), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Outlined.EmojiEvents, containerColor = scheme.tertiaryContainer, contentColor = scheme.tertiary)
            Spacer(Modifier.width(Spacing.m))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                Text(stringResource(R.string.new_hunter_welcome), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.new_hunter_welcome_sub), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            }
            IconButton(onClick = onDismiss, modifier = Modifier.testTag("banner-close")) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close), tint = scheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Daily goal ring and streak, then one tap to reach the goal: "Practise now" on the main exam (with a link to change
 * the exams), or for hunters, "Play What is it?".
 */
@Composable
private fun TodayCard(state: HomeUiState, onPlay: (examId: String) -> Unit, onChangeExams: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val done = state.answeredToday
    val goal = state.dailyGoal
    val reached = done >= goal
    val playsPhotos = state.hunter && state.photoGameId != null
    BhCard {
        Column(Modifier.padding(Sizes.cardPadding), verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(
                    done.toFloat() / goal,
                    size = Sizes.ringXl,
                    strokeWidth = Sizes.ringStrokeXl,
                    color = scheme.primary,
                ) {
                    if (reached) {
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(Sizes.iconXl))
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$done", style = BhTextStyles.ringNumber)
                            Text(
                                stringResource(R.string.goal_of, goal),
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Spacer(Modifier.width(Spacing.l))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Overline(stringResource(R.string.today))
                    Text(
                        when {
                            reached -> stringResource(R.string.goal_done)
                            playsPhotos -> pluralStringResource(R.plurals.animals_remaining, goal - done, goal - done)
                            else -> pluralStringResource(R.plurals.goal_remaining, goal - done, goal - done)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.testTag("goal-text"),
                    )
                    if (state.streak > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                tint = BetterHunterTheme.extendedColors.streak,
                                modifier = Modifier.size(Sizes.iconS),
                            )
                            Spacer(Modifier.width(Spacing.xs + Spacing.xxs))
                            Text(
                                pluralStringResource(R.plurals.streak, state.streak, state.streak),
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            if (playsPhotos) {
                PrimaryButton(
                    stringResource(R.string.play_id),
                    onClick = { onPlay(state.photoGameId) },
                    modifier = Modifier.fillMaxWidth().testTag("today-play-id"),
                    icon = Icons.Rounded.PlayArrow,
                )
            } else state.mainExam?.let { exam ->
                Column {
                    HorizontalDivider(color = scheme.outlineVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            exam.name(currentLang()),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        LinkButton(onClick = onChangeExams, modifier = Modifier.testTag("today-change")) {
                            Text(stringResource(R.string.action_change))
                        }
                    }
                }
                PrimaryButton(
                    stringResource(R.string.practise_now),
                    onClick = { onPlay(exam.id) },
                    modifier = Modifier.fillMaxWidth().testTag("today-practice"),
                    icon = Icons.Rounded.PlayArrow,
                )
            }
        }
    }
}

/**
 * The two sections side by side: Exam training (with readiness) then "What is it?"; for hunters, "What is it?" first
 * (with readiness) and Exam training for refreshers.
 */
@Composable
private fun Hubs(state: HomeUiState, onOpenExams: () -> Unit, onOpenRecognition: () -> Unit) {
    val showGames = state.gameCount > 0
    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
        val tile = Modifier.weight(1f).fillMaxHeight()
        if (state.hunter) {
            if (showGames) RecognitionHub(state, onOpenRecognition, tile)
            ExamsHub(state, onOpenExams, tile)
        } else {
            ExamsHub(state, onOpenExams, tile)
            if (showGames) RecognitionHub(state, onOpenRecognition, tile)
        }
    }
}

@Composable
private fun ExamsHub(state: HomeUiState, onClick: () -> Unit, modifier: Modifier) {
    HubCard(
        icon = Icons.Outlined.School,
        title = stringResource(R.string.hub_exams),
        subtitle = stringResource(if (state.hunter) R.string.hub_exams_refresh else R.string.hub_exams_sub),
        tone = HubTone.Green,
        onClick = onClick,
        modifier = modifier.testTag("hub-exams"),
    ) {
        val readiness = state.readiness
        val passed = state.passedExamId
        when {
            state.hunter && passed != null -> ExamPassed(passed)
            readiness != null -> Readiness(readiness)
            else -> Facts(
                pluralStringResource(R.plurals.exams_count, state.trainingExamCount, state.trainingExamCount),
                MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun RecognitionHub(state: HomeUiState, onClick: () -> Unit, modifier: Modifier) {
    HubCard(
        icon = Icons.Outlined.Visibility,
        title = stringResource(R.string.what_is_it),
        subtitle = stringResource(R.string.hub_id_sub),
        tone = HubTone.Orange,
        onClick = onClick,
        modifier = modifier.testTag("hub-recognise"),
    ) {
        if (state.hunter) {
            Readiness(state.photoReadiness)
        } else {
            Facts(
                listOfNotNull(
                    state.animalCount.takeIf { it > 0 }?.let { pluralStringResource(R.plurals.animals_count, it, it) },
                    pluralStringResource(R.plurals.games_count, state.gameCount, state.gameCount),
                ).joinToString(" · "),
                MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun Readiness(value: Float) {
    LabelledMeter(
        label = stringResource(R.string.readiness),
        value = stringResource(R.string.percent, (value * 100).roundToInt()),
        progress = value,
    )
}

@Composable
private fun Facts(text: String, color: Color) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = color)
}

/** The three legal points, always visible, with a link to the full text in About. */
@Composable
private fun Footer(onOpenAbout: () -> Unit) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = muted, modifier = Modifier.size(Sizes.iconS))
        Spacer(Modifier.width(Spacing.s))
        Text(
            stringResource(R.string.footer),
            style = MaterialTheme.typography.bodySmall,
            color = muted,
            modifier = Modifier.weight(1f).testTag("footer"),
        )
        LinkButton(onClick = onOpenAbout) { Text(stringResource(R.string.learn_more)) }
    }
}
