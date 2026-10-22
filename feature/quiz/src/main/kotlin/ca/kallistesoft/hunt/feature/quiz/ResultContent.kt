package ca.kallistesoft.hunt.feature.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.BottomActionBar
import ca.kallistesoft.hunt.core.designsystem.component.ProgressRing
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.PhotoThumb
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.SourceLinks
import ca.kallistesoft.hunt.core.ui.currentLang
import kotlin.math.roundToInt

/** Score, pass/fail for mock exams, and every missed question with its answer and explanation. */
@Composable
internal fun ResultContent(state: QuizUiState.Finished, onHome: () -> Unit, onAgain: () -> Unit) {
    val s = state.session
    val lang = currentLang()
    val isExam = s.mode == QuizMode.EXAM
    val success = BetterHunterTheme.extendedColors.success
    val color = when {
        !isExam -> MaterialTheme.colorScheme.primary
        s.passed -> success
        else -> MaterialTheme.colorScheme.error
    }

    Scaffold(
        topBar = { BackTopAppBar(stringResource(R.string.score), onBack = null) },
        bottomBar = {
            BottomActionBar {
                OutlinedButton(onClick = onHome, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.home)) }
                Button(onClick = onAgain, modifier = Modifier.weight(1f).testTag("again")) {
                    Text(stringResource(R.string.again))
                }
            }
        },
    ) { padding ->
        LazyColumn(
            contentPadding = padding.plus(top = Spacing.l, bottom = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            if (state.timeUp) {
                item {
                    Text(
                        stringResource(R.string.time_up),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = Spacing.m), contentAlignment = Alignment.Center) {
                    ProgressRing(s.score.toFloat(), size = Sizes.ringL, strokeWidth = Sizes.ringStrokeL, color = color) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${(s.score * 100).roundToInt()} %",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = color,
                                modifier = Modifier.testTag("score"),
                            )
                            Text("${s.correctCount} / ${s.questions.size}", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
            if (isExam) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    stringResource(
                                        R.string.pass_result,
                                        stringResource(if (s.passed) R.string.passed else R.string.failed),
                                        (s.exam.passMark * 100).roundToInt(),
                                    ),
                                    fontWeight = FontWeight.SemiBold,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    if (s.passed) Icons.Rounded.Verified else Icons.Outlined.ErrorOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(Sizes.iconL),
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(labelColor = color, leadingIconContentColor = color),
                            border = BorderStroke(Sizes.border, color),
                        )
                    }
                }
            }
            item {
                Text(
                    stringResource(R.string.missed_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = Spacing.l),
                )
            }
            if (s.missed.isEmpty()) item { Text(stringResource(R.string.all_correct)) }
            items(s.missed, key = { it.first.id }) { (q, picked) ->
                val text = q.text(lang)
                BhCard {
                    Column(Modifier.padding(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        q.image?.let { PhotoThumb(it, Modifier.padding(bottom = Spacing.xs)) }
                        Text(text.q, style = MaterialTheme.typography.titleSmall)
                        picked?.let {
                            Text(stringResource(R.string.your_answer, text.choices[it]), color = MaterialTheme.colorScheme.error)
                        }
                        Text(
                            stringResource(R.string.answer, text.choices[q.answerIndex]),
                            color = success,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(text.explanation, style = MaterialTheme.typography.bodySmall)
                        SourceLinks(q)
                    }
                }
            }
        }
    }
}
