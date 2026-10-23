package ca.kallistesoft.hunt.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ca.kallistesoft.hunt.core.designsystem.component.Overline
import ca.kallistesoft.hunt.core.designsystem.component.TooltipIconButton

/**
 * Top bar of every screen below the dashboard: back arrow (or a cross with [closeIcon], to leave a mock exam), the
 * title centred in the condensed heading font with an optional [overline] above it, and [actions] on the right.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopAppBar(
    title: String,
    onBack: (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit = {},
    overline: String? = null,
    closeIcon: Boolean = false,
) = BackTopAppBarImpl(title, onBack, actions, overline, closeIcon, scrollBehavior = null)

/**
 * [BackTopAppBar] for long reading screens: it hides as the list scrolls down and comes back on the way up. The
 * screen also adds the [scrollBehavior]'s `nestedScrollConnection` to its Scaffold.
 */
@ExperimentalMaterial3Api
@Composable
fun BackTopAppBar(
    title: String,
    onBack: (() -> Unit)?,
    scrollBehavior: TopAppBarScrollBehavior,
    actions: @Composable RowScope.() -> Unit = {},
    overline: String? = null,
    closeIcon: Boolean = false,
) = BackTopAppBarImpl(title, onBack, actions, overline, closeIcon, scrollBehavior)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackTopAppBarImpl(
    title: String,
    onBack: (() -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
    overline: String?,
    closeIcon: Boolean,
    scrollBehavior: TopAppBarScrollBehavior?,
) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (overline != null) Overline(overline, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        navigationIcon = {
            if (onBack != null) {
                TooltipIconButton(
                    if (closeIcon) Icons.Rounded.Close else Icons.AutoMirrored.Rounded.ArrowBack,
                    label = stringResource(if (closeIcon) R.string.close else R.string.back),
                    onClick = onBack,
                )
            }
        },
        actions = actions,
        // Clear of a display cutout too (landscape), not only the status bar.
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        scrollBehavior = scrollBehavior,
    )
}
