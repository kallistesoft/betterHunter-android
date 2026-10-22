package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A Scaffold's inner padding plus the screen's own margins, for a scrolling list's `contentPadding`. Edge to edge, the
 * list then scrolls under the transparent navigation bar while its last item still ends above it.
 */
@Composable
fun PaddingValues.plus(start: Dp = 16.dp, top: Dp = 0.dp, end: Dp = 16.dp, bottom: Dp = 24.dp): PaddingValues {
    val direction = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(direction) + start,
        top = calculateTopPadding() + top,
        end = calculateEndPadding(direction) + end,
        bottom = calculateBottomPadding() + bottom,
    )
}
