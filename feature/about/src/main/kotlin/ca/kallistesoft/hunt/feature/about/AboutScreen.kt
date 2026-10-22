package ca.kallistesoft.hunt.feature.about

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import ca.kallistesoft.hunt.core.designsystem.component.BhPreviews
import ca.kallistesoft.hunt.core.designsystem.component.LinkRow
import ca.kallistesoft.hunt.core.designsystem.component.NoticeCard
import ca.kallistesoft.hunt.core.designsystem.component.NoticeTone
import ca.kallistesoft.hunt.core.designsystem.component.PreviewSurface
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.manuals
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.CONTACT_EMAIL
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.currentLang
import ca.kallistesoft.hunt.core.ui.rememberLinkOpener

/** Disclaimer, how the app works, sources, contact and privacy. */
@Composable
fun AboutScreen(versionName: String, onBack: () -> Unit) {
    val links = rememberLinkOpener()
    val lang = currentLang()
    val privacyUrl = stringResource(R.string.privacy_url)
    Scaffold(topBar = { BackTopAppBar(stringResource(R.string.about), onBack) }) { padding ->
        LazyColumn(contentPadding = padding.plus(bottom = Spacing.xxxl)) {
            item { NoticeCard(stringResource(R.string.disclaimer), NoticeTone.Info, Modifier.testTag("disclaimer")) }
            item { Heading(stringResource(R.string.how_it_works)) }
            item { Labelled(stringResource(R.string.practice), stringResource(R.string.how_practice)) }
            item { Labelled(stringResource(R.string.exam_sim), stringResource(R.string.how_exam)) }
            item { Heading(stringResource(R.string.sources)) }
            for (manual in manuals.values) {
                item { LinkRow(Icons.AutoMirrored.Outlined.MenuBook, manual.title(lang), onClick = { links.open(manual.url) }) }
            }
            item {
                Text(
                    stringResource(R.string.photos_credit),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
            item { Heading(stringResource(R.string.contact)) }
            item { LinkRow(Icons.Outlined.Email, CONTACT_EMAIL, onClick = { links.email(CONTACT_EMAIL) }) }
            item { LinkRow(Icons.Outlined.PrivacyTip, stringResource(R.string.privacy), onClick = { links.open(privacyUrl) }) }
            item {
                Text(
                    stringResource(R.string.version, versionName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xxl),
                )
            }
        }
    }
}

@Composable
private fun Heading(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = Spacing.xxl, bottom = Spacing.s),
    )
}

@Composable
private fun Labelled(label: String, text: String) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("$label : ") }
            append(text)
        },
        modifier = Modifier.padding(bottom = Spacing.s),
    )
}

@BhPreviews
@Composable
internal fun AboutPreview() = PreviewSurface { AboutScreen(versionName = "1.0.0", onBack = {}) }
