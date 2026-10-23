package ca.kallistesoft.hunt.feature.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.BottomActionBar
import ca.kallistesoft.hunt.core.designsystem.component.PrimaryButton
import ca.kallistesoft.hunt.core.designsystem.component.ProgressRing
import ca.kallistesoft.hunt.core.designsystem.component.SecondaryButton
import ca.kallistesoft.hunt.core.designsystem.component.SectionTitle
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.Exam
import ca.kallistesoft.hunt.core.model.Lang
import ca.kallistesoft.hunt.core.model.Question
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.LinkOpener
import ca.kallistesoft.hunt.core.ui.PhotoThumb
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.SourceLinks
import ca.kallistesoft.hunt.core.ui.currentLang
import ca.kallistesoft.hunt.core.ui.rememberLinkOpener
import kotlin.math.roundToInt

/**
 * Results of a run: the score ring, then for practice the daily goal and how many questions go to "Review my
 * mistakes", for a mock exam pass or fail and the time used; then every missed question with the right answer, why,
 * and its source.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ResultContent(
    state: QuizUiState.Finished,
    dailyGoalReached: Boolean,
    onHome: () -> Unit,
    onAgain: () -> Unit,
) {
    val s = state.session
    val lang = currentLang()
    val links = rememberLinkOpener()
    val inspection = LocalInspectionMode.current
    val isExam = s.mode == QuizMode.EXAM
    val success = BetterHunterTheme.extendedColors.success
    val error = MaterialTheme.colorScheme.error
    val color = when {
        !isExam -> success
        s.passed -> success
        else -> error
    }
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val missed = s.missed
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BackTopAppBar(
                stringResource(if (isExam) R.string.exam_results else R.string.practice_results),
                onBack = null,
                overline = examLabel(s.exam),
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            BottomActionBar {
                SecondaryButton(stringResource(R.string.home), onClick = onHome, modifier = Modifier.weight(1f))
                PrimaryButton(stringResource(R.string.again), onClick = onAgain, modifier = Modifier.weight(1f).testTag("again"))
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
            if (state.timeUp) {
                item { Text(stringResource(R.string.time_up), style = MaterialTheme.typography.titleMedium) }
            }
            item {
                ProgressRing(
                    s.score.toFloat(),
                    Modifier.padding(vertical = Spacing.s),
                    size = Sizes.ringL,
                    strokeWidth = Sizes.ringStrokeL,
                    color = color,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.percent, (s.score * 100).roundToInt()),
                            style = MaterialTheme.typography.displayMedium,
                            color = color,
                            modifier = Modifier.testTag("score"),
                        )
                        Text("${s.correctCount} / ${s.questions.size}", style = MaterialTheme.typography.bodyLarge, color = muted)
                    }
                }
            }
            item {
                if (isExam) {
                    StatusPill(
                        stringResource(
                            R.string.pass_result,
                            stringResource(if (s.passed) R.string.passed else R.string.failed),
                            (s.exam.passMark * 100).roundToInt(),
                        ),
                        if (s.passed) Icons.Rounded.Check else Icons.Rounded.Close,
                        color,
                        outlined = true,
                    )
                } else if (dailyGoalReached) {
                    StatusPill(stringResource(R.string.daily_goal_reached), Icons.Rounded.Check, success, outlined = false)
                }
            }
            item {
                val count = missed.size
                val summary = if (isExam) {
                    listOfNotNull(
                        state.secondsLeft?.let { left ->
                            val total = s.exam.minutes * 60
                            stringResource(R.string.exam_time, clock(total - left), clock(total))
                        },
                        pluralStringResource(R.plurals.to_review, count, count).takeIf { count > 0 },
                    ).joinToString(" · ")
                } else {
                    if (count > 0) pluralStringResource(R.plurals.to_review_mistakes, count, count) else ""
                }
                if (summary.isNotEmpty()) {
                    Text(summary, style = MaterialTheme.typography.bodySmall, color = muted, textAlign = TextAlign.Center)
                }
            }
            item { SectionTitle(stringResource(R.string.missed_title), Modifier.fillMaxWidth()) }
            if (missed.isEmpty()) {
                item { Text(stringResource(R.string.all_correct), Modifier.fillMaxWidth()) }
            }
            items(missed, key = { it.first.id }, contentType = { "missed" }) { (q, picked) ->
                MissedCard(q, picked, lang, links, inspection)
            }
        }
    }
}

/**
 * A missed question: the user's answer, the right one, why, and its source. [lang], [links] and [inspection] are read
 * once by the list, not once per card.
 */
@Composable
internal fun MissedCard(q: Question, picked: Int?, lang: Lang, links: LinkOpener, inspection: Boolean) {
    val text = q.text(lang)
    val success = BetterHunterTheme.extendedColors.success
    BhCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            q.image?.let { PhotoThumb(it, Modifier.padding(bottom = Spacing.xs), inspection) }
            Text(text.q, style = MaterialTheme.typography.titleSmall)
            picked?.let {
                AnswerLine(Icons.Rounded.Close, stringResource(R.string.your_answer, text.choices[it]), MaterialTheme.colorScheme.error)
            }
            AnswerLine(Icons.Rounded.Check, stringResource(R.string.answer, text.choices[q.answerIndex]), success, bold = true)
            Text(text.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SourceLinks(q, lang = lang, links = links)
        }
    }
}

/** "ICAF · QUÉBEC" above the title: which exam this is about. */
internal fun examLabel(exam: Exam): String = listOf(exam.id, exam.region).filter { it.isNotEmpty() }.joinToString(" · ")

/** Minutes and seconds, "42:18". */
private fun clock(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)

/** A pill with an icon: pass or fail (outlined), or "Daily goal reached" (tinted). */
@Composable
internal fun StatusPill(text: String, icon: ImageVector, color: Color, outlined: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (outlined) Color.Transparent else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = color,
        border = if (outlined) BorderStroke(Sizes.border, color) else null,
    ) {
        Row(
            Modifier.heightIn(min = Sizes.chipHeight).padding(horizontal = Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, Modifier.size(Sizes.iconM))
            Spacer(Modifier.width(Spacing.s))
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** "✕ Your answer: …" or "✓ Correct answer: …". */
@Composable
private fun AnswerLine(icon: ImageVector, text: String, color: Color, bold: Boolean = false) {
    Row {
        Box(Modifier.heightIn(min = Sizes.iconL), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(Sizes.iconS))
        }
        Spacer(Modifier.width(Spacing.s))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
