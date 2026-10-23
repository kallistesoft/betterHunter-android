package ca.kallistesoft.hunt.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import ca.kallistesoft.hunt.core.model.Lang
import ca.kallistesoft.hunt.core.model.Question

/** Content language for the app's current locale (set per app with AppCompat, or the device's). */
@Composable
fun currentLang(): Lang = Lang.forLanguage(LocalConfiguration.current.locales[0]?.language)

/** The other content language: names are shown in both on "What is it?" answers. */
val Lang.other: Lang get() = if (this == Lang.FR) Lang.EN else Lang.FR

/** Choice [index] of this question in the other language, when it is written differently. */
fun Question.otherLanguageChoice(lang: Lang, index: Int): String? {
    val here = texts[lang]?.choices?.getOrNull(index)
    return texts[lang.other]?.choices?.getOrNull(index)?.takeIf { it != here }
}
