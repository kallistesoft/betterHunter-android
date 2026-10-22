package ca.kallistesoft.hunt.core.update

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Where the app learns which versions are still allowed, and who it is. Provided by the app module so each build type
 * can point at its own file.
 */
data class ForceUpdateSettings(
    /** JSON file such as `{"minVersionCode": 3}`; see the README for hosting. */
    val configUrl: String,
    val currentVersionCode: Long,
    /** Play Store listing to open when in-app updates are unavailable (the release application ID). */
    val storePackage: String,
)

/** Remote update rules. Unknown fields are ignored so the file can grow. */
@Serializable
data class RemoteUpdateConfig(val minVersionCode: Long = 0) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun parse(text: String): RemoteUpdateConfig = json.decodeFromString(serializer(), text)
    }
}

/** An installed build older than the minimum must update before it can be used. */
fun isUpdateRequired(currentVersionCode: Long, minVersionCode: Long?): Boolean =
    minVersionCode != null && currentVersionCode < minVersionCode
