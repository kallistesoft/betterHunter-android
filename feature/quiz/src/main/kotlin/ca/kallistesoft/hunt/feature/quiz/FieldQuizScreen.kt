package ca.kallistesoft.hunt.feature.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.designsystem.component.BadgeSize
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.BottomActionBar
import ca.kallistesoft.hunt.core.designsystem.component.IconBadge
import ca.kallistesoft.hunt.core.designsystem.component.LinkButton
import ca.kallistesoft.hunt.core.designsystem.component.MeterBar
import ca.kallistesoft.hunt.core.designsystem.component.Overline
import ca.kallistesoft.hunt.core.designsystem.component.PrimaryButton
import ca.kallistesoft.hunt.core.designsystem.component.ProgressRing
import ca.kallistesoft.hunt.core.designsystem.component.SecondaryButton
import ca.kallistesoft.hunt.core.designsystem.component.SectionTitle
import ca.kallistesoft.hunt.core.designsystem.component.StatusColumn
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.FieldRun
import ca.kallistesoft.hunt.core.model.FieldSkill
import ca.kallistesoft.hunt.core.model.FieldTraining
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.currentLang
import ca.kallistesoft.hunt.core.ui.icon
import ca.kallistesoft.hunt.core.ui.nameRes
import ca.kallistesoft.hunt.core.ui.rememberLinkOpener
import ca.kallistesoft.hunt.core.ui.rememberNotificationPermission
import kotlin.math.roundToInt

/**
 * A new hunter's run ([FieldRun]): questions, then the results. [onOpenSkill] starts the next skill after a skill
 * run; [onClose] goes back (to Field skills or Home).
 */
@Composable
fun FieldQuizScreen(
    run: FieldRun,
    skill: FieldSkill?,
    onClose: () -> Unit,
    onOpenSkill: (FieldSkill) -> Unit,
    viewModel: FieldQuizViewModel = hiltViewModel<FieldQuizViewModel, FieldQuizViewModel.Factory>(
        creationCallback = { factory -> factory.create(run, skill) },
    ),
) {
    val askPermission = rememberNotificationPermission(viewModel::turnOnDailyReminders)
    when (val state = viewModel.uiState.collectAsStateWithLifecycle().value) {
        FieldQuizUiState.Loading -> Scaffold(
            topBar = { BackTopAppBar("", onClose) },
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is FieldQuizUiState.Empty -> Scaffold(
            topBar = { BackTopAppBar(runTitle(run, skill), onClose) },
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { padding ->
            StatusColumn(padding) {
                Text(
                    stringResource(if (run == FieldRun.MISTAKES) R.string.nothing_to_review else R.string.no_questions),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        is FieldQuizUiState.Running -> QuestionContent(
            QuizUiState.Running(state.session, secondsLeft = null),
            canReport = viewModel.canReport.collectAsStateWithLifecycle().value,
            onChoose = viewModel::choose,
            onNext = viewModel::next,
            onClose = onClose,
            overline = runOverline(state),
            inTheField = true,
        )
        is FieldQuizUiState.Finished -> {
            val daily = state.daily
            if (daily != null) {
                DailyDoneContent(daily, onClose, onTurnOnReminders = askPermission, onNotNow = viewModel::declineDailyReminders)
            } else {
                FieldResultContent(state, onClose, onNextSkill = onOpenSkill, onAgain = viewModel::restart)
            }
        }
    }
}

@Composable
private fun runTitle(run: FieldRun, skill: FieldSkill?): String = when (run) {
    FieldRun.SKILL -> skill?.let { stringResource(it.nameRes) }.orEmpty()
    FieldRun.MIX -> stringResource(R.string.todays_mix)
    FieldRun.SEASON_CHECK -> stringResource(R.string.season_check)
    FieldRun.DAILY -> stringResource(R.string.today)
    FieldRun.MISTAKES -> stringResource(R.string.review_title)
}

/** "Field skills · Shot placement", "Season check · all skills", "Today · 3 species"… */
@Composable
private fun runOverline(state: FieldQuizUiState.Running): String = when (state.run) {
    FieldRun.SEASON_CHECK -> stringResource(R.string.season_check_overline)
    FieldRun.DAILY -> stringResource(
        R.string.daily_overline,
        if (state.session.question.image != null) {
            stringResource(R.string.daily_species, FieldTraining.DAILY_SPECIES)
        } else {
            stringResource(R.string.daily_general, FieldTraining.DAILY_GENERAL)
        },
    )
    else -> stringResource(R.string.field_overline, runTitle(state.run, state.skill))
}

/**
 * Results of a field run: the score, then how the skill moved ("Skill getting stronger 62% → 70%", or "Skill
 * mastered"), or every skill the mix touched, then the questions to review.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FieldResultContent(
    state: FieldQuizUiState.Finished,
    onSkills: () -> Unit,
    onNextSkill: (FieldSkill) -> Unit,
    onAgain: () -> Unit,
) {
    val session = state.session ?: return
    val lang = currentLang()
    val links = rememberLinkOpener()
    val inspection = LocalInspectionMode.current
    val success = BetterHunterTheme.extendedColors.success
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val missed = session.missed
    val single = state.changes.singleOrNull()?.takeIf { state.run == FieldRun.SKILL }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BackTopAppBar(
                runTitle(state.run, state.skill),
                onBack = null,
                overline = stringResource(R.string.field_results),
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            BottomActionBar {
                SecondaryButton(
                    stringResource(if (state.run == FieldRun.DAILY) R.string.home else R.string.skills),
                    onClick = onSkills,
                    modifier = Modifier.weight(1f).testTag("field-skills"),
                )
                val next = state.nextSkill
                if (next != null) {
                    PrimaryButton(
                        stringResource(R.string.next_skill, stringResource(next.nameRes)),
                        onClick = { onNextSkill(next) },
                        modifier = Modifier.weight(1f).testTag("next-skill"),
                    )
                } else {
                    PrimaryButton(stringResource(R.string.again), onClick = onAgain, modifier = Modifier.weight(1f).testTag("again"))
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        LazyColumn(
            Modifier.consumeWindowInsets(padding),
            contentPadding = padding.plus(top = Spacing.s, bottom = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                ProgressRing(
                    session.score.toFloat(),
                    Modifier.padding(vertical = Spacing.s),
                    size = Sizes.ringL,
                    strokeWidth = Sizes.ringStrokeL,
                    color = success,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.percent, (session.score * 100).roundToInt()),
                            style = MaterialTheme.typography.displayMedium,
                            color = success,
                            modifier = Modifier.testTag("score"),
                        )
                        Text("${session.correctCount} / ${session.questions.size}", style = MaterialTheme.typography.bodyLarge, color = muted)
                    }
                }
            }
            if (single != null) {
                item {
                    if (single.mastered) MasteredCard() else SkillChangeCard(single, stringResource(R.string.skill_stronger.takeIf { single.after > single.before } ?: R.string.skill_steady))
                }
            } else if (state.changes.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.your_skills), Modifier.fillMaxWidth()) }
                item {
                    BhCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
                            state.changes.forEach { SkillChangeRow(it) }
                        }
                    }
                }
            }
            item { SectionTitle(stringResource(R.string.missed_title), Modifier.fillMaxWidth()) }
            if (missed.isEmpty()) {
                item { Text(stringResource(R.string.all_correct), Modifier.fillMaxWidth()) }
            } else {
                item { Text(stringResource(R.string.to_my_mistakes), Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall, color = muted) }
            }
            items(missed, key = { it.first.id }, contentType = { "missed" }) { (q, picked) ->
                MissedCard(q, picked, lang, links, inspection)
            }
        }
    }
}

/** "Skill getting stronger": the skill's readiness before and after. */
@Composable
private fun SkillChangeCard(change: SkillChange, title: String) {
    BhCard(Modifier.fillMaxWidth().testTag("skill-change")) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.TrendingUp, contentDescription = null, tint = BetterHunterTheme.extendedColors.success, modifier = Modifier.size(Sizes.iconL))
                Spacer(Modifier.width(Spacing.s))
                Text(title, style = MaterialTheme.typography.titleSmall)
            }
            SkillChangeRow(change)
        }
    }
}

@Composable
private fun SkillChangeRow(change: SkillChange) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(change.skill.icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(Sizes.iconM))
            Spacer(Modifier.width(Spacing.s))
            Text(stringResource(change.skill.nameRes), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(
                if (change.mastered) {
                    stringResource(R.string.mastered)
                } else {
                    stringResource(R.string.percent_change, (change.before * 100).roundToInt(), (change.after * 100).roundToInt())
                },
                style = MaterialTheme.typography.labelLarge,
                color = BetterHunterTheme.extendedColors.success,
            )
        }
        MeterBar(change.after)
    }
}

/** "Skill mastered", and how it stays mastered. */
@Composable
private fun MasteredCard() {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.m), horizontalAlignment = Alignment.CenterHorizontally) {
        StatusPill(stringResource(R.string.skill_mastered), Icons.Rounded.EmojiEvents, BetterHunterTheme.extendedColors.success, outlined = true)
        Text(stringResource(R.string.skill_mastered_sub), style = MaterialTheme.typography.bodyMedium, color = muted, textAlign = TextAlign.Center)
        BhCard(Modifier.fillMaxWidth().testTag("staying-mastered")) {
            Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                Overline(stringResource(R.string.staying_mastered), color = MaterialTheme.colorScheme.secondary)
                Text(stringResource(R.string.staying_mastered_mix), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.staying_mastered_miss), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.fade_note), style = MaterialTheme.typography.bodySmall, color = muted)
            }
        }
    }
}

/**
 * Today's 6 questions done: the streak, right answers per part, and once, "Want a nudge tomorrow?" (P11: the
 * notification permission is only asked here and after saving a season date).
 */
@Composable
internal fun DailyDoneContent(
    daily: DailySummary,
    onHome: () -> Unit,
    onTurnOnReminders: () -> Unit,
    onNotNow: () -> Unit,
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Scaffold(
        topBar = { BackTopAppBar(stringResource(R.string.done_for_today), onHome, overline = stringResource(R.string.today), closeIcon = true) },
        bottomBar = {
            if (!daily.askReminders) {
                BottomActionBar {
                    PrimaryButton(stringResource(R.string.home), onClick = onHome, modifier = Modifier.weight(1f).testTag("daily-home"))
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        LazyColumn(
            Modifier.consumeWindowInsets(padding),
            contentPadding = padding.plus(top = Spacing.l, bottom = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item { IconBadge(Icons.Rounded.Check, size = BadgeSize.Hero) }
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Text(stringResource(R.string.done_for_today), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.testTag("daily-done"))
                    if (daily.streak > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = BetterHunterTheme.extendedColors.streak, modifier = Modifier.size(Sizes.iconS))
                            Spacer(Modifier.width(Spacing.xs))
                            Text(pluralStringResource(R.plurals.streak, daily.streak, daily.streak), style = MaterialTheme.typography.bodyMedium, color = muted)
                        }
                    }
                    Text(stringResource(R.string.daily_right, daily.right, daily.total), style = MaterialTheme.typography.bodyMedium, color = muted)
                }
            }
            item {
                BhCard(Modifier.fillMaxWidth()) {
                    Column {
                        PartRow(Icons.Outlined.Visibility, stringResource(R.string.daily_species, daily.speciesTotal), daily.speciesRight, daily.speciesTotal, orange = true)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        PartRow(Icons.Outlined.MenuBook, stringResource(R.string.daily_general, daily.generalTotal), daily.generalRight, daily.generalTotal)
                    }
                }
            }
            if (daily.askReminders) {
                item { NudgeCard(onTurnOnReminders, onNotNow) }
            }
        }
    }
}

@Composable
private fun PartRow(icon: ImageVector, title: String, right: Int, total: Int, orange: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    Row(Modifier.padding(Spacing.l), verticalAlignment = Alignment.CenterVertically) {
        IconBadge(
            icon,
            size = BadgeSize.Medium,
            containerColor = if (orange) scheme.tertiaryContainer else scheme.secondaryContainer,
            contentColor = if (orange) scheme.tertiary else scheme.secondary,
        )
        Spacer(Modifier.width(Spacing.m))
        Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        Text(
            stringResource(R.string.part_right, right, total),
            style = MaterialTheme.typography.labelLarge,
            color = if (right > 0) BetterHunterTheme.extendedColors.success else scheme.onSurfaceVariant,
        )
    }
}

/** "Want a nudge tomorrow?": why, when, and the two answers. */
@Composable
internal fun NudgeCard(onTurnOn: () -> Unit, onNotNow: () -> Unit) {
    BhCard(
        Modifier.fillMaxWidth().testTag("nudge"),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(Sizes.border, BetterHunterTheme.extendedColors.warmBorder),
    ) {
        Column(Modifier.padding(Sizes.cardPadding), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Outlined.NotificationsActive, containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.width(Spacing.m))
                Text(stringResource(R.string.nudge_title), style = MaterialTheme.typography.titleMedium)
            }
            Text(stringResource(R.string.nudge_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            PrimaryButton(stringResource(R.string.turn_on_reminders), onClick = onTurnOn, modifier = Modifier.fillMaxWidth().testTag("nudge-on"))
            LinkButton(onClick = onNotNow, modifier = Modifier.align(Alignment.CenterHorizontally).testTag("nudge-not-now")) {
                Text(stringResource(R.string.not_now))
            }
        }
    }
}
