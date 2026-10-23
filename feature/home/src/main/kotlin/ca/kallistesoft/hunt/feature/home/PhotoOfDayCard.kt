package ca.kallistesoft.hunt.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ca.kallistesoft.hunt.core.designsystem.component.AnswerChoice
import ca.kallistesoft.hunt.core.designsystem.component.AnswerState
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.Overline
import ca.kallistesoft.hunt.core.designsystem.component.ResultChip
import ca.kallistesoft.hunt.core.designsystem.component.SecondaryButton
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.ui.PhotoCaption
import ca.kallistesoft.hunt.core.ui.QuestionPhoto
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.currentLang
import ca.kallistesoft.hunt.core.ui.otherLanguageChoice

/**
 * One "What is it?" photo per day, answered right on the dashboard: the photo with its caption, four names two by two,
 * then the animal's name on the photo and how to recognise it.
 */
@Composable
internal fun PhotoOfDayCard(photo: PhotoOfDayState, onAnswer: (Int) -> Unit, onPlay: () -> Unit) {
    val q = photo.question
    val lang = currentLang()
    val text = q.text(lang)
    val chosen = photo.choice
    val right = chosen == q.answerIndex

    Column(Modifier.testTag("photo-of-day"), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        QuestionPhoto(q, maxHeightFraction = 0.32f, zoomHint = false) {
            if (chosen != null) {
                ResultChip(
                    correct = right,
                    text = stringResource(if (right) R.string.correct else R.string.wrong),
                    modifier = Modifier.align(Alignment.TopStart).padding(Spacing.m),
                )
            }
            PhotoCaption(
                overline = stringResource(R.string.photo_of_day),
                title = if (chosen == null) stringResource(R.string.what_is_it) else text.choices[q.answerIndex],
                subtitle = if (chosen == null) null else q.otherLanguageChoice(lang, q.answerIndex),
            )
        }
        // Two by two; after answering, the right one in green and a wrong pick in red.
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.s + Spacing.xxs)) {
            text.choices.indices.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s + Spacing.xxs)) {
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
                            subtitle = q.otherLanguageChoice(lang, i),
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
            BhCard(shape = MaterialTheme.shapes.medium) {
                Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Overline(stringResource(R.string.how_recognise), color = MaterialTheme.colorScheme.secondary)
                    Text(text.explanation, style = MaterialTheme.typography.bodyMedium)
                }
            }
            SecondaryButton(
                stringResource(R.string.play_id),
                onClick = onPlay,
                modifier = Modifier.fillMaxWidth().testTag("pod-play"),
                icon = Icons.Outlined.PhotoLibrary,
            )
        }
    }
}
