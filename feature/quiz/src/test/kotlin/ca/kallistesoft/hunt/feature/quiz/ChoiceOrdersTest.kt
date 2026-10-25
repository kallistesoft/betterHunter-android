package ca.kallistesoft.hunt.feature.quiz

import ca.kallistesoft.hunt.core.testing.TestContent
import org.junit.Assert.assertEquals
import org.junit.Test

class ChoiceOrdersTest {
    private val bank = listOf(TestContent.question("q1", "federal"), TestContent.question("q2", "federal"))

    @Test
    fun `the order shown is saved and put back, the bank's order saved as empty`() {
        val shown = listOf(bank[0].withChoiceOrder(listOf(3, 2, 1, 0)), bank[1])
        val saved = shown.savedChoiceOrders()
        assertEquals(listOf("3,2,1,0", ""), saved)
        assertEquals(shown, bank.withSavedChoiceOrders(saved))
    }

    @Test
    fun `nothing saved, or saved for other questions, keeps the bank's order`() {
        assertEquals(bank, bank.withSavedChoiceOrders(null))
        assertEquals(bank, bank.withSavedChoiceOrders(listOf("1,0,2,3")))
        assertEquals("an unreadable order is ignored", bank, bank.withSavedChoiceOrders(listOf("x", "")))
    }
}
