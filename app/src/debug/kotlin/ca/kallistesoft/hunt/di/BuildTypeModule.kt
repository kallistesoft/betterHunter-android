package ca.kallistesoft.hunt.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object BuildTypeModule {
    /**
     * Debug: separate update rules, so the force-update flow can be tried without affecting users, and feature flags
     * fetched on every refresh so console changes show at once.
     */
    @Provides
    fun buildTypeConfig() = BuildTypeConfig(
        updateConfigUrl = "https://hunt.kallistesoft.ca/android/update-debug.json",
        featureFlagFetchIntervalSeconds = 0,
    )
}
