package ca.kallistesoft.hunt.core.billing.di

import android.content.Context
import ca.kallistesoft.hunt.core.ads.AdFree
import ca.kallistesoft.hunt.core.billing.GoogleBillingGateway
import ca.kallistesoft.hunt.core.billing.PlayPurchases
import ca.kallistesoft.hunt.core.billing.Purchases
import ca.kallistesoft.hunt.core.billing.StoreAdFree
import ca.kallistesoft.hunt.core.billing.TestPurchases
import ca.kallistesoft.hunt.core.data.Now
import ca.kallistesoft.hunt.core.data.UserDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/** Purchases in Google Play ([PlayStore]) or without it ([TestStore], debug builds); the app binds [Purchases] to one. */
@Qualifier
annotation class PlayStore

@Qualifier
annotation class TestStore

@Module
@InstallIn(SingletonComponent::class)
internal object BillingModule {
    @Provides
    @Singleton
    @PlayStore
    fun playPurchases(@ApplicationContext context: Context, userData: UserDataRepository, now: Now): Purchases = PlayPurchases(
        gateway = GoogleBillingGateway.create(context),
        userData = userData,
        now = now,
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
    )

    @Provides
    @Singleton
    @TestStore
    fun testPurchases(userData: UserDataRepository, now: Now): Purchases = TestPurchases(userData, now)

    @Provides
    fun adFree(userData: UserDataRepository, now: Now): AdFree = StoreAdFree(userData, now)
}
