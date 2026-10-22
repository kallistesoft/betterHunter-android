package ca.kallistesoft.hunt.core.flags.di

import android.content.Context
import ca.kallistesoft.hunt.core.flags.DefaultFeatureFlags
import ca.kallistesoft.hunt.core.flags.FeatureFlagSettings
import ca.kallistesoft.hunt.core.flags.FeatureFlags
import ca.kallistesoft.hunt.core.flags.FirebaseFeatureFlags
import com.google.firebase.FirebaseApp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object FlagsModule {
    /** Firebase when the build has a google-services.json (FirebaseApp initialised), defaults otherwise. */
    @Provides
    @Singleton
    fun featureFlags(@ApplicationContext context: Context, settings: FeatureFlagSettings): FeatureFlags =
        if (FirebaseApp.getApps(context).isEmpty()) {
            DefaultFeatureFlags()
        } else {
            FirebaseFeatureFlags.create(settings, CoroutineScope(SupervisorJob() + Dispatchers.Default))
        }
}
