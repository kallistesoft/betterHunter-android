package ca.kallistesoft.hunt.feature.exams

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.IconBadge
import ca.kallistesoft.hunt.core.designsystem.component.LinkButton
import ca.kallistesoft.hunt.core.designsystem.component.MeterBar
import ca.kallistesoft.hunt.core.designsystem.component.Overline
import ca.kallistesoft.hunt.core.designsystem.component.PrimaryButton
import ca.kallistesoft.hunt.core.designsystem.component.SectionTitle
import ca.kallistesoft.hunt.core.designsystem.component.plus
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.FieldRun
import ca.kallistesoft.hunt.core.model.FieldSkill
import ca.kallistesoft.hunt.core.model.SkillSummary
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.R
import ca.kallistesoft.hunt.core.ui.icon
import ca.kallistesoft.hunt.core.ui.nameRes
import ca.kallistesoft.hunt.core.ui.subRes
import kotlin.math.roundToInt

/**
 * "Field skills" (new hunters, P11): today's mix ("Keep your edge" once every skill is mastered), then one row per
 * skill with its readiness, "Mastered", or "Was mastered · 2 answers expired".
 */
@Composable
fun FieldSkillsScreen(
    onBack: () -> Unit,
    onStart: (run: FieldRun, skill: FieldSkill?) -> Unit,
    viewModel: FieldSkillsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FieldSkillsContent(state, onBack, onStart)
}

@Composable
internal fun FieldSkillsContent(
    state: FieldSkillsUiState,
    onBack: () -> Unit,
    onStart: (run: FieldRun, skill: FieldSkill?) -> Unit,
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Scaffold(topBar = { BackTopAppBar(stringResource(R.string.hub_skills), onBack) }) { padding ->
        if (state.loading) return@Scaffold
        LazyColumn(contentPadding = padding.plus(top = Spacing.s), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            item {
                Text(
                    stringResource(if (state.allMastered) R.string.skills_all_mastered_sub else R.string.skills_sub),
                    color = muted,
                    modifier = Modifier.padding(bottom = Spacing.s),
                )
            }
            item { MixCard(state) { onStart(FieldRun.MIX, null) } }
            item {
                SectionTitle(stringResource(R.string.skills)) {
                    LinkButton(onClick = { onStart(FieldRun.MISTAKES, null) }, modifier = Modifier.testTag("skills-mistakes")) {
                        Text(stringResource(R.string.my_mistakes_count, state.mistakes))
                    }
                }
            }
            items(state.skills, key = { it.skill.name }) { summary ->
                SkillRow(summary, onClick = { onStart(FieldRun.SKILL, summary.skill) })
            }
            item {
                Row(Modifier.padding(top = Spacing.s), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = muted, modifier = Modifier.size(Sizes.iconS))
                    Spacer(Modifier.width(Spacing.s))
                    Text(stringResource(R.string.fade_note), style = MaterialTheme.typography.bodySmall, color = muted)
                }
            }
        }
    }
}

/** Today's mix, or "Keep your edge" once every skill is mastered. */
@Composable
private fun MixCard(state: FieldSkillsUiState, onStart: () -> Unit) {
    val edge = state.allMastered
    BhCard(
        Modifier.testTag("mix-card"),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(Sizes.border, BetterHunterTheme.extendedColors.warmBorder),
    ) {
        Column(Modifier.padding(Sizes.cardPadding), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Outlined.Bolt, containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.width(Spacing.m))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                    Text(stringResource(if (edge) R.string.keep_edge else R.string.todays_mix), style = MaterialTheme.typography.titleMedium)
                    val size = FieldRun.MIX.size
                    Text(
                        when {
                            edge -> stringResource(R.string.keep_edge_sub, size)
                            state.keptInMix > 0 -> stringResource(R.string.mix_sub_kept, size - state.keptInMix, state.keptInMix)
                            else -> stringResource(R.string.mix_sub, size)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            PrimaryButton(
                stringResource(if (edge) R.string.start else R.string.start_mix),
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().testTag("start-mix"),
                icon = Icons.Rounded.PlayArrow,
            )
        }
    }
}

@Composable
private fun SkillRow(summary: SkillSummary, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val success = BetterHunterTheme.extendedColors.success
    BhCard(Modifier.testTag("skill-${summary.skill.name}"), shape = MaterialTheme.shapes.medium, onClick = onClick) {
        Row(Modifier.padding(Spacing.l), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(summary.skill.icon)
            Spacer(Modifier.width(Spacing.m))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(summary.skill.nameRes), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    if (summary.mastered) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = success, modifier = Modifier.size(Sizes.iconS))
                        Spacer(Modifier.width(Spacing.xs))
                        Overline(stringResource(R.string.mastered), color = success)
                    } else {
                        Text(
                            stringResource(R.string.percent, (summary.readiness * 100).roundToInt()),
                            style = MaterialTheme.typography.labelLarge,
                            color = success,
                        )
                    }
                }
                Text(
                    if (summary.faded) {
                        pluralStringResource(R.plurals.was_mastered, summary.expired, summary.expired)
                    } else {
                        stringResource(summary.skill.subRes)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (summary.faded) scheme.tertiary else scheme.onSurfaceVariant,
                )
                if (!summary.mastered) MeterBar(summary.readiness)
            }
        }
    }
}
