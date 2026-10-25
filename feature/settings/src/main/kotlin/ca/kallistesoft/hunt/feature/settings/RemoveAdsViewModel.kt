package ca.kallistesoft.hunt.feature.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.billing.AdFreeProduct
import ca.kallistesoft.hunt.core.billing.BuyResult
import ca.kallistesoft.hunt.core.billing.Purchases
import ca.kallistesoft.hunt.core.data.Now
import ca.kallistesoft.hunt.core.data.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/** What the screen says after a purchase or a restore. */
enum class RemoveAdsMessage { PENDING, UNAVAILABLE, FAILED, RESTORED, RESTORE_FAILED }

data class RemoveAdsUiState(
    /** Play's localized prices; a product missing here shows "Price shown by Google Play". */
    val prices: Map<AdFreeProduct, String> = emptyMap(),
    val forever: Boolean = false,
    /** The last day of the running 7-day pass (a day number), or null. */
    val untilDay: Long? = null,
    /** A purchase or restore is under way: the buttons wait. */
    val busy: Boolean = false,
    val message: RemoveAdsMessage? = null,
    val loading: Boolean = true,
)

/** Remove ads (P15): 7 days (kept on this phone only) or forever, bought in Google Play, and Restore purchases. */
@HiltViewModel
class RemoveAdsViewModel @Inject constructor(
    userData: UserDataRepository,
    private val purchases: Purchases,
    private val now: Now,
) : ViewModel() {
    private data class Action(val busy: Boolean = false, val message: RemoveAdsMessage? = null)

    private val action = MutableStateFlow(Action())

    val uiState: StateFlow<RemoveAdsUiState> = combine(userData.progress, purchases.prices, action) { p, prices, a ->
        RemoveAdsUiState(
            prices = prices,
            forever = p.adFreeForever,
            untilDay = p.adFreeUntil?.takeIf { it > now() }?.let(::epochDay),
            busy = a.busy,
            message = a.message,
            loading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RemoveAdsUiState())

    fun buy(activity: Activity, product: AdFreeProduct) = run {
        when (purchases.buy(activity, product)) {
            BuyResult.BOUGHT, BuyResult.CANCELLED -> null
            BuyResult.PENDING -> RemoveAdsMessage.PENDING
            BuyResult.UNAVAILABLE -> RemoveAdsMessage.UNAVAILABLE
            BuyResult.FAILED -> RemoveAdsMessage.FAILED
        }
    }

    fun restore() = run { if (purchases.restore()) RemoveAdsMessage.RESTORED else RemoveAdsMessage.RESTORE_FAILED }

    fun dismissMessage() = action.update { it.copy(message = null) }

    private fun run(block: suspend () -> RemoveAdsMessage?) {
        if (action.value.busy) return
        action.value = Action(busy = true)
        viewModelScope.launch { action.value = Action(message = block()) }
    }
}

/** The day (a day number) of [millis], in the phone's time zone. */
internal fun epochDay(millis: Long): Long = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
