package ca.kallistesoft.hunt.core.billing

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.QueryProductDetailsResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** [GoogleBillingGateway] turns the Play client's callbacks into results ([BillingClient] mocked). */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GoogleBillingGatewayTest {
    private val client = mockk<BillingClient>(relaxed = true)
    private lateinit var listener: (BillingResult, List<Purchase>?) -> Unit
    private val gateway = GoogleBillingGateway { l ->
        listener = l
        client
    }

    private fun result(code: Int) = BillingResult.newBuilder().setResponseCode(code).build()

    private fun purchase(state: Int, acknowledged: Boolean = false) = mockk<Purchase> {
        every { purchaseToken } returns "t-$state"
        every { products } returns listOf("remove_ads_week")
        every { purchaseState } returns state
        every { isAcknowledged } returns acknowledged
    }

    @Test
    fun `connects once, and says when Play can't be reached`() = runTest {
        every { client.isReady } returns true
        assertTrue(gateway.connect())

        every { client.isReady } returns false
        val setup = slot<BillingClientStateListener>()
        every { client.startConnection(capture(setup)) } returns Unit
        val ok = async { gateway.connect() }
        runCurrent()
        setup.captured.onBillingSetupFinished(result(BillingResponseCode.OK))
        setup.captured.onBillingServiceDisconnected() // after the answer: ignored
        assertTrue(ok.await())

        val down = async { gateway.connect() }
        runCurrent()
        setup.captured.onBillingServiceDisconnected()
        assertFalse(down.await())

        val failed = async { gateway.connect() }
        runCurrent()
        setup.captured.onBillingSetupFinished(result(BillingResponseCode.BILLING_UNAVAILABLE))
        assertFalse(failed.await())
    }

    @Test
    fun `products come with Play's price, and none when Play fails`() = runTest {
        val answer = slot<ProductDetailsResponseListener>()
        every { client.queryProductDetailsAsync(any(), capture(answer)) } returns Unit
        val week = mockk<ProductDetails> {
            every { productId } returns "remove_ads_week"
            every { oneTimePurchaseOfferDetails } returns mockk { every { formattedPrice } returns "CA$1.99" }
        }
        val noOffer = mockk<ProductDetails> { every { oneTimePurchaseOfferDetails } returns null }

        val found = async { gateway.products(listOf("remove_ads_week")) }
        runCurrent()
        answer.captured.onProductDetailsResponse(result(BillingResponseCode.OK), QueryProductDetailsResult.create(listOf(week, noOffer), emptyList()))
        val products = found.await()!!
        assertEquals(listOf("remove_ads_week" to "CA$1.99"), products.map { it.id to it.price })

        val failed = async { gateway.products(listOf("remove_ads_week")) }
        runCurrent()
        answer.captured.onProductDetailsResponse(result(BillingResponseCode.ERROR), QueryProductDetailsResult.create(emptyList(), emptyList()))
        assertNull(failed.await())
    }

    @Test
    fun `launching needs Play's product, and returns the launch's code`() {
        val activity = mockk<Activity>()
        assertEquals(BillingResponseCode.ITEM_UNAVAILABLE, gateway.launch(activity, StoreProduct("remove_ads_week", "CA$1.99", null)))

        // Google's builders check the product details they're given: stubbed, to see the launch itself.
        mockkStatic(BillingFlowParams::class, BillingFlowParams.ProductDetailsParams::class)
        val details = mockk<ProductDetails>()
        val productParams = mockk<BillingFlowParams.ProductDetailsParams>()
        every { BillingFlowParams.ProductDetailsParams.newBuilder() } returns mockk {
            every { setProductDetails(details) } returns this
            every { build() } returns productParams
        }
        val params = mockk<BillingFlowParams>()
        every { BillingFlowParams.newBuilder() } returns mockk {
            every { setProductDetailsParamsList(listOf(productParams)) } returns this
            every { build() } returns params
        }
        every { client.launchBillingFlow(activity, params) } returns result(BillingResponseCode.OK)
        assertEquals(BillingResponseCode.OK, gateway.launch(activity, StoreProduct("remove_ads_week", "CA$1.99", details)))
        unmockkAll()
    }

    @Test
    fun `purchases, acknowledge and consume report Play's answers`() = runTest {
        val owned = slot<PurchasesResponseListener>()
        every { client.queryPurchasesAsync(any<com.android.billingclient.api.QueryPurchasesParams>(), capture(owned)) } returns Unit
        val list = async { gateway.purchases() }
        runCurrent()
        owned.captured.onQueryPurchasesResponse(
            result(BillingResponseCode.OK),
            listOf(purchase(Purchase.PurchaseState.PURCHASED, acknowledged = true), purchase(Purchase.PurchaseState.PENDING), purchase(0)),
        )
        assertEquals(
            listOf(StorePurchase.State.PURCHASED, StorePurchase.State.PENDING, StorePurchase.State.OTHER),
            list.await()!!.map { it.state },
        )
        val none = async { gateway.purchases() }
        runCurrent()
        owned.captured.onQueryPurchasesResponse(result(BillingResponseCode.SERVICE_UNAVAILABLE), emptyList())
        assertNull(none.await())

        val ack = slot<AcknowledgePurchaseResponseListener>()
        every { client.acknowledgePurchase(any(), capture(ack)) } returns Unit
        val acked = async { gateway.acknowledge("t") }
        runCurrent()
        ack.captured.onAcknowledgePurchaseResponse(result(BillingResponseCode.OK))
        assertTrue(acked.await())

        val consume = slot<ConsumeResponseListener>()
        every { client.consumeAsync(any(), capture(consume)) } returns Unit
        val consumed = async { gateway.consume("t") }
        runCurrent()
        consume.captured.onConsumeResponse(result(BillingResponseCode.ERROR), "t")
        assertFalse(consumed.await())
    }

    @Test
    fun `purchases updated by Play come out as updates`() = runTest {
        val next = async { gateway.updates.first() }
        runCurrent()
        listener(result(BillingResponseCode.OK), listOf(purchase(Purchase.PurchaseState.PURCHASED)))
        val update = next.await()
        assertEquals(BillingResponseCode.OK, update.code)
        assertEquals("t-${Purchase.PurchaseState.PURCHASED}", update.purchases.single().token)

        val cancelled = async { gateway.updates.first() }
        runCurrent()
        listener(result(BillingResponseCode.USER_CANCELED), null)
        assertTrue(cancelled.await().purchases.isEmpty())
    }
}
