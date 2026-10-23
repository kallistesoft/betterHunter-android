package ca.kallistesoft.hunt

import android.app.Application
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.reminders.Reminders
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Open only for the Robolectric tests, whose subclass sets up a test WorkManager before [onCreate] runs. */
@HiltAndroidApp
open class BetterHunterApplication : Application() {
    @Inject lateinit var userData: UserDataRepository

    override fun onCreate() {
        super.onCreate()
        // Reminders follow the progress: switched on or off, a season date, today's questions done, the playing time.
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            userData.progress
                .map { it.copy(lastResult = emptyMap(), weak = emptyMap(), correctDay = emptyMap()) }
                .distinctUntilChanged()
                .collect { Reminders.schedule(this@BetterHunterApplication, it) }
        }
    }
}
