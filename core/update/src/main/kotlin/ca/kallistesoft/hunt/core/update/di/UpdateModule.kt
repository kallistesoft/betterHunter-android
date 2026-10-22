package ca.kallistesoft.hunt.core.update.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import ca.kallistesoft.hunt.core.update.ForceUpdateStore
import ca.kallistesoft.hunt.core.update.HttpUpdateConfigSource
import ca.kallistesoft.hunt.core.update.UpdateConfigSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class UpdateModule {
    @Binds
    abstract fun configSource(impl: HttpUpdateConfigSource): UpdateConfigSource

    companion object {
        @Provides
        @Singleton
        @ForceUpdateStore
        fun store(@ApplicationContext context: Context): DataStore<Preferences> =
            PreferenceDataStoreFactory.create(produceFile = { context.preferencesDataStoreFile("force_update") })
    }
}
