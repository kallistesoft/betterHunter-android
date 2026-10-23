package ca.kallistesoft.hunt.feature.quiz

import android.view.KeyEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.withKeyDown
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes
import ca.kallistesoft.hunt.core.testing.ScreenshotDevice
import ca.kallistesoft.hunt.core.ui.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** The question's keyboard: what each key does, through the modifier the quiz screens use. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [ScreenshotDevice.SDK], qualifiers = ScreenshotDevice.LIGHT_EN)
class QuizShortcutsModifierTest {
    @get:Rule
    val compose = createComposeRule()

    private val events = mutableListOf<String>()

    /** Whether an answer is chosen, so Enter can go on. */
    private var canGoNext by mutableStateOf(false)

    private fun show(choiceCount: Int = 3) = compose.setContent {
        val focus = remember { FocusRequester() }
        Box(
            Modifier.size(Sizes.touchTarget).testTag("question")
                .quizShortcuts(focus, choiceCount, canGoNext, onChoose = { events += "choose $it" }, onNext = { events += "next" }),
        )
        LaunchedEffect(Unit) { focus.requestFocus() }
    }

    @Test
    fun `letters and numbers choose an answer the question has`() {
        show(choiceCount = 3)
        compose.onNodeWithTag("question").performKeyInput {
            pressKey(Key.B)
            pressKey(Key.Three)
            pressKey(Key.D) // only three answers
        }
        assertEquals(listOf("choose 1", "choose 2"), events)
    }

    @Test
    fun `Enter goes on only once an answer is chosen`() {
        show()
        compose.onNodeWithTag("question").performKeyInput { pressKey(Key.Enter) }
        assertEquals(emptyList<String>(), events)

        canGoNext = true
        compose.onNodeWithTag("question").performKeyInput {
            pressKey(Key.Enter)
            pressKey(Key.NumPadEnter)
        }
        assertEquals(listOf("next", "next"), events)
    }

    @Test
    fun `combinations and other keys are left alone`() {
        canGoNext = true
        show()
        compose.onNodeWithTag("question").performKeyInput {
            withKeyDown(Key.CtrlLeft) { pressKey(Key.A) }
            withKeyDown(Key.AltLeft) { pressKey(Key.Enter) }
            withKeyDown(Key.MetaLeft) { pressKey(Key.One) }
            pressKey(Key.E)
        }
        assertEquals(emptyList<String>(), events)
    }

    @Test
    fun `the shortcut helper lists every answer key and Enter`() {
        val context = RuntimeEnvironment.getApplication()
        val group = quizShortcutGroup(context)
        assertEquals(context.getString(R.string.shortcuts_quiz), group.label)
        val keys = group.items.map { it.keycode }
        assertEquals(
            listOf(
                KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_B, KeyEvent.KEYCODE_2,
                KeyEvent.KEYCODE_C, KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_ENTER,
            ),
            keys,
        )
        assertEquals(context.getString(R.string.shortcut_answer, "C"), group.items[4].label)
    }
}
