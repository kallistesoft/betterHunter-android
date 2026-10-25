package ca.kallistesoft.hunt.feature.exams

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OndemandVideo
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.LinkButton
import ca.kallistesoft.hunt.core.designsystem.component.PrimaryButton
import ca.kallistesoft.hunt.core.designsystem.component.SecondaryButton
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.MOCK_COUNT
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.currentLang

/**
 * The ten mock exams of [examId] (P14). A mock never taken starts at once ([onStartMock], rewarded false); a retake,
 * and "Reset all", ask to watch a rewarded ad first, and a retake then starts with rewarded true (no interstitial).
 */
@Composable
fun MockExamsScreen(
    examId: String,
    onBack: () -> Unit,
    onStartMock: (mock: Int, rewarded: Boolean) -> Unit,
    viewModel: MockExamsViewModel = hiltViewModel<MockExamsViewModel, MockExamsViewModel.Factory>(
        creationCallback = { factory -> factory.create(examId) },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    state.start?.let { mock ->
        LaunchedEffect(mock) {
            viewModel.started()
            onStartMock(mock, true)
        }
    }
    MockExamsContent(
        state,
        onBack = onBack,
        onStart = { onStartMock(it, false) },
        onUnlock = viewModel::unlock,
        onDismissOffline = viewModel::dismissOffline,
    )
}

@Composable
internal fun MockExamsContent(
    state: MockExamsUiState,
    onBack: () -> Unit,
    onStart: (mock: Int) -> Unit,
    onUnlock: (MockUnlock) -> Unit,
    onDismissOffline: () -> Unit,
) {
    // The retake or reset waiting for "Watch ad": 1..10 a retake, 0 "Reset all", null none.
    var confirm by rememberSaveable { mutableStateOf<Int?>(null) }
    val lang = currentLang()
    Scaffold(
        topBar = { BackTopAppBar(stringResource(R.string.mock_exams), onBack, overline = state.exam?.label(lang)) },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        LazyColumn(
            Modifier.consumeWindowInsets(padding).testTag("mocks-list"),
            contentPadding = padding.plus(top = Spacing.s, bottom = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.s + Spacing.xxs),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Text(
                        stringResource(R.string.mocks_summary, state.passedCount, MOCK_COUNT),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.testTag("mocks-summary"),
                    )
                    Text(
                        stringResource(R.string.mocks_intro),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            // All ten done: "Reset all" right under the count, not below the list.
            if (state.allDone) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                        Text(
                            stringResource(R.string.mocks_all_done, MOCK_COUNT),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        PrimaryButton(
                            stringResource(R.string.mocks_reset_all),
                            onClick = { confirm = 0 },
                            enabled = !state.watchingAd,
                            icon = Icons.Outlined.OndemandVideo,
                            modifier = Modifier.fillMaxWidth().testTag("mocks-reset"),
                        )
                    }
                }
            }
            // The rewarded ad is loading (up to a few seconds) or on screen.
            if (state.watchingAd) item { LinearProgressIndicator(Modifier.fillMaxWidth().testTag("ad-loading")) }
            val next = state.mocks.firstOrNull { it.result == null }?.number
            items(state.mocks, key = { it.number }) { row ->
                MockRowCard(
                    row,
                    next = row.number == next,
                    busy = state.watchingAd,
                    onStart = { onStart(row.number) },
                    onRetake = { confirm = row.number },
                )
            }
        }
    }

    confirm?.let { which ->
        val reset = which == 0
        AlertDialog(
            onDismissRequest = { confirm = null },
            icon = { Icon(Icons.Outlined.OndemandVideo, contentDescription = null) },
            title = { Text(if (reset) stringResource(R.string.reset_title) else stringResource(R.string.retake_title, which)) },
            text = { Text(if (reset) stringResource(R.string.reset_body, MOCK_COUNT) else stringResource(R.string.retake_body, which)) },
            confirmButton = {
                PrimaryButton(
                    stringResource(R.string.watch_ad),
                    onClick = {
                        confirm = null
                        onUnlock(if (reset) MockUnlock.ResetAll else MockUnlock.Retake(which))
                    },
                    modifier = Modifier.testTag("watch-ad"),
                )
            },
            dismissButton = { LinkButton(onClick = { confirm = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }

    if (state.offline) {
        AlertDialog(
            onDismissRequest = onDismissOffline,
            icon = { Icon(Icons.Outlined.WifiOff, contentDescription = null) },
            title = { Text(stringResource(R.string.ad_offline_title)) },
            text = { Text(stringResource(R.string.ad_offline_body)) },
            confirmButton = {
                PrimaryButton(stringResource(R.string.ok), onClick = onDismissOffline, modifier = Modifier.testTag("offline-ok"))
            },
        )
    }
}

/**
 * "Mock 3": not taken (Start, the [next] one stands out), or passed / failed with its best and last scores (Retake,
 * behind an ad).
 */
@Composable
private fun MockRowCard(row: MockRow, next: Boolean, busy: Boolean, onStart: () -> Unit, onRetake: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val result = row.result
    val status = when {
        result == null -> stringResource(R.string.mock_not_taken)
        row.passed -> stringResource(R.string.mock_passed)
        else -> stringResource(R.string.mock_failed)
    }
    val statusColor = when {
        result == null -> scheme.onSurfaceVariant
        row.passed -> BetterHunterTheme.extendedColors.success
        else -> scheme.error
    }
    BhCard(Modifier.fillMaxWidth().testTag("mock-${row.number}")) {
        Row(
            Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                Text(
                    stringResource(R.string.mock_n, row.number),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(status, style = MaterialTheme.typography.labelMedium, color = statusColor, fontWeight = FontWeight.SemiBold)
                if (result != null) {
                    Text(
                        stringResource(R.string.mock_scores, result.best, result.last),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(Spacing.m))
            if (result == null && next) {
                PrimaryButton(
                    stringResource(R.string.mock_start),
                    onClick = onStart,
                    enabled = !busy,
                    icon = Icons.Rounded.PlayArrow,
                    modifier = Modifier.testTag("mock-start-${row.number}"),
                )
            } else if (result == null) {
                SecondaryButton(
                    stringResource(R.string.mock_start),
                    onClick = onStart,
                    enabled = !busy,
                    icon = Icons.Rounded.PlayArrow,
                    modifier = Modifier.testTag("mock-start-${row.number}"),
                )
            } else {
                SecondaryButton(
                    stringResource(R.string.mock_retake),
                    onClick = onRetake,
                    enabled = !busy,
                    icon = Icons.Outlined.OndemandVideo,
                    modifier = Modifier.testTag("mock-retake-${row.number}"),
                )
            }
        }
    }
}
