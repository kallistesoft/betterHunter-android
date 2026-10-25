package ca.kallistesoft.hunt.core.billing

import android.app.Activity
import ca.kallistesoft.hunt.core.data.Now
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.testing.FakeUserDataRepository
import ca.kallistesoft.hunt.core.testing.TEST_NOW
import com.android.billingclient.api.BillingClient.BillingResponseCode
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The purchase rules (P15) on a fake Play: what each product gives, once, and what a refund or a failure does. */
@OptIn(ExperimentalCoroutinesApi::class)
class PlayPurchasesTest {
    private val activity = mockk<Activity>()
    private val userData = FakeUserDataRepository()
    private val play = FakeGateway()

    private fun TestScope.purchases() = PlayPurchases(play, userData, Now { TEST_NOW }, backgroundScope)

    private fun bought(id: String, token: String, state: StorePurchase.State = StorePurchase.State.PURCHASED, acknowledged: Boolean = false) =
        StorePurchase(token, listOf(id), state, acknowledged)

    private val progress: UserProgress get() = userData.progress.value

    /** Opens the sheet, then Play answers [update] as the sheet closes. */
    private suspend fun TestScope.buy(purchases: PlayPurchases, product: AdFreeProduct, update: PurchaseUpdate): BuyResult {
        val result = async { purchases.buy(activity, product) }
        runCurrent()
        play.updates.emit(update)
        runCurrent()
        return result.await()
    }

    @Test
    fun `refresh loads Play's prices and applies what the account owns`() = runTest {
        play.owned = listOf(bought(AdFreeProduct.FOREVER.productId, "f1"))
        val purchases = purchases()
        purchases.refresh()
        runCurrent()
        assertEquals(mapOf(AdFreeProduct.WEEK to "CA$1.99", AdFreeProduct.FOREVER to "CA$14.99"), purchases.prices.value)
        assertTrue(progress.adFreeForever)
        assertEquals("forever is acknowledged", listOf("f1"), play.acknowledged)
    }

    @Test
    fun `nothing changes when Play can't be reached`() = runTest {
        userData.progress.value = UserProgress().withAdFreeForever(true)
        play.connects = false
        val purchases = purchases()
        purchases.refresh()
        runCurrent()
        assertTrue(purchases.prices.value.isEmpty())
        assertTrue("kept offline", progress.adFreeForever)
        assertEquals(BuyResult.UNAVAILABLE, purchases.buy(activity, AdFreeProduct.WEEK))
        assertFalse(purchases.restore())
    }

    @Test
    fun `a week bought is counted, then consumed so it can be bought again`() = runTest {
        val purchases = purchases()
        val result = buy(purchases, AdFreeProduct.WEEK, PurchaseUpdate(BillingResponseCode.OK, listOf(bought(AdFreeProduct.WEEK.productId, "w1"))))
        assertEquals(BuyResult.BOUGHT, result)
        assertEquals(TEST_NOW + UserProgress.WEEK_PASS_MILLIS, progress.adFreeUntil)
        assertEquals(listOf("w1"), play.consumed)
        assertFalse("a week isn't forever", progress.adFreeForever)
        assertEquals(listOf(AdFreeProduct.WEEK.productId), play.launched)
    }

    @Test
    fun `a week whose consume failed is retried at the next launch, but counted once`() = runTest {
        play.consumes = false
        val purchases = purchases()
        buy(purchases, AdFreeProduct.WEEK, PurchaseUpdate(BillingResponseCode.OK, listOf(bought(AdFreeProduct.WEEK.productId, "w1"))))
        play.owned = listOf(bought(AdFreeProduct.WEEK.productId, "w1"))
        play.consumes = true
        purchases.refresh()
        runCurrent()
        assertEquals(TEST_NOW + UserProgress.WEEK_PASS_MILLIS, progress.adFreeUntil)
        assertEquals(listOf("w1", "w1"), play.consumed)
    }

    @Test
    fun `forever bought is acknowledged once, and a refund takes it back`() = runTest {
        val purchases = purchases()
        val result = buy(
            purchases,
            AdFreeProduct.FOREVER,
            PurchaseUpdate(BillingResponseCode.OK, listOf(bought(AdFreeProduct.FOREVER.productId, "f1"))),
        )
        assertEquals(BuyResult.BOUGHT, result)
        assertTrue(progress.adFreeForever)
        assertEquals(listOf("f1"), play.acknowledged)

        play.owned = listOf(bought(AdFreeProduct.FOREVER.productId, "f1", acknowledged = true))
        assertTrue(purchases.restore())
        assertEquals("not acknowledged twice", listOf("f1"), play.acknowledged)

        play.owned = emptyList() // refunded
        purchases.refresh()
        runCurrent()
        assertFalse(progress.adFreeForever)
    }

    @Test
    fun `a pending payment counts once Play reports it paid`() = runTest {
        val purchases = purchases()
        val pending = buy(
            purchases,
            AdFreeProduct.FOREVER,
            PurchaseUpdate(BillingResponseCode.OK, listOf(bought(AdFreeProduct.FOREVER.productId, "f1", StorePurchase.State.PENDING))),
        )
        assertEquals(BuyResult.PENDING, pending)
        assertFalse(progress.adFreeForever)

        play.updates.emit(PurchaseUpdate(BillingResponseCode.OK, listOf(bought(AdFreeProduct.FOREVER.productId, "f1"))))
        runCurrent()
        assertTrue("paid while the app is open", progress.adFreeForever)
    }

    @Test
    fun `cancelled, already owned, unavailable and other answers`() = runTest {
        val purchases = purchases()
        assertEquals(BuyResult.CANCELLED, buy(purchases, AdFreeProduct.WEEK, PurchaseUpdate(BillingResponseCode.USER_CANCELED, emptyList())))
        assertNull(progress.adFreeUntil)

        play.owned = listOf(bought(AdFreeProduct.FOREVER.productId, "f1", acknowledged = true))
        assertEquals(
            "already owned: restored",
            BuyResult.BOUGHT,
            buy(purchases, AdFreeProduct.FOREVER, PurchaseUpdate(BillingResponseCode.ITEM_ALREADY_OWNED, emptyList())),
        )
        assertTrue(progress.adFreeForever)

        assertEquals(BuyResult.UNAVAILABLE, buy(purchases, AdFreeProduct.WEEK, PurchaseUpdate(BillingResponseCode.NETWORK_ERROR, emptyList())))
        assertEquals(BuyResult.FAILED, buy(purchases, AdFreeProduct.WEEK, PurchaseUpdate(BillingResponseCode.ERROR, emptyList())))
        assertEquals(
            "OK but nothing for this product",
            BuyResult.FAILED,
            buy(purchases, AdFreeProduct.WEEK, PurchaseUpdate(BillingResponseCode.OK, emptyList())),
        )
    }

    @Test
    fun `a sheet that can't open answers at once`() = runTest {
        val purchases = purchases()
        play.launchCode = BillingResponseCode.BILLING_UNAVAILABLE
        assertEquals(BuyResult.UNAVAILABLE, purchases.buy(activity, AdFreeProduct.WEEK))
        play.launchCode = BillingResponseCode.ITEM_ALREADY_OWNED
        play.owned = listOf(bought(AdFreeProduct.FOREVER.productId, "f1", acknowledged = true))
        assertEquals(BuyResult.BOUGHT, purchases.buy(activity, AdFreeProduct.FOREVER))
        play.products = emptyList()
        play.launchCode = BillingResponseCode.OK
        assertEquals("not set up in Play", BuyResult.UNAVAILABLE, PlayPurchases(play, userData, Now { TEST_NOW }, backgroundScope).buy(activity, AdFreeProduct.WEEK))
    }

    @Test
    fun `restore fails when Play doesn't answer`() = runTest {
        play.owned = null
        assertFalse(purchases().restore())
    }

    private class FakeGateway : BillingGateway {
        override val updates = MutableSharedFlow<PurchaseUpdate>()
        var connects = true
        var products: List<StoreProduct>? = listOf(
            StoreProduct(AdFreeProduct.WEEK.productId, "CA$1.99", null),
            StoreProduct(AdFreeProduct.FOREVER.productId, "CA$14.99", null),
            StoreProduct("something_else", "CA$0.99", null),
        )
        var owned: List<StorePurchase>? = emptyList()
        var launchCode = BillingResponseCode.OK
        var consumes = true
        val launched = mutableListOf<String>()
        val acknowledged = mutableListOf<String>()
        val consumed = mutableListOf<String>()

        override suspend fun connect() = connects

        override suspend fun products(ids: List<String>) = products

        override fun launch(activity: Activity, product: StoreProduct): Int {
            launched += product.id
            return launchCode
        }

        override suspend fun purchases() = owned

        override suspend fun acknowledge(token: String): Boolean {
            acknowledged += token
            return true
        }

        override suspend fun consume(token: String): Boolean {
            consumed += token
            return consumes
        }
    }
}
