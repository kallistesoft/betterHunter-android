package ca.kallistesoft.hunt.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import ca.kallistesoft.hunt.core.data.AssetContentRepository
import ca.kallistesoft.hunt.core.data.ContentRepository
import ca.kallistesoft.hunt.core.data.DataStoreUserDataRepository
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.data.UserProgressSerializer
import ca.kallistesoft.hunt.core.model.UserProgress
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    abstract fun content(impl: AssetContentRepository): ContentRepository

    @Binds
    abstract fun userData(impl: DataStoreUserDataRepository): UserDataRepository

    companion object {
        @Provides
        @Singleton
        fun progressStore(@ApplicationContext context: Context): DataStore<UserProgress> = DataStoreFactory.create(
            serializer = UserProgressSerializer,
            corruptionHandler = ReplaceFileCorruptionHandler { UserProgressSerializer.defaultValue },
            produceFile = { context.dataStoreFile("user_progress.json") },
        )

        @Provides
        fun today(): Today = Today.System
    }
}
