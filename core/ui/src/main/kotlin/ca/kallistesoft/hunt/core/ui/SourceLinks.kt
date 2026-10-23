package ca.kallistesoft.hunt.core.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ca.kallistesoft.hunt.core.designsystem.component.LinkButton
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.Lang
import ca.kallistesoft.hunt.core.model.Question
import ca.kallistesoft.hunt.core.model.SourceRef

/**
 * "Source" links under an explanation: the manual page, plus the official page confirming the current rule when the
 * question has one (for photo ID questions, the species' fact sheet). In a list, pass [lang] and [links] read once
 * by the list rather than once per item.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SourceLinks(
    question: Question,
    modifier: Modifier = Modifier,
    lang: Lang = currentLang(),
    links: LinkOpener = rememberLinkOpener(),
) {
    val manual = SourceRef.parse(question.sourceRef, lang)
    val rule = question.sourceUrl
    if (manual == null && rule == null) return
    FlowRow(modifier, itemVerticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.source), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        manual?.let { SourceLink(it.label) { links.open(it.url) } }
        rule?.let {
            SourceLink(stringResource(if (question.image != null) R.string.species_page else R.string.current_rule)) {
                links.open(it)
            }
        }
    }
}

@Composable
private fun SourceLink(label: String, onClick: () -> Unit) {
    LinkButton(onClick = onClick) {
        Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, Modifier.size(Sizes.iconXs))
        Spacer(Modifier.width(Spacing.xs))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
