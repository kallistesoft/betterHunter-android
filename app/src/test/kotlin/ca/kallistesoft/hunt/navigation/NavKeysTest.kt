package ca.kallistesoft.hunt.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Test

class NavKeysTest {
    @Test
    fun `a double tap opens a screen once`() {
        val backStack = NavBackStack<NavKey>(Home)
        backStack.push(Settings)
        backStack.push(Settings)
        assertEquals(listOf(Home, Settings), backStack.toList())

        backStack.push(About)
        backStack.push(Settings) // not on top: opened again
        assertEquals(listOf(Home, Settings, About, Settings), backStack.toList())
    }
}
