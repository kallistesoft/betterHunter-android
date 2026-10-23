package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes

/** Badge sizes: the tile and the inset of the icon inside it. */
enum class BadgeSize(val size: Dp, val iconInset: Dp) {
    /** Answer letters (A, B, C, D). */
    Small(Sizes.badgeS, Sizes.badgeInsetS),
    Medium(Sizes.badgeM, Sizes.badgeInsetM),

    /** Exams in lists and cards, section tiles on the dashboard. */
    Large(Sizes.badgeL, Sizes.badgeInsetL),

    /** Big icon at the top of a screen. */
    ExtraLarge(Sizes.badgeXl, Sizes.badgeInsetXl),

    /** The one icon of a confirmation screen ("Did you pass your hunter course?"). */
    Hero(Sizes.badgeHero, Sizes.badgeInsetHero),
}

/** A tinted rounded tile holding an icon or a letter (green tint by default). */
@Composable
fun IconBadge(
    modifier: Modifier = Modifier,
    size: BadgeSize = BadgeSize.Large,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        modifier.size(size.size),
        shape = MaterialTheme.shapes.extraSmall,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center, content = content)
    }
}

@Composable
fun IconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: BadgeSize = BadgeSize.Large,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = contentColorFor(containerColor),
) {
    IconBadge(modifier, size, containerColor, contentColor) {
        Icon(icon, contentDescription = null, modifier = Modifier.fillMaxSize().padding(size.iconInset))
    }
}
