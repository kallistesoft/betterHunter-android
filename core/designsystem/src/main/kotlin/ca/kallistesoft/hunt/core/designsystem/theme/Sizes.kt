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
    val badgeS = 32.dp
    val badgeM = 40.dp
    val badgeL = 44.dp
    val badgeXl = 60.dp
    val badgeHero = 84.dp
    val badgeInsetS = 6.dp
    val badgeInsetM = 8.dp
    val badgeInsetL = 10.dp
    val badgeInsetXl = 14.dp
    val badgeInsetHero = 22.dp

    // Progress rings and their stroke
    val ringS = 48.dp
    val ringM = 64.dp
    val ringL = 160.dp

    /** Daily goal ring on the dashboard. */
    val ringXl = 84.dp
    val ringStrokeXl = 8.dp
    val ringStrokeS = 5.dp
    val ringStrokeM = 7.dp
    val ringStrokeL = 12.dp
    val loader = 28.dp
    val loaderStroke = 3.dp

    // Borders
    val border = 1.dp
    val borderEmphasis = 1.5.dp

    // Touch and buttons
    /** Smallest touch target (round icon buttons, toggle options). */
    val touchTarget = 44.dp
    val buttonHeight = 52.dp
    val toggleHeight = 38.dp
    val togglePadding = 3.dp
    val chipHeight = 32.dp
    val logo = 34.dp

    // Progress bars
    val progressBar = 5.dp
    val meterBar = 6.dp
    val segmentGap = 3.dp

    // Content
    val answerPadding = 14.dp
    val cardPadding = 18.dp
    val answerRowMinHeight = 60.dp
    val answerTileMinHeight = 64.dp

    /** Onboarding choices ([ca.kallistesoft.hunt.core.designsystem.component.ChoiceCard]) and their radio or check mark. */
    val choiceMinHeight = 80.dp
    val selectionMark = 22.dp
    val checkboxCorner = 6.dp

    /** A row of a settings card (icon tile, title, value). */
    val settingRowMinHeight = 72.dp
    val photoThumb = 72.dp

    /** Start inset of a divider in a list of items led by a large badge (aligns with the text). */
    val listDividerInset = 72.dp
}
