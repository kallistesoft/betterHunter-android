package ca.kallistesoft.hunt.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object BuildTypeModule {
    /** Release: the update rules users get, and feature flags fetched at most hourly (Remote Config's quota). */
    @Provides
    fun buildTypeConfig() = BuildTypeConfig(
        updateConfigUrl = "https://hunt.kallistesoft.ca/android/update.json",
        featureFlagFetchIntervalSeconds = 3_600,
    )
}
