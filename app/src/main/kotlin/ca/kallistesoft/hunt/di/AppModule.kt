package ca.kallistesoft.hunt.di

import ca.kallistesoft.hunt.BuildConfig
import ca.kallistesoft.hunt.core.update.ForceUpdateSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun forceUpdateSettings() = ForceUpdateSettings(
        configUrl = BuildConfig.UPDATE_CONFIG_URL,
        currentVersionCode = BuildConfig.VERSION_CODE.toLong(),
        storePackage = BuildConfig.STORE_PACKAGE,
    )
}
