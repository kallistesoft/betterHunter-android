package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Round badge sizes: the circle and the inset of the icon inside it. */
enum class BadgeSize(val size: Dp, val iconInset: Dp) {
    /** Answer letters (A, B, C, D). */
    Small(28.dp, 6.dp),
    Medium(40.dp, 8.dp),

    /** Exams in lists and cards. */
    Large(44.dp, 10.dp),

    /** Section entry points on the dashboard. */
    ExtraLarge(60.dp, 14.dp),
}

/** A coloured circle holding an icon or a letter. */
@Composable
fun IconBadge(
    modifier: Modifier = Modifier,
    size: BadgeSize = BadgeSize.Large,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(modifier.size(size.size), shape = CircleShape, color = containerColor, contentColor = contentColor) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center, content = content)
    }
}

@Composable
fun IconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: BadgeSize = BadgeSize.Large,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = contentColorFor(containerColor),
) {
    IconBadge(modifier, size, containerColor, contentColor) {
        Icon(icon, contentDescription = null, modifier = Modifier.fillMaxSize().padding(size.iconInset))
    }
}
