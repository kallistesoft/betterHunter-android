package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing

// Previews of the design system. Internal so the screenshot tests render the same composables.

@BhPreviews
@Composable
internal fun AnswerChoicePreview() = PreviewSurface {
    Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        AnswerChoice("A valid firearms licence", AnswerState.Idle, onClick = {}, letter = "A")
        AnswerChoice("Photo ID only", AnswerState.Selected, onClick = {}, letter = "B")
        AnswerChoice("A provincial hunter certificate", AnswerState.Correct, onClick = null, letter = "C")
        AnswerChoice("Proof of shooting club membership", AnswerState.Wrong, onClick = null, letter = "D")
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            AnswerChoice("Mule deer", AnswerState.Wrong, onClick = null, modifier = Modifier.weight(1f))
            AnswerChoice("White-tailed deer", AnswerState.Correct, onClick = null, modifier = Modifier.weight(1f))
        }
        AnswerResult(correct = true, text = "Correct!")
        AnswerResult(correct = false, text = "Incorrect")
    }
}

@BhPreviews
@Composable
internal fun CardsPreview() = PreviewSurface {
    Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        NoticeCard("Sample content, not reviewed. Do not use to study.", NoticeTone.Warning, icon = Icons.Rounded.WarningAmber)
        NoticeCard("Independent study aid, not affiliated with the RCMP.", NoticeTone.Info)
        HubCard(
            icon = Icons.Outlined.School,
            title = "Exam training",
            subtitle = "Firearms licence and hunter exams: practice and mock exams.",
            facts = listOf("4 exams", "Readiness 60 %"),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            onClick = {},
        )
        BhCard { Text("Card content", Modifier.padding(Spacing.l)) }
    }
}

@BhPreviews
@Composable
internal fun SmallComponentsPreview() = PreviewSurface {
    Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        SectionTitle("My exams") { TextButton(onClick = {}) { Text("Change") } }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            IconBadge(size = BadgeSize.Small) { Text("A") }
            IconBadge(Icons.Outlined.Shield, size = BadgeSize.Medium)
            IconBadge(Icons.Outlined.Shield)
            IconBadge(Icons.Outlined.School, size = BadgeSize.ExtraLarge)
            ProgressRing(0.6f) { Text("60%", style = MaterialTheme.typography.labelMedium) }
        }
        LinkRow(Icons.Outlined.Email, "contact@kallistesoft.ca", onClick = {})
    }
}
