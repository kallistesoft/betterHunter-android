package ca.kallistesoft.hunt.core.data

import android.content.Context
import ca.kallistesoft.hunt.core.model.Content
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import javax.inject.Inject
import javax.inject.Singleton

/** Exam recipes and question banks bundled as assets (see scripts/sync-content.sh). */
interface ContentRepository {
    val content: Flow<Content>
}

@Singleton
internal class AssetContentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : ContentRepository {
    private val mutex = Mutex()
    private var cached: Content? = null

    override val content: Flow<Content> = flow { emit(load()) }

    private suspend fun load(): Content = mutex.withLock {
        cached ?: withContext(Dispatchers.IO) {
            ContentParser.content(read("content/exams.json")!!) { scope -> read("content/questions/$scope.json") }
        }.also { cached = it }
    }

    private fun read(path: String): String? = try {
        context.assets.open(path).bufferedReader().use { it.readText() }
    } catch (_: FileNotFoundException) {
        null
    }
}
