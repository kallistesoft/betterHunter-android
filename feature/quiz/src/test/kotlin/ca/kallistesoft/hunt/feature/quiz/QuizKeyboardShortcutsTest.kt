package ca.kallistesoft.hunt.feature.quiz

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuizKeyboardShortcutsTest {
    @Test
    fun lettersAndNumbersChooseTheSameAnswer() {
        assertEquals(0, answerForKey(KeyEvent.KEYCODE_A))
        assertEquals(0, answerForKey(KeyEvent.KEYCODE_1))
        assertEquals(2, answerForKey(KeyEvent.KEYCODE_C))
        assertEquals(2, answerForKey(KeyEvent.KEYCODE_NUMPAD_3))
        assertEquals(3, answerForKey(KeyEvent.KEYCODE_4))
    }

    @Test
    fun otherKeysChooseNothing() {
        assertNull(answerForKey(KeyEvent.KEYCODE_E))
        assertNull(answerForKey(KeyEvent.KEYCODE_5))
        assertNull(answerForKey(KeyEvent.KEYCODE_ENTER))
    }
}
