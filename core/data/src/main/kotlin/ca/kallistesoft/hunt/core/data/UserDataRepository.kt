package ca.kallistesoft.hunt.core.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import ca.kallistesoft.hunt.core.model.UserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Today as a local calendar day number (days since the epoch); swapped out in tests. */
fun interface Today {
    operator fun invoke(): Long

    companion object {
        val System = Today { LocalDate.now().toEpochDay() }
    }
}

/** The user's local progress. Nothing leaves the device. */
interface UserDataRepository {
    val progress: Flow<UserProgress>

    suspend fun record(questionId: String, correct: Boolean)

    /** Records several answers at once (end of a mock exam). */
    suspend fun recordAll(results: List<Pair<String, Boolean>>)

    suspend fun setMyExams(ids: List<String>)

    /** Answers today's photo of the day; it also counts toward the goal, streak and weak spots. */
    suspend fun answerPhotoOfDay(questionId: String, choice: Int, correct: Boolean)
}

@Singleton
internal class DataStoreUserDataRepository @Inject constructor(
    private val store: DataStore<UserProgress>,
    private val today: Today,
) : UserDataRepository {
    override val progress: Flow<UserProgress> = store.data

    override suspend fun record(questionId: String, correct: Boolean) {
        store.updateData { it.record(questionId, correct, today()) }
    }

    override suspend fun recordAll(results: List<Pair<String, Boolean>>) {
        if (results.isEmpty()) return
        val day = today()
        store.updateData { p -> results.fold(p) { acc, (id, correct) -> acc.record(id, correct, day) } }
    }

    override suspend fun setMyExams(ids: List<String>) {
        store.updateData { it.copy(myExams = ids) }
    }

    override suspend fun answerPhotoOfDay(questionId: String, choice: Int, correct: Boolean) {
        val day = today()
        store.updateData { it.withPhotoOfDayChoice(choice, day).record(questionId, correct, day) }
    }
}

internal object UserProgressSerializer : Serializer<UserProgress> {
    private val json = Json { ignoreUnknownKeys = true }

    override val defaultValue = UserProgress()

    override suspend fun readFrom(input: InputStream): UserProgress = try {
        json.decodeFromString(UserProgress.serializer(), input.readBytes().decodeToString())
    } catch (e: SerializationException) {
        throw CorruptionException("Unreadable progress file", e)
    }

    override suspend fun writeTo(t: UserProgress, output: OutputStream) {
        output.write(json.encodeToString(UserProgress.serializer(), t).encodeToByteArray())
    }
}
