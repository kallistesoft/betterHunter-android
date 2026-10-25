package ca.kallistesoft.hunt.core.billing

import ca.kallistesoft.hunt.core.ads.AdFree
import ca.kallistesoft.hunt.core.data.Now
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.model.UserProgress
import kotlinx.coroutines.flow.first

/** The entitlements [this] progress holds at [nowMillis]: what was bought, never which product (P15). */
fun UserProgress.entitlements(nowMillis: Long): Set<Entitlement> =
    if (isAdFree(nowMillis)) setOf(Entitlement.NO_ADS) else emptySet()

/** [AdFree] from the saved purchases: the ads stop while [Entitlement.NO_ADS] is held. */
internal class StoreAdFree(private val userData: UserDataRepository, private val now: Now) : AdFree {
    override suspend fun isAdFree(): Boolean = Entitlement.NO_ADS in userData.progress.first().entitlements(now())
}
