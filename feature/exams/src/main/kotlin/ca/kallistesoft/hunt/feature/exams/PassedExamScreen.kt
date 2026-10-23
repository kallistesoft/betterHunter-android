package ca.kallistesoft.hunt.feature.exams

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ca.kallistesoft.hunt.core.designsystem.component.BadgeSize
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.BottomActionBar
import ca.kallistesoft.hunt.core.designsystem.component.IconBadge
import ca.kallistesoft.hunt.core.designsystem.component.LinkButton
import ca.kallistesoft.hunt.core.designsystem.component.Overline
import ca.kallistesoft.hunt.core.designsystem.component.PrimaryButton
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.R

/**
 * "Did you pass your hunter course?", from an exam card in Exam training: what changes, then "Yes, I passed"
 * ([onConfirmed] once saved) or "Not yet". The app takes the user's word; nothing is verified.
 */
@Composable
fun PassedExamScreen(
    examId: String,
    onConfirmed: () -> Unit,
    onBack: () -> Unit,
    viewModel: PassedExamViewModel = hiltViewModel<PassedExamViewModel, PassedExamViewModel.Factory>(
        creationCallback = { factory -> factory.create(examId) },
    ),
) {
    PassedExamContent(examId, onConfirm = { viewModel.confirm(onConfirmed) }, onBack = onBack)
}

@Composable
internal fun PassedExamContent(examId: String, onConfirm: () -> Unit, onBack: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Scaffold(
        topBar = { BackTopAppBar("", onBack) },
        bottomBar = {
            BottomActionBar(
                below = {
                    LinkButton(onClick = onBack, modifier = Modifier.testTag("passed-not-yet")) {
                        Text(stringResource(R.string.passed_not_yet))
                    }
                },
            ) {
                PrimaryButton(
                    stringResource(R.string.passed_yes),
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f).testTag("passed-yes"),
                    icon = Icons.Rounded.Check,
                )
            }
        },
    ) { padding ->
        LazyColumn(contentPadding = padding.plus(top = Spacing.l), horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                IconBadge(
                    Icons.Outlined.EmojiEvents,
                    size = BadgeSize.Hero,
                    containerColor = scheme.tertiaryContainer,
                    contentColor = scheme.tertiary,
                )
            }
            item {
                Column(
                    Modifier.padding(top = Spacing.l, bottom = Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.s),
                ) {
                    Overline(stringResource(R.string.real_exam))
                    Text(
                        stringResource(R.string.passed_title, examId),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(stringResource(R.string.passed_sub), color = scheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
            item {
                BhCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
                        Change(Icons.Outlined.Visibility, R.string.passed_change_home, orange = true)
                        Change(Icons.Outlined.School, R.string.passed_change_training)
                        Change(Icons.Outlined.PersonOutline, R.string.passed_change_profile)
                    }
                }
            }
        }
    }
}

/** One thing that changes, with its section's icon; the text's `<b>` marks the section name. */
@Composable
private fun Change(icon: ImageVector, text: Int, orange: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconBadge(
            icon,
            size = BadgeSize.Medium,
            containerColor = if (orange) scheme.tertiaryContainer else scheme.secondaryContainer,
            contentColor = if (orange) scheme.tertiary else scheme.secondary,
        )
        Spacer(Modifier.width(Spacing.m))
        Text(AnnotatedString.fromHtml(stringResource(text)), style = MaterialTheme.typography.bodyMedium)
    }
}
