package ca.kallistesoft.hunt.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import ca.kallistesoft.hunt.core.designsystem.component.BadgeSize
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.IconBadge
import ca.kallistesoft.hunt.core.designsystem.component.LinkButton
import ca.kallistesoft.hunt.core.designsystem.component.PrimaryButton
import ca.kallistesoft.hunt.core.designsystem.component.ProgressRing
import ca.kallistesoft.hunt.core.designsystem.component.SecondaryButton
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.Content
import ca.kallistesoft.hunt.core.model.Exam
import ca.kallistesoft.hunt.core.model.ExamGroup
import ca.kallistesoft.hunt.core.model.UserProgress
import kotlin.math.roundToInt

/** What a card needs to show one exam: its pool size, the user's readiness and weak spots. */
data class ExamSummary(val exam: Exam, val questionCount: Int, val readiness: Float, val weakCount: Int) {
    val available: Boolean get() = !exam.comingSoon && questionCount > 0
}

/**
 * Full card for one exam: icon, facts, readiness, Practice / Mock exam / review mistakes. A hunter course also gets
 * "I passed the real exam" ([onPassedRealExam]), or "Hunting exam passed" once the user said so ([passed]).
 */
@Composable
fun ExamCard(
    summary: ExamSummary,
    onPractice: () -> Unit,
    onExam: () -> Unit,
    onReview: () -> Unit,
    modifier: Modifier = Modifier,
    onPassedRealExam: (() -> Unit)? = null,
    passed: Boolean = false,
) {
    val exam = summary.exam
    val lang = currentLang()
    val scheme = MaterialTheme.colorScheme
    val meta = buildList {
        // A hunting exam's name already says its province ("Hunting exam · Québec").
        if (exam.region.isNotEmpty() && exam.group != ExamGroup.HUNTING) add(exam.region)
        if (summary.available) {
            add(stringResource(R.string.minutes, exam.minutes))
            add(stringResource(R.string.to_pass, (exam.passMark * 100).roundToInt()))
        }
    }.joinToString(" · ")

    BhCard(modifier.testTag("exam-card-${exam.id}")) {
        Column(Modifier.padding(Spacing.l)) {
            Row(verticalAlignment = Alignment.Top) {
                ExamAvatar(exam.id)
                Spacer(Modifier.width(Spacing.m))
                Column(Modifier.weight(1f)) {
                    Text(exam.name(lang), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(Spacing.xxs))
                    val muted = MaterialTheme.typography.bodySmall
                    Text(
                        if (summary.available) {
                            pluralStringResource(R.plurals.questions_count, summary.questionCount, summary.questionCount)
                        } else {
                            stringResource(R.string.coming_soon)
                        },
                        style = muted,
                        color = scheme.onSurfaceVariant,
                    )
                    if (meta.isNotEmpty()) Text(meta, style = muted, color = scheme.onSurfaceVariant)
                }
                if (summary.available) ReadinessRing(summary.readiness)
            }
            if (summary.available) {
                Spacer(Modifier.height(Spacing.l))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s + Spacing.xxs)) {
                    PrimaryButton(
                        stringResource(R.string.practice),
                        onClick = onPractice,
                        modifier = Modifier.weight(1f).testTag("practice-${exam.id}"),
                        icon = Icons.Rounded.PlayArrow,
                    )
                    SecondaryButton(
                        stringResource(R.string.exam_sim),
                        onClick = onExam,
                        modifier = Modifier.weight(1f).testTag("exam-${exam.id}"),
                        icon = Icons.Outlined.Timer,
                    )
                }
                if (summary.weakCount > 0) {
                    LinkButton(onClick = onReview, modifier = Modifier.padding(top = Spacing.xs).testTag("review-${exam.id}")) {
                        Icon(Icons.Rounded.Replay, contentDescription = null, Modifier.size(Sizes.iconM))
                        Spacer(Modifier.width(Spacing.s))
                        Text(stringResource(R.string.review_mistakes, summary.weakCount))
                    }
                }
                if (passed || onPassedRealExam != null) {
                    HorizontalDivider(Modifier.padding(vertical = Spacing.xs), color = scheme.outlineVariant)
                    if (passed) {
                        ExamPassed(exam.shortName(lang), Modifier.padding(vertical = Spacing.m))
                    } else if (onPassedRealExam != null) {
                        LinkButton(onClick = onPassedRealExam, modifier = Modifier.testTag("passed-${exam.id}")) {
                            Icon(Icons.Outlined.EmojiEvents, contentDescription = null, Modifier.size(Sizes.iconM))
                            Spacer(Modifier.width(Spacing.s))
                            Text(stringResource(R.string.passed_real_exam))
                        }
                    }
                }
            }
        }
    }
}

/** "✓ Hunting exam passed", in green: the user said they passed this exam for real; [name] is its short name. */
@Composable
fun ExamPassed(name: String, modifier: Modifier = Modifier) {
    val green = MaterialTheme.colorScheme.secondary
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Check, contentDescription = null, tint = green, modifier = Modifier.size(Sizes.iconS))
        Spacer(Modifier.width(Spacing.xs + Spacing.xxs))
        Text(stringResource(R.string.exam_passed, name), style = MaterialTheme.typography.labelMedium, color = green)
    }
}

/** Icon tile for an exam. */
@Composable
fun ExamAvatar(
    examId: String,
    modifier: Modifier = Modifier,
    size: BadgeSize = BadgeSize.Large,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.secondary,
) {
    IconBadge(examIcon(examId), modifier, size, containerColor, contentColor)
}

/** One exam in a list: badge, name, region and number of questions, with an optional trailing control. */
@Composable
fun ExamListItem(
    exam: Exam,
    questionCount: Int,
    modifier: Modifier = Modifier,
    leading: @Composable () -> Unit = { ExamAvatar(exam.id) },
    trailing: @Composable () -> Unit = {},
) {
    ListItem(
        headlineContent = { Text(exam.name(currentLang()), fontWeight = FontWeight.SemiBold) },
        supportingContent = {
            val count = pluralStringResource(R.plurals.questions_count, questionCount, questionCount)
            Text(if (exam.region.isEmpty() || exam.group == ExamGroup.HUNTING) count else "${exam.region} · $count")
        },
        leadingContent = leading,
        trailingContent = trailing,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier,
    )
}

/** Small ring showing how ready the user is for an exam. */
@Composable
fun ReadinessRing(value: Float, modifier: Modifier = Modifier) {
    val pct = (value * 100).roundToInt()
    val description = stringResource(R.string.readiness_value, pct)
    Column(
        modifier.clearAndSetSemantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProgressRing(value) {
            Text("$pct%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        }
        Spacer(Modifier.height(Spacing.xs))
        Text(stringResource(R.string.readiness), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Summary of [exam] from the bundled content and the user's progress. */
fun examSummary(content: Content, progress: UserProgress, exam: Exam): ExamSummary {
    val pool = content.questionsFor(exam)
    return ExamSummary(exam, pool.size, progress.readiness(pool), progress.weakIds(pool).size)
}
