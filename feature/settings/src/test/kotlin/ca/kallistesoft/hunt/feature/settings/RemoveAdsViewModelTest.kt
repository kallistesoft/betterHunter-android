package ca.kallistesoft.hunt.feature.settings

import android.app.Activity
import app.cash.turbine.test
import ca.kallistesoft.hunt.core.billing.AdFreeProduct
import ca.kallistesoft.hunt.core.billing.BuyResult
import ca.kallistesoft.hunt.core.data.Now
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.testing.FakePurchases
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.MainDispatcherRule
import ca.kallistesoft.hunt.core.testing.TEST_NOW
import ca.kallistesoft.hunt.core.testing.awaitItemMatching
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RemoveAdsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val activity = mockk<Activity>()
    private val userData = FakeUserDataRepository()
    private val purchases = FakePurchases(userData)

    private fun viewModel() = RemoveAdsViewModel(userData, purchases, Now { TEST_NOW })

    @Test
    fun `Play's prices, then a week bought shows its last day`() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            val start = awaitItemMatching { !it.loading }
            assertEquals("CA$14.99", start.prices[AdFreeProduct.FOREVER])
            assertNull(start.untilDay)
            vm.buy(activity, AdFreeProduct.WEEK)
            val bought = awaitItemMatching { it.untilDay != null && !it.busy }
            assertEquals(epochDay(TEST_NOW + UserProgress.WEEK_PASS_MILLIS), bought.untilDay)
            assertFalse(bought.forever)
            assertNull(bought.message)
        }
    }

    @Test
    fun `forever bought, and a second tap while Play's sheet is open is ignored`() = runTest {
        val vm = viewModel()
        purchases.onBuy = CompletableDeferred()
        vm.uiState.test {
            awaitItemMatching { !it.loading }
            vm.buy(activity, AdFreeProduct.FOREVER)
            assertTrue(awaitItemMatching { it.busy }.busy)
            vm.buy(activity, AdFreeProduct.WEEK)
            purchases.onBuy?.complete(Unit)
            assertTrue(awaitItemMatching { it.forever && !it.busy }.forever)
        }
        assertEquals(listOf(AdFreeProduct.FOREVER), purchases.bought)
    }

    @Test
    fun `pending, unavailable, failed and cancelled purchases`() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            awaitItemMatching { !it.loading }
            for ((result, message) in listOf(
                BuyResult.PENDING to RemoveAdsMessage.PENDING,
                BuyResult.UNAVAILABLE to RemoveAdsMessage.UNAVAILABLE,
                BuyResult.FAILED to RemoveAdsMessage.FAILED,
            )) {
                purchases.result = result
                vm.buy(activity, AdFreeProduct.WEEK)
                assertEquals(message, awaitItemMatching { it.message == message }.message)
                vm.dismissMessage()
                assertNull(awaitItemMatching { it.message == null }.message)
            }
            purchases.result = BuyResult.CANCELLED
            vm.buy(activity, AdFreeProduct.WEEK)
            val cancelled = awaitItemMatching { !it.busy }
            assertNull("closing Play's sheet says nothing", cancelled.message)
        }
    }

    @Test
    fun `restore says whether Play answered`() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            awaitItemMatching { !it.loading }
            vm.restore()
            assertEquals(RemoveAdsMessage.RESTORED, awaitItemMatching { it.message != null }.message)
            purchases.restores = false
            vm.restore()
            assertEquals(RemoveAdsMessage.RESTORE_FAILED, awaitItemMatching { it.message == RemoveAdsMessage.RESTORE_FAILED }.message)
        }
    }

    @Test
    fun `a week that's over isn't shown`() = runTest {
        userData.addWeekPass("old", TEST_NOW - UserProgress.WEEK_PASS_MILLIS - 1)
        viewModel().uiState.test { assertNull(awaitItemMatching { !it.loading }.untilDay) }
    }
}
