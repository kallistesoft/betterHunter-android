package ca.kallistesoft.hunt.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.BottomActionBar
import ca.kallistesoft.hunt.core.designsystem.component.IconBadge
import ca.kallistesoft.hunt.core.designsystem.component.LinkButton
import ca.kallistesoft.hunt.core.designsystem.component.Overline
import ca.kallistesoft.hunt.core.designsystem.component.PrimaryButton
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.longDate
import ca.kallistesoft.hunt.core.ui.rememberNotificationPermission
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

/** "When does your season start?" from Home's season card or Settings; then, once saved, "Remind me before my season?". */
@Composable
fun SeasonDateScreen(onDone: () -> Unit, viewModel: SeasonDateViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.done) { if (state.done) onDone() }
    val askPermission = rememberNotificationPermission(viewModel::answerReminders)
    SeasonDateContent(
        state,
        onBack = onDone,
        onPick = viewModel::pick,
        onSave = viewModel::save,
        onDontKnow = viewModel::dontKnow,
        onRemind = askPermission,
        onNoThanks = { viewModel.answerReminders(false) },
    )
}

@Composable
internal fun SeasonDateContent(
    state: SeasonDateUiState,
    onBack: () -> Unit,
    onPick: (Long) -> Unit,
    onSave: () -> Unit,
    onDontKnow: () -> Unit,
    onRemind: () -> Unit,
    onNoThanks: () -> Unit,
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Scaffold(
        topBar = { BackTopAppBar(stringResource(R.string.season_start), onBack) },
        bottomBar = {
            if (!state.loading) {
                if (state.askReminders) {
                    RemindCard(state.picked, onRemind, onNoThanks)
                } else {
                    BottomActionBar {
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            val picked = state.picked
                            PrimaryButton(
                                if (picked != null) stringResource(R.string.save_date, longDate(picked)) else stringResource(R.string.pick_a_date),
                                onClick = onSave,
                                enabled = picked != null,
                                modifier = Modifier.fillMaxWidth().testTag("season-save"),
                            )
                            LinkButton(onClick = onDontKnow, modifier = Modifier.testTag("season-dont-know")) {
                                Text(stringResource(R.string.dont_know_yet))
                            }
                        }
                    }
                }
            }
        },
    ) { padding ->
        if (state.loading) return@Scaffold
        LazyColumn(contentPadding = padding.plus(top = Spacing.s), verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                    Text(stringResource(R.string.season_date_title), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(R.string.season_date_body), color = muted)
                }
            }
            item { Calendar(state, onPick) }
            item {
                Row {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = muted, modifier = Modifier.size(Sizes.iconS))
                    Spacer(Modifier.width(Spacing.s))
                    Text(stringResource(R.string.season_date_note), style = MaterialTheme.typography.bodySmall, color = muted)
                }
            }
        }
    }
}

/** A month at a time, weeks starting on the locale's first day (Sunday in Canada); days before today are off. */
@Composable
private fun Calendar(state: SeasonDateUiState, onPick: (Long) -> Unit) {
    val locale = Locale.getDefault()
    val today = LocalDate.ofEpochDay(state.today)
    val last = today.plusDays(SeasonDateViewModel.MAX_DAYS_AHEAD)
    val start = state.picked?.let(LocalDate::ofEpochDay) ?: today
    var shown by rememberSaveable { mutableLongStateOf(YearMonth.from(start).atDay(1).toEpochDay()) }
    val month = YearMonth.from(LocalDate.ofEpochDay(shown))
    val firstDay = WeekFields.of(locale).firstDayOfWeek
    val lead = (month.atDay(1).dayOfWeek.value - firstDay.value + 7) % 7
    val scheme = MaterialTheme.colorScheme
    BhCard(Modifier.testTag("calendar")) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { shown = month.minusMonths(1).atDay(1).toEpochDay() },
                    enabled = month > YearMonth.from(today),
                    modifier = Modifier.testTag("calendar-prev"),
                ) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = stringResource(R.string.previous_month)) }
                Text(
                    month.atDay(1).format(DateTimeFormatter.ofPattern("LLLL yyyy", locale)).replaceFirstChar { it.titlecase(locale) },
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { shown = month.plusMonths(1).atDay(1).toEpochDay() },
                    enabled = month < YearMonth.from(last),
                    modifier = Modifier.testTag("calendar-next"),
                ) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = stringResource(R.string.next_month)) }
            }
            Row {
                (0 until 7).forEach { i ->
                    Text(
                        firstDay.plus(i.toLong()).getDisplayName(TextStyle.NARROW, locale),
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            (0 until (lead + month.lengthOfMonth() + 6) / 7).forEach { week ->
                Row {
                    (0 until 7).forEach { col ->
                        val n = week * 7 + col - lead + 1
                        Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                            if (n in 1..month.lengthOfMonth()) {
                                val date = month.atDay(n)
                                val day = date.toEpochDay()
                                val enabled = date >= today && date <= last
                                val selected = day == state.picked
                                val label = longDate(day)
                                Box(
                                    Modifier.size(Sizes.touchTarget).clip(CircleShape)
                                        .background(if (selected) scheme.primary else androidx.compose.ui.graphics.Color.Transparent)
                                        .clickable(enabled = enabled, role = Role.Button) { onPick(day) }
                                        .semantics { contentDescription = label; this.selected = selected }
                                        .testTag("day-$day"),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "$n",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selected || date == today) FontWeight.SemiBold else FontWeight.Normal,
                                        color = when {
                                            selected -> scheme.onPrimary
                                            !enabled -> scheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            else -> scheme.onSurface
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** "Saved: Saturday, November 14 · Remind me before my season?" */
@Composable
private fun RemindCard(saved: Long?, onRemind: () -> Unit, onNoThanks: () -> Unit) {
    BhCard(
        Modifier.fillMaxWidth().padding(Spacing.l).testTag("season-remind"),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(Sizes.border, BetterHunterTheme.extendedColors.warmBorder),
    ) {
        Column(Modifier.padding(Sizes.cardPadding), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            saved?.let { Overline(stringResource(R.string.saved_date, longDate(it)), color = MaterialTheme.colorScheme.secondary) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Outlined.NotificationsActive, containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.width(Spacing.m))
                Text(stringResource(R.string.remind_title), style = MaterialTheme.typography.titleMedium)
            }
            Text(stringResource(R.string.remind_body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            PrimaryButton(stringResource(R.string.remind_me), onClick = onRemind, modifier = Modifier.fillMaxWidth().testTag("season-remind-me"))
            LinkButton(onClick = onNoThanks, modifier = Modifier.align(Alignment.CenterHorizontally).testTag("season-no-thanks")) {
                Text(stringResource(R.string.no_thanks))
            }
        }
    }
}
