package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing

/**
 * The main actions of a screen, pinned to the bottom (a Scaffold's `bottomBar`). Opaque, so the list scrolling under
 * it never shows through the buttons, and above the navigation bar when drawing edge to edge.
 */
@Composable
fun BottomActionBar(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Surface(modifier, color = MaterialTheme.colorScheme.background) {
        Row(
            Modifier.navigationBarsPadding().padding(Spacing.l).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}
