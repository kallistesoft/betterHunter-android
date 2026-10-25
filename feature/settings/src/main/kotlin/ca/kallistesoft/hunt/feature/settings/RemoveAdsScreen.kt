package ca.kallistesoft.hunt.feature.settings

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.billing.AdFreeProduct
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.IconBadge
import ca.kallistesoft.hunt.core.designsystem.component.LinkButton
import ca.kallistesoft.hunt.core.designsystem.component.NoticeCard
import ca.kallistesoft.hunt.core.designsystem.component.NoticeTone
import ca.kallistesoft.hunt.core.designsystem.component.PrimaryButton
import ca.kallistesoft.hunt.core.designsystem.component.SecondaryButton
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.shortDate

/**
 * Remove ads (P15): 7 days, kept on this phone only (said under the option and again before Play's sheet), or forever,
 * restored by Play; and Restore purchases.
 */
@Composable
fun RemoveAdsScreen(onBack: () -> Unit, viewModel: RemoveAdsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    RemoveAdsContent(
        state,
        onBack = onBack,
        onBuy = { product -> activity?.let { viewModel.buy(it, product) } },
        onRestore = viewModel::restore,
        onDismissMessage = viewModel::dismissMessage,
    )
}

@Composable
internal fun RemoveAdsContent(
    state: RemoveAdsUiState,
    onBack: () -> Unit,
    onBuy: (AdFreeProduct) -> Unit,
    onRestore: () -> Unit,
    onDismissMessage: () -> Unit,
) {
    // The week's limit is said once more before Play's sheet opens.
    var confirmWeek by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        topBar = { BackTopAppBar(stringResource(R.string.remove_ads), onBack) },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        if (state.loading) return@Scaffold
        LazyColumn(
            Modifier.consumeWindowInsets(padding),
            contentPadding = padding.plus(top = Spacing.s, bottom = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            item {
                Text(
                    stringResource(R.string.remove_ads_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (state.busy) item { LinearProgressIndicator(Modifier.fillMaxWidth().testTag("remove-ads-busy")) }
            state.message?.let { message -> item { MessageCard(message, onDismissMessage) } }
            if (state.forever) {
                item { NoticeCard(stringResource(R.string.remove_ads_thanks), NoticeTone.Info, Modifier.fillMaxWidth().testTag("remove-ads-forever-owned"), Icons.Rounded.Check) }
            } else {
                state.untilDay?.let { day ->
                    item {
                        NoticeCard(
                            stringResource(R.string.remove_ads_week_running, shortDate(day)),
                            NoticeTone.Info,
                            Modifier.fillMaxWidth().testTag("remove-ads-week-running"),
                            Icons.Rounded.Check,
                        )
                    }
                }
                item {
                    OfferCard(
                        Icons.Outlined.CalendarMonth,
                        stringResource(R.string.remove_ads_week_title),
                        price = state.prices[AdFreeProduct.WEEK],
                        enabled = !state.busy,
                        primary = false,
                        tag = "remove-ads-week",
                        onBuy = { confirmWeek = true },
                    ) {
                        Note(Icons.Outlined.PhoneAndroid, stringResource(R.string.remove_ads_week_lost), warning = true)
                        Note(Icons.Outlined.Info, stringResource(R.string.remove_ads_week_more))
                    }
                }
                item {
                    OfferCard(
                        Icons.Outlined.Verified,
                        stringResource(R.string.remove_ads_forever_title),
                        price = state.prices[AdFreeProduct.FOREVER],
                        enabled = !state.busy,
                        primary = true,
                        tag = "remove-ads-forever",
                        onBuy = { onBuy(AdFreeProduct.FOREVER) },
                    ) {
                        Note(Icons.Outlined.Restore, stringResource(R.string.remove_ads_forever_kept))
                    }
                }
            }
            item {
                LinkButton(onClick = onRestore, modifier = Modifier.testTag("remove-ads-restore")) {
                    Icon(Icons.Outlined.Restore, contentDescription = null, Modifier.size(Sizes.iconM))
                    Spacer(Modifier.width(Spacing.s))
                    Text(stringResource(R.string.remove_ads_restore))
                }
            }
        }
    }

    if (confirmWeek) {
        AlertDialog(
            onDismissRequest = { confirmWeek = false },
            icon = { Icon(Icons.Outlined.PhoneAndroid, contentDescription = null) },
            title = { Text(stringResource(R.string.remove_ads_week_confirm_title)) },
            text = { Text(stringResource(R.string.remove_ads_week_lost)) },
            confirmButton = {
                PrimaryButton(
                    stringResource(R.string.remove_ads_continue),
                    onClick = {
                        confirmWeek = false
                        onBuy(AdFreeProduct.WEEK)
                    },
                    modifier = Modifier.testTag("remove-ads-week-continue"),
                )
            },
            dismissButton = { LinkButton(onClick = { confirmWeek = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

/** One way to remove ads: its name, its notes, and "Buy · CA$1.99" with Play's price. */
@Composable
private fun OfferCard(
    icon: ImageVector,
    title: String,
    price: String?,
    enabled: Boolean,
    primary: Boolean,
    tag: String,
    onBuy: () -> Unit,
    notes: @Composable () -> Unit,
) {
    BhCard(Modifier.fillMaxWidth().testTag(tag)) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon)
                Spacer(Modifier.width(Spacing.m))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            notes()
            val label = price?.let { stringResource(R.string.remove_ads_buy, it) } ?: stringResource(R.string.remove_ads_price_unknown)
            val modifier = Modifier.fillMaxWidth().testTag("$tag-buy")
            if (primary) {
                PrimaryButton(label, onClick = onBuy, modifier = modifier, enabled = enabled && price != null)
            } else {
                SecondaryButton(label, onClick = onBuy, modifier = modifier, enabled = enabled && price != null)
            }
        }
    }
}

/** A line under an offer; [warning] stands out (the week is lost with the app). */
@Composable
private fun Note(icon: ImageVector, text: String, warning: Boolean = false) {
    val color = if (warning) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(Sizes.iconM))
        Spacer(Modifier.width(Spacing.s))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = if (warning) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun MessageCard(message: RemoveAdsMessage, onDismiss: () -> Unit) {
    val (text, tone) = when (message) {
        RemoveAdsMessage.PENDING -> R.string.remove_ads_pending to NoticeTone.Info
        RemoveAdsMessage.RESTORED -> R.string.remove_ads_restored to NoticeTone.Info
        RemoveAdsMessage.UNAVAILABLE -> R.string.remove_ads_unavailable to NoticeTone.Warning
        RemoveAdsMessage.FAILED -> R.string.remove_ads_failed to NoticeTone.Warning
        RemoveAdsMessage.RESTORE_FAILED -> R.string.remove_ads_restore_failed to NoticeTone.Warning
    }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        NoticeCard(stringResource(text), tone, Modifier.fillMaxWidth().testTag("remove-ads-message"))
        LinkButton(onClick = onDismiss, modifier = Modifier.testTag("remove-ads-message-ok")) { Text(stringResource(R.string.ok)) }
    }
}
