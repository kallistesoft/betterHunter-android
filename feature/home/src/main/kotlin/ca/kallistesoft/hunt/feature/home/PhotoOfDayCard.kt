package ca.kallistesoft.hunt.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
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
    val success = BetterHunterTheme.extendedColors.success
    val error = MaterialTheme.colorScheme.error

    BhCard(Modifier.testTag("photo-of-day")) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.photo_of_day), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.size(12.dp))
            QuestionPhoto(q, maxHeightFraction = 0.3f)
            Text(text.q, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.size(8.dp))
            // Two by two; after answering, the right one in green and a wrong pick in red.
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                text.choices.indices.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { i ->
                            val color = when {
                                chosen == null -> null
                                i == q.answerIndex -> success
                                i == chosen -> error
                                else -> null
                            }
                            PhotoChoice(
                                label = text.choices[i],
                                color = color,
                                onClick = { onAnswer(i) }.takeIf { chosen == null },
                                modifier = Modifier.weight(1f).testTag("pod-choice-$i"),
                            )
                        }
                    }
                }
            }
            if (chosen != null) {
                Spacer(Modifier.size(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (right) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                        contentDescription = null,
                        tint = if (right) success else error,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(if (right) R.string.correct else R.string.wrong),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.how_recognise), style = MaterialTheme.typography.labelLarge)
                Text(text.explanation)
                Spacer(Modifier.size(8.dp))
                OutlinedButton(onClick = onPlay, modifier = Modifier.fillMaxWidth().testTag("pod-play")) {
                    Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.play_id))
                }
            }
        }
    }
}

@Composable
private fun PhotoChoice(label: String, color: Color?, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.small
    val border = BorderStroke(if (color == null) 1.dp else 2.dp, color ?: MaterialTheme.colorScheme.outlineVariant)
    val container = color?.copy(alpha = 0.15f) ?: Color.Transparent
    val content: @Composable () -> Unit = {
        Box(Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
            Text(label, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = shape, color = container, border = border, content = content)
    } else {
        Surface(modifier = modifier, shape = shape, color = container, border = border, content = content)
    }
}
