package ca.kallistesoft.hunt.core.billing

import android.content.Context
import ca.kallistesoft.hunt.core.billing.di.BillingModule
import ca.kallistesoft.hunt.core.data.Now
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.TEST_NOW
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class EntitlementsTest {
    private val userData = FakeUserDataRepository()
    private val now = Now { TEST_NOW }

    @Test
    fun `no ads is held with forever or a running week, and the ads follow it`() = runTest {
        assertEquals(emptySet<Entitlement>(), UserProgress().entitlements(TEST_NOW))
        assertEquals(setOf(Entitlement.NO_ADS), UserProgress().withAdFreeForever(true).entitlements(TEST_NOW))

        val adFree = StoreAdFree(userData, now)
        assertFalse(adFree.isAdFree())
        userData.addWeekPass("w", TEST_NOW - 1)
        assertTrue(adFree.isAdFree())
        assertFalse("over after 7 days", StoreAdFree(userData, Now { TEST_NOW + UserProgress.WEEK_PASS_MILLIS }).isAdFree())
    }

    @Test
    fun `products are found by their Play id`() {
        assertEquals(AdFreeProduct.WEEK, AdFreeProduct.of("remove_ads_week"))
        assertEquals(AdFreeProduct.FOREVER, AdFreeProduct.of("remove_ads_forever"))
        assertNull(AdFreeProduct.of("pro"))
    }

    @Test
    fun `debug purchases give the entitlement at once`() = runTest {
        val purchases = TestPurchases(userData, now)
        purchases.refresh()
        assertEquals("CA$1.99", purchases.prices.value[AdFreeProduct.WEEK])
        assertEquals(BuyResult.BOUGHT, purchases.buy(mockk(), AdFreeProduct.WEEK))
        assertEquals(TEST_NOW + UserProgress.WEEK_PASS_MILLIS, userData.progress.value.adFreeUntil)
        assertEquals(BuyResult.BOUGHT, purchases.buy(mockk(), AdFreeProduct.FOREVER))
        assertTrue(userData.progress.value.adFreeForever)
        assertTrue(purchases.restore())
    }

    @Test
    fun `the module builds both stores and the ad switch`() = runTest {
        val context: Context = RuntimeEnvironment.getApplication()
        assertTrue(BillingModule.playPurchases(context, userData, now) is PlayPurchases)
        assertTrue(BillingModule.testPurchases(userData, now) is TestPurchases)
        assertFalse(BillingModule.adFree(userData, now).isAdFree())
    }
}
