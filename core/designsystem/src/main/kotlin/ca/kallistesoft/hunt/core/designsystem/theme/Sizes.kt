package ca.kallistesoft.hunt.core.designsystem.theme

import androidx.compose.ui.unit.dp

/** Component sizes: the only place, with [Spacing], where sizes are given in dp. */
object Sizes {
    // Icons
    val iconXs = 14.dp
    val iconS = 16.dp
    val iconM = 18.dp
    val iconL = 20.dp
    val iconXl = 40.dp
    val iconHero = 72.dp

    // Round badges and the inset of their icon
    val badgeS = 28.dp
    val badgeM = 40.dp
    val badgeL = 44.dp
    val badgeXl = 60.dp
    val badgeInsetS = 6.dp
    val badgeInsetM = 8.dp
    val badgeInsetL = 10.dp
    val badgeInsetXl = 14.dp

    // Progress rings and their stroke
    val ringS = 48.dp
    val ringM = 64.dp
    val ringL = 160.dp
    val ringStrokeS = 5.dp
    val ringStrokeM = 7.dp
    val ringStrokeL = 12.dp
    val loader = 28.dp
    val loaderStroke = 3.dp

    // Borders
    val border = 1.dp
    val borderEmphasis = 2.dp

    // Content
    val answerPadding = 14.dp
    val answerTileMinHeight = 52.dp
    val photoThumb = 72.dp

    /** Start inset of a divider in a list of items led by a large badge (aligns with the text). */
    val listDividerInset = 72.dp
}
