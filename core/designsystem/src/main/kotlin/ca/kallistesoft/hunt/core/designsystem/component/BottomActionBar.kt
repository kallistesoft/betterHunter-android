package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing

/**
 * The main actions of a screen, pinned to the bottom (a Scaffold's `bottomBar`). Opaque, so the list scrolling under
 * it never shows through the buttons, and above the navigation bar and clear of a display cutout when drawing edge to
 * edge. [below] goes under the buttons, centred (a short note, or a link-like second action).
 */
@Composable
fun BottomActionBar(
    modifier: Modifier = Modifier,
    below: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(modifier, color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .padding(horizontal = Spacing.screen, vertical = Spacing.m).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s + Spacing.xxs),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
            below?.invoke(this)
        }
    }
}
