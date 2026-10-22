package ca.kallistesoft.hunt.core.update

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Decides whether this build must be updated before use. The last minimum version fetched is kept on the device, so
 * a blocked build stays blocked offline; if the rules were never fetched, the app is never blocked.
 */
@Singleton
class ForceUpdateRepository @Inject constructor(
    @ForceUpdateStore private val store: DataStore<Preferences>,
    private val source: UpdateConfigSource,
    private val settings: ForceUpdateSettings,
) {
    val updateRequired: Flow<Boolean> = store.data
        .map { isUpdateRequired(settings.currentVersionCode, it[MIN_VERSION_CODE]) }
        .distinctUntilChanged()

    /** Fetches the latest rules; keeps the cached ones when that fails. */
    suspend fun refresh() {
        val config = source.fetch(settings.configUrl) ?: return
        store.edit { it[MIN_VERSION_CODE] = config.minVersionCode }
    }

    private companion object {
        val MIN_VERSION_CODE = longPreferencesKey("min_version_code")
    }
}
