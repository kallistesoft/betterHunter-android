package ca.kallistesoft.hunt.feature.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
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
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.BhPreviews
import ca.kallistesoft.hunt.core.designsystem.component.NoticeCard
import ca.kallistesoft.hunt.core.designsystem.component.NoticeTone
import ca.kallistesoft.hunt.core.designsystem.component.PreviewSurface
import ca.kallistesoft.hunt.core.designsystem.component.RowEnd
import ca.kallistesoft.hunt.core.designsystem.component.SectionTitle
import ca.kallistesoft.hunt.core.designsystem.component.SettingRow
import ca.kallistesoft.hunt.core.designsystem.component.SettingsCard
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.manuals
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.currentLang
import ca.kallistesoft.hunt.core.ui.rememberLinkOpener

/** Disclaimer, how the app works and its sources (contact, privacy and the version are in Settings). */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val links = rememberLinkOpener()
    val lang = currentLang()
    Scaffold(topBar = { BackTopAppBar(stringResource(R.string.about), onBack) }) { padding ->
        LazyColumn(contentPadding = padding.plus(bottom = Spacing.xxxl), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            item {
                NoticeCard(stringResource(R.string.disclaimer), NoticeTone.Info, Modifier.testTag("disclaimer"), icon = Icons.Outlined.Info)
            }
            item { SectionTitle(stringResource(R.string.how_it_works)) }
            item {
                BhCard {
                    Column(Modifier.padding(horizontal = Spacing.l)) {
                        Labelled(stringResource(R.string.practice), stringResource(R.string.how_practice))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Labelled(stringResource(R.string.exam_sim), stringResource(R.string.how_exam))
                    }
                }
            }
            item { SectionTitle(stringResource(R.string.sources)) }
            item {
                SettingsCard(
                    rows = manuals.values.map { manual ->
                        {
                            SettingRow(
                                Icons.AutoMirrored.Outlined.MenuBook,
                                manual.title(lang),
                                onClick = { links.open(manual.url) },
                                end = RowEnd.External,
                            )
                        }
                    },
                )
            }
            item {
                Text(
                    stringResource(R.string.photos_credit),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.s),
                )
            }
        }
    }
}

/** "Practice: answer and explanation after every question", the label in bold. */
@Composable
private fun Labelled(label: String, text: String) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(stringResource(R.string.label_colon, label)) }
            append(" ")
            append(text)
        },
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = Spacing.l),
    )
}

@BhPreviews
@Composable
internal fun AboutPreview() = PreviewSurface { AboutScreen(onBack = {}) }
