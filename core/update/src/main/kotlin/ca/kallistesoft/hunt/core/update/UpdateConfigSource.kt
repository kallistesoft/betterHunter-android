package ca.kallistesoft.hunt.core.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

/** Fetches the remote update rules. */
fun interface UpdateConfigSource {
    /** The current rules, or null when they can't be fetched (offline, server error, bad file). */
    suspend fun fetch(url: String): RemoteUpdateConfig?
}

internal class HttpUpdateConfigSource @Inject constructor() : UpdateConfigSource {
    override suspend fun fetch(url: String): RemoteUpdateConfig? = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL(url).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                connection.useCaches = false
                connection.setRequestProperty("Accept", "application/json")
                if (connection.responseCode != HttpURLConnection.HTTP_OK) return@runCatching null
                RemoteUpdateConfig.parse(connection.inputStream.bufferedReader().use { it.readText() })
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }

    private companion object {
        const val TIMEOUT_MS = 10_000
    }
}
