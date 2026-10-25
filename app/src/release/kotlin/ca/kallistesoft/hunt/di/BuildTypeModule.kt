package ca.kallistesoft.hunt.di

import ca.kallistesoft.hunt.core.billing.Purchases
import ca.kallistesoft.hunt.core.billing.di.PlayStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object BuildTypeModule {
    /** Release: the minimum version users get, and Remote Config fetched at most hourly (its quota). */
    @Provides
    fun buildTypeConfig() = BuildTypeConfig(
        minVersionParameter = "min_version_code",
        featureFlagFetchIntervalSeconds = 3_600,
    )

    /** Release: Remove ads is bought in Google Play. */
    @Provides
    fun purchases(@PlayStore purchases: Purchases): Purchases = purchases
}
