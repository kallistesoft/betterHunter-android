package ca.kallistesoft.hunt.core.ads.di

import android.app.Application
import android.content.Context
import android.os.SystemClock
import ca.kallistesoft.hunt.core.ads.AdConsent
import ca.kallistesoft.hunt.core.ads.AdsConfig
import ca.kallistesoft.hunt.core.ads.AdsManager
import ca.kallistesoft.hunt.core.ads.GoogleAdsSdk
import ca.kallistesoft.hunt.core.ads.InterstitialAds
import ca.kallistesoft.hunt.core.ads.ResumedActivity
import ca.kallistesoft.hunt.core.ads.UmpConsent
import ca.kallistesoft.hunt.core.flags.FeatureFlags
import com.google.android.ump.UserMessagingPlatform
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/** AdMob behind [InterstitialAds] and [AdConsent]; the app provides [AdsConfig] (the ad unit of its build type). */
@Module
@InstallIn(SingletonComponent::class)
internal object AdsModule {
    @Provides
    @Singleton
    fun adsManager(@ApplicationContext context: Context, flags: FeatureFlags, config: AdsConfig): AdsManager {
        // Created when the activity is injected, before it resumes: the first resumed activity is seen.
        val resumed = ResumedActivity()
        (context.applicationContext as Application).registerActivityLifecycleCallbacks(resumed)
        return AdsManager(
            consent = UmpConsent(UserMessagingPlatform.getConsentInformation(context)),
            sdk = GoogleAdsSdk(context),
            flags = flags,
            currentActivity = resumed::get,
            config = config,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
            clock = SystemClock::elapsedRealtime,
        )
    }

    @Provides
    fun interstitialAds(manager: AdsManager): InterstitialAds = manager

    @Provides
    fun adConsent(manager: AdsManager): AdConsent = manager
}
