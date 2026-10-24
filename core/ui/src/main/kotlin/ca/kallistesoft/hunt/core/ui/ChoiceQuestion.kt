package ca.kallistesoft.hunt.core.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import ca.kallistesoft.hunt.core.designsystem.component.ChoiceCard
import ca.kallistesoft.hunt.core.designsystem.component.ChoiceMode
import ca.kallistesoft.hunt.core.designsystem.component.Overline
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.Weapon

/** How a profile choice is shown: its icon, name and a line about it. */
data class ChoiceOption(val icon: ImageVector, @StringRes val title: Int, @StringRes val subtitle: Int)

fun HunterType.choiceOption(): ChoiceOption = when (this) {
    HunterType.WORKING_ON_IT -> ChoiceOption(Icons.AutoMirrored.Outlined.MenuBook, R.string.hunter_working, R.string.hunter_working_sub)
    HunterType.HUNTER -> ChoiceOption(HuntIcons.Antlers, R.string.hunter_hunter, R.string.hunter_hunter_sub)
}

fun Weapon.choiceOption(): ChoiceOption = when (this) {
    Weapon.FIREARM -> ChoiceOption(Icons.Outlined.GpsFixed, R.string.weapon_firearm, R.string.weapon_firearm_sub)
    Weapon.BOW -> ChoiceOption(HuntIcons.Bow, R.string.weapon_bow, R.string.weapon_bow_sub)
    Weapon.CROSSBOW -> ChoiceOption(HuntIcons.Crossbow, R.string.weapon_crossbow, R.string.weapon_crossbow_sub)
}

fun Province.choiceOption(): ChoiceOption = ChoiceOption(icon, nameRes, whereRes)

/**
 * One profile question (onboarding, and its edit from Settings): an optional [overline], the question, a hint, the
 * choices as [ChoiceCard]s and an optional [note] under them. [padding] is the Scaffold's.
 */
@Composable
fun <T : Enum<T>> ChoiceQuestion(
    padding: PaddingValues,
    @StringRes overline: Int?,
    @StringRes title: Int,
    @StringRes subtitle: Int,
    options: List<T>,
    selected: (T) -> Boolean,
    mode: ChoiceMode,
    onClick: (T) -> Unit,
    option: (T) -> ChoiceOption,
    @StringRes note: Int? = null,
) {
    LazyColumn(
        Modifier.fillMaxSize().consumeWindowInsets(padding),
        contentPadding = padding.plus(top = Spacing.s),
        verticalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        item {
            Column(Modifier.padding(bottom = Spacing.xs), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                if (overline != null) Overline(stringResource(overline))
                Text(
                    stringResource(title),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() },
                )
                Text(stringResource(subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Column(
                Modifier.then(if (mode == ChoiceMode.Single) Modifier.selectableGroup() else Modifier),
                verticalArrangement = Arrangement.spacedBy(Spacing.m),
            ) {
                options.forEach { value ->
                    val o = option(value)
                    ChoiceCard(
                        icon = o.icon,
                        title = stringResource(o.title),
                        subtitle = stringResource(o.subtitle),
                        selected = selected(value),
                        mode = mode,
                        onClick = { onClick(value) },
                        modifier = Modifier.fillMaxWidth().testTag("choice-${value.name}"),
                    )
                }
            }
        }
        if (note != null) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val muted = MaterialTheme.colorScheme.onSurfaceVariant
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = muted, modifier = Modifier.size(Sizes.iconS))
                    Spacer(Modifier.width(Spacing.s))
                    Text(stringResource(note), style = MaterialTheme.typography.bodySmall, color = muted)
                }
            }
        }
    }
}
