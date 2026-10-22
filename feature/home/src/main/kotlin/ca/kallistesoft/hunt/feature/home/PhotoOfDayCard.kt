package ca.kallistesoft.hunt.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.kallistesoft.hunt.core.designsystem.component.AnswerChoice
import ca.kallistesoft.hunt.core.designsystem.component.AnswerResult
import ca.kallistesoft.hunt.core.designsystem.component.AnswerState
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.ui.QuestionPhoto
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.currentLang

/** One "What is it?" photo per day, answered right on the dashboard. */
@Composable
internal fun PhotoOfDayCard(photo: PhotoOfDayState, onAnswer: (Int) -> Unit, onPlay: () -> Unit) {
    val q = photo.question
    val text = q.text(currentLang())
    val chosen = photo.choice
    val right = chosen == q.answerIndex

    BhCard(Modifier.testTag("photo-of-day")) {
        Column(Modifier.padding(Spacing.l)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(Spacing.s))
                Text(stringResource(R.string.photo_of_day), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.size(Spacing.m))
            QuestionPhoto(q, maxHeightFraction = 0.3f)
            Text(text.q, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.size(Spacing.s))
            // Two by two; after answering, the right one in green and a wrong pick in red.
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                text.choices.indices.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                        row.forEach { i ->
                            val state = when {
                                chosen == null -> AnswerState.Idle
                                i == q.answerIndex -> AnswerState.Correct
                                i == chosen -> AnswerState.Wrong
                                else -> AnswerState.Idle
                            }
                            AnswerChoice(
                                text = text.choices[i],
                                state = state,
                                onClick = { onAnswer(i) }.takeIf { chosen == null },
                                modifier = Modifier.weight(1f).testTag("pod-choice-$i"),
                                stateDescription = when (state) {
                                    AnswerState.Correct -> stringResource(R.string.correct)
                                    AnswerState.Wrong -> stringResource(R.string.wrong)
                                    else -> null
                                },
                            )
                        }
                    }
                }
            }
            if (chosen != null) {
                Spacer(Modifier.size(Spacing.m))
                AnswerResult(
                    correct = right,
                    text = stringResource(if (right) R.string.correct else R.string.wrong),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.how_recognise), style = MaterialTheme.typography.labelLarge)
                Text(text.explanation)
                Spacer(Modifier.size(Spacing.s))
                OutlinedButton(onClick = onPlay, modifier = Modifier.fillMaxWidth().testTag("pod-play")) {
                    Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(Spacing.s))
                    Text(stringResource(R.string.play_id))
                }
            }
        }
    }
}
