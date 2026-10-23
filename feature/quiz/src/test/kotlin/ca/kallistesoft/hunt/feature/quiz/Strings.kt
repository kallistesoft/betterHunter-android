package ca.kallistesoft.hunt.feature.quiz

import org.robolectric.RuntimeEnvironment

/** A string of the app, as the UI under test shows it (Robolectric's locale). */
internal fun string(id: Int, vararg args: Any): String = RuntimeEnvironment.getApplication().getString(id, *args)

internal fun plural(id: Int, count: Int, vararg args: Any): String =
    RuntimeEnvironment.getApplication().resources.getQuantityString(id, count, *args)
