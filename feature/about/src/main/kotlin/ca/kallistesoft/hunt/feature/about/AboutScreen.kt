package ca.kallistesoft.hunt.feature.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
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
        LazyColumn(contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 32.dp), modifier = Modifier.padding(padding)) {
            item {
                BhCard(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f), border = null) {
                    Text(stringResource(R.string.disclaimer), Modifier.padding(16.dp).testTag("disclaimer"))
                }
            }
            item { Heading(stringResource(R.string.how_it_works)) }
            item { Labelled(stringResource(R.string.practice), stringResource(R.string.how_practice)) }
            item { Labelled(stringResource(R.string.exam_sim), stringResource(R.string.how_exam)) }
            item { Heading(stringResource(R.string.sources)) }
            for (manual in manuals.values) {
                item { LinkRow(Icons.AutoMirrored.Outlined.MenuBook, manual.title(lang)) { links.open(manual.url) } }
            }
            item {
                Text(
                    stringResource(R.string.photos_credit),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            item { Heading(stringResource(R.string.contact)) }
            item { LinkRow(Icons.Outlined.Email, CONTACT_EMAIL) { links.email(CONTACT_EMAIL) } }
            item { LinkRow(Icons.Outlined.PrivacyTip, stringResource(R.string.privacy)) { links.open(privacyUrl) } }
            item {
                Text(
                    stringResource(R.string.version, versionName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 24.dp),
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
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
    )
}

@Composable
private fun Labelled(label: String, text: String) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append("$label : ") }
            append(text)
        },
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

@Composable
private fun LinkRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, Modifier.size(18.dp)) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick),
    )
}
