package ca.kallistesoft.hunt.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.HubCard
import ca.kallistesoft.hunt.core.designsystem.component.HubTone
import ca.kallistesoft.hunt.core.designsystem.component.IconBadge
import ca.kallistesoft.hunt.core.designsystem.component.LinkButton
import ca.kallistesoft.hunt.core.designsystem.component.Overline
import ca.kallistesoft.hunt.core.designsystem.component.PrimaryButton
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.FieldRun
import ca.kallistesoft.hunt.core.model.SeasonStatus
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.shortDate

/**
 * Before the season: "Your season starts in 3 weeks" (the hunter's own date) or, without a date, "Hunting season is
 * coming" with "Set my season start date". Tapping it starts the season check.
 */
@Composable
internal fun SeasonCard(season: SeasonStatus, onCheck: () -> Unit, onSetDate: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val size = FieldRun.SEASON_CHECK.size
    BhCard(
        Modifier.testTag("season-card"),
        containerColor = scheme.primaryContainer,
        border = BorderStroke(Sizes.border, BetterHunterTheme.extendedColors.warmBorder),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onCheck).padding(start = Spacing.l, top = Spacing.m, bottom = Spacing.m, end = Spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(Icons.Outlined.Event, containerColor = scheme.tertiaryContainer, contentColor = scheme.tertiary)
            Spacer(Modifier.width(Spacing.m))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                val (title, sub) = when (season) {
                    is SeasonStatus.Countdown -> countdownTitle(season.daysLeft) to
                        stringResource(R.string.season_date_sub, shortDate(season.start), size)
                    SeasonStatus.Coming -> stringResource(R.string.season_coming) to stringResource(R.string.season_coming_sub, size)
                }
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(sub, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = scheme.onSurfaceVariant)
        }
        if (season == SeasonStatus.Coming) {
            HorizontalDivider(color = BetterHunterTheme.extendedColors.warmBorder)
            LinkButton(onClick = onSetDate, modifier = Modifier.padding(horizontal = Spacing.s).testTag("season-set-date")) {
                Icon(Icons.Outlined.Event, contentDescription = null, modifier = Modifier.size(Sizes.iconS))
                Spacer(Modifier.width(Spacing.s))
                Text(stringResource(R.string.season_set_date))
            }
        }
    }
}

/** "Your season starts in 3 weeks / in 5 days / tomorrow / today". */
@Composable
fun countdownTitle(daysLeft: Int): String = when {
    daysLeft <= 0 -> stringResource(R.string.season_today)
    daysLeft == 1 -> stringResource(R.string.season_tomorrow)
    daysLeft >= 14 -> (daysLeft / 7).let { pluralStringResource(R.plurals.season_in_weeks, it, it) }
    else -> pluralStringResource(R.plurals.season_in_days, daysLeft, daysLeft)
}

/** Today's 6 questions: 3 species and 3 general, each with its progress, and one button to go on. */
@Composable
internal fun DailyCard(daily: DailyState, streak: Int, onStart: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    BhCard(Modifier.testTag("daily-card")) {
        Column(Modifier.padding(Sizes.cardPadding), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Overline(stringResource(R.string.today))
                Text(
                    stringResource(R.string.daily_title, daily.speciesTotal + daily.generalTotal),
                    style = MaterialTheme.typography.titleLarge,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (streak > 0) {
                        Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = BetterHunterTheme.extendedColors.streak, modifier = Modifier.size(Sizes.iconS))
                        Spacer(Modifier.width(Spacing.xs))
                    }
                    Text(
                        listOfNotNull(
                            pluralStringResource(R.plurals.streak, streak, streak).takeIf { streak > 0 },
                            stringResource(R.string.daily_every_day),
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                    )
                }
            }
            Column {
                DailyRow(
                    Icons.Outlined.Visibility, orange = true,
                    title = stringResource(R.string.daily_species, daily.speciesTotal),
                    sub = stringResource(R.string.daily_species_sub),
                    done = daily.speciesDone, total = daily.speciesTotal, onClick = onStart,
                    modifier = Modifier.testTag("daily-species"),
                )
                HorizontalDivider(color = scheme.outlineVariant)
                DailyRow(
                    Icons.AutoMirrored.Outlined.MenuBook, orange = false,
                    title = stringResource(R.string.daily_general, daily.generalTotal),
                    sub = stringResource(R.string.daily_general_sub),
                    done = daily.generalDone, total = daily.generalTotal, onClick = onStart,
                    modifier = Modifier.testTag("daily-general"),
                )
            }
            if (daily.allDone) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.testTag("daily-all-done")) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = BetterHunterTheme.extendedColors.success, modifier = Modifier.size(Sizes.iconM))
                    Spacer(Modifier.width(Spacing.s))
                    Text(stringResource(R.string.daily_all_done), style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                val speciesLeft = daily.speciesTotal - daily.speciesDone
                val generalLeft = daily.generalTotal - daily.generalDone
                PrimaryButton(
                    when {
                        daily.speciesDone + daily.generalDone == 0 -> stringResource(R.string.daily_start)
                        speciesLeft > 0 -> stringResource(R.string.daily_continue_species, speciesLeft)
                        else -> stringResource(R.string.daily_continue_general, generalLeft)
                    },
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth().testTag("daily-continue"),
                    icon = Icons.Rounded.PlayArrow,
                )
            }
        }
    }
}

@Composable
private fun DailyRow(
    icon: ImageVector,
    orange: Boolean,
    title: String,
    sub: String,
    done: Int,
    total: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val success = BetterHunterTheme.extendedColors.success
    Row(modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = Spacing.m), verticalAlignment = Alignment.CenterVertically) {
        IconBadge(
            icon,
            containerColor = if (orange) scheme.tertiaryContainer else scheme.secondaryContainer,
            contentColor = if (orange) scheme.tertiary else scheme.secondary,
        )
        Spacer(Modifier.width(Spacing.m))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                repeat(total) { i ->
                    Box(Modifier.size(Spacing.s).background(if (i < done) success else scheme.surfaceContainerHighest, CircleShape))
                }
            }
            Text("$done / $total", style = MaterialTheme.typography.labelMedium, color = if (done > 0) success else scheme.onSurfaceVariant)
        }
    }
}

/** New hunters: "What is it?" and Field skills side by side, each with its readiness. */
@Composable
internal fun FieldHubs(state: HomeUiState, onOpenRecognition: () -> Unit, onOpenFieldSkills: () -> Unit) {
    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
        val tile = Modifier.weight(1f).fillMaxHeight()
        if (state.gameCount > 0) RecognitionHub(state, onOpenRecognition, tile)
        HubCard(
            icon = Icons.Outlined.GpsFixed,
            title = stringResource(R.string.hub_skills),
            subtitle = stringResource(R.string.hub_skills_sub),
            tone = HubTone.Green,
            onClick = onOpenFieldSkills,
            modifier = tile.testTag("hub-skills"),
        ) {
            Readiness(state.skillsReadiness)
        }
    }
}

