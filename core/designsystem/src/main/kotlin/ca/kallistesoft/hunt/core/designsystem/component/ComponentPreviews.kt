package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
            AnswerChoice("Mule deer", AnswerState.Selected, onClick = {}, modifier = Modifier.weight(1f), subtitle = "Cerf mulet")
            AnswerChoice("Elk", AnswerState.Idle, onClick = {}, modifier = Modifier.weight(1f), subtitle = "Wapiti")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            AnswerChoice("Mule deer", AnswerState.Wrong, onClick = null, modifier = Modifier.weight(1f))
            AnswerChoice("White-tailed deer", AnswerState.Correct, onClick = null, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            ResultChip(correct = true, text = "Correct")
            ResultChip(correct = false, text = "Incorrect")
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
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            HubCard(
                icon = Icons.Outlined.School,
                title = "Exam training",
                subtitle = "Licence and hunter exams",
                tone = HubTone.Green,
                onClick = {},
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                LabelledMeter("Readiness", "64 %", 0.64f)
            }
            HubCard(
                icon = Icons.Outlined.Visibility,
                title = "What is it?",
                subtitle = "Identify Canada's game animals",
                tone = HubTone.Orange,
                onClick = {},
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Text("66 animals · 1 game", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
            }
        }
        BhCard { Text("Card content", Modifier.padding(Spacing.l)) }
        ChoiceCard(Icons.Outlined.School, "Working on it", "Preparing for my licence", selected = true, mode = ChoiceMode.Single, onClick = {})
        ChoiceCard(Icons.Outlined.Shield, "Firearm", "Rifle, shotgun or muzzleloader", selected = false, mode = ChoiceMode.Multiple, onClick = {})
    }
}

@BhPreviews
@Composable
internal fun SmallComponentsPreview() = PreviewSurface {
    Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        SectionTitle("My exams") { LinkButton(onClick = {}) { Text("Change") } }
        PillToggle(listOf("FR", "EN"), selected = 1, onSelect = {}, description = "Language")
        QuizProgress(
            List(20) { i ->
                when {
                    i < 2 -> SegmentState.Right
                    i == 2 -> SegmentState.Wrong
                    i == 3 -> SegmentState.Current
                    else -> SegmentState.Upcoming
                }
            },
            progress = 0.2f,
        )
        QuizProgress(segments = null, progress = 0.36f)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            IconBadge(size = BadgeSize.Small) { Text("A") }
            IconBadge(Icons.Outlined.Shield, size = BadgeSize.Medium)
            IconBadge(Icons.Outlined.Shield)
            IconBadge(Icons.Outlined.School, size = BadgeSize.ExtraLarge)
            ProgressRing(0.6f) { Text("60%", style = MaterialTheme.typography.labelMedium) }
            BuckLogo()
        }
        LinkRow(Icons.Outlined.Email, "contact@kallistesoft.ca", onClick = {})
        BottomActionBar {
            SecondaryButton("Home", onClick = {}, modifier = Modifier.weight(1f))
            PrimaryButton("Try again", onClick = {}, modifier = Modifier.weight(1f))
        }
    }
}
