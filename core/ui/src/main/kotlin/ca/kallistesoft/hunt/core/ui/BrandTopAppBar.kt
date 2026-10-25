package ca.kallistesoft.hunt.core.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ca.kallistesoft.hunt.core.designsystem.component.BuckLogo
import ca.kallistesoft.hunt.core.designsystem.component.PillToggle
import ca.kallistesoft.hunt.core.designsystem.theme.BhTextStyles
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.model.Lang

/** Top bar of onboarding's first screen: the logo and name, the FR/EN toggle (Settings isn't reachable yet), then [actions]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandTopAppBar(onSetLanguage: (Lang) -> Unit, actions: @Composable RowScope.() -> Unit = {}) =
    BrandTopAppBarImpl(onSetLanguage, scrollBehavior = null, actions)

/** Home's top bar: the logo and name, then [actions], hiding as the list scrolls; the language is in Settings. */
@ExperimentalMaterial3Api
@Composable
fun BrandTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    actions: @Composable RowScope.() -> Unit = {},
) = BrandTopAppBarImpl(onSetLanguage = null, scrollBehavior, actions)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrandTopAppBarImpl(
    onSetLanguage: ((Lang) -> Unit)?,
    scrollBehavior: TopAppBarScrollBehavior?,
    actions: @Composable RowScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BuckLogo()
                Spacer(Modifier.width(Spacing.s + Spacing.xxs))
                Text(stringResource(R.string.app_title), style = BhTextStyles.brand)
            }
        },
        actions = {
            if (onSetLanguage != null) LanguageToggle(currentLang(), onSetLanguage)
            actions()
        },
        // Clear of a display cutout too (landscape), not only the status bar.
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = scheme.background,
            scrolledContainerColor = scheme.background,
            actionIconContentColor = scheme.onSurfaceVariant,
        ),
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun LanguageToggle(lang: Lang, onSetLanguage: (Lang) -> Unit) {
    PillToggle(
        options = Lang.entries.map { it.code.uppercase() },
        selected = Lang.entries.indexOf(lang),
        onSelect = { onSetLanguage(Lang.entries[it]) },
        description = stringResource(R.string.language),
        modifier = Modifier.testTag("lang-toggle"),
    )
}
