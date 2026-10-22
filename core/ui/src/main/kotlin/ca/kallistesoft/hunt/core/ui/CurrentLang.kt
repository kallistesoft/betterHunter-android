package ca.kallistesoft.hunt.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import ca.kallistesoft.hunt.core.model.Lang

/** Content language for the app's current locale (set per app with AppCompat, or the device's). */
@Composable
fun currentLang(): Lang = Lang.forLanguage(LocalConfiguration.current.locales[0]?.language)
