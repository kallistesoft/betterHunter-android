package ca.kallistesoft.hunt.feature.quiz

import android.content.Context
import android.view.KeyEvent
import android.view.KeyboardShortcutGroup
import android.view.KeyboardShortcutInfo
import androidx.compose.foundation.focusable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import ca.kallistesoft.hunt.core.ui.R

/** Letter keys choose the answer of the same letter, number keys the answer at that position (A–D, 1–4). */
private val AnswerKeys = listOf(
    KeyEvent.KEYCODE_A to KeyEvent.KEYCODE_1,
    KeyEvent.KEYCODE_B to KeyEvent.KEYCODE_2,
    KeyEvent.KEYCODE_C to KeyEvent.KEYCODE_3,
    KeyEvent.KEYCODE_D to KeyEvent.KEYCODE_4,
)
private val NumPadKeys = listOf(KeyEvent.KEYCODE_NUMPAD_1, KeyEvent.KEYCODE_NUMPAD_2, KeyEvent.KEYCODE_NUMPAD_3, KeyEvent.KEYCODE_NUMPAD_4)

/** The answer index a key code chooses, or null. */
internal fun answerForKey(keyCode: Int): Int? =
    AnswerKeys.indexOfFirst { (letter, digit) -> keyCode == letter || keyCode == digit }.takeIf { it >= 0 }
        ?: NumPadKeys.indexOf(keyCode).takeIf { it >= 0 }

/**
 * Keyboard shortcuts of a question (Chromebook, tablet with a keyboard): A–D or 1–4 choose an answer, Enter goes to the
 * next question once one is chosen. The screen takes focus through [focus] so the keys reach it (request it for each
 * question: its buttons are replaced). A focused button (Tab) still gets Enter first: this handler only sees the keys
 * its children leave.
 */
internal fun Modifier.quizShortcuts(
    focus: FocusRequester,
    choiceCount: Int,
    canGoNext: Boolean,
    onChoose: (Int) -> Unit,
    onNext: () -> Unit,
): Modifier = focusRequester(focus)
    .focusable()
    .onKeyEvent { event ->
        // Leave key releases, held keys and combinations (Ctrl+A, Alt+Tab…) alone.
        val plain = !event.isCtrlPressed && !event.isAltPressed && !event.isMetaPressed
        if (event.type != KeyEventType.KeyDown || event.nativeKeyEvent.repeatCount > 0 || !plain) return@onKeyEvent false
        val code = event.key.nativeKeyCode
        val answer = answerForKey(code)
        when {
            answer != null && answer < choiceCount -> {
                onChoose(answer)
                true
            }
            (code == KeyEvent.KEYCODE_ENTER || code == KeyEvent.KEYCODE_NUMPAD_ENTER) && canGoNext -> {
                onNext()
                true
            }
            else -> false
        }
    }

/** The same shortcuts for the system's Keyboard Shortcut Helper (Meta + /), from the activity. */
fun quizShortcutGroup(context: Context): KeyboardShortcutGroup {
    val letters = "ABCD"
    val answers = AnswerKeys.flatMapIndexed { i, (letter, digit) ->
        val label = context.getString(R.string.shortcut_answer, letters[i].toString())
        listOf(KeyboardShortcutInfo(label, letter, 0), KeyboardShortcutInfo(label, digit, 0))
    }
    val next = KeyboardShortcutInfo(context.getString(R.string.shortcut_next), KeyEvent.KEYCODE_ENTER, 0)
    return KeyboardShortcutGroup(context.getString(R.string.shortcuts_quiz), answers + next)
}
