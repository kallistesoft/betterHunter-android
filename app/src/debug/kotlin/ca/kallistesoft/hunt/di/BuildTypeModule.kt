package ca.kallistesoft.hunt.di

import ca.kallistesoft.hunt.core.billing.Purchases
import ca.kallistesoft.hunt.core.billing.di.TestStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object BuildTypeModule {
    /**
     * Debug: its own minimum version parameter, so the force-update flow can be tried without affecting users, and
     * Remote Config fetched on every refresh so console changes show at once.
     */
    @Provides
    fun buildTypeConfig() = BuildTypeConfig(
        minVersionParameter = "min_version_code_debug",
        featureFlagFetchIntervalSeconds = 0,
    )

    /** Debug: Remove ads without Play (its application id has no products there): bought at once, nothing charged. */
    @Provides
    fun purchases(@TestStore purchases: Purchases): Purchases = purchases
}
