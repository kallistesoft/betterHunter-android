package ca.kallistesoft.hunt.core.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.Reminder
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.Weapon
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.LocalTime
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

    /** Saves the answers to onboarding. */
    suspend fun saveProfile(type: HunterType, weapons: Set<Weapon>, home: Province)

    /** Adds or hides a province's quizzes (My provinces). */
    suspend fun setProvince(province: Province, enabled: Boolean)

    /** "I passed the real exam": the user becomes a hunter. */
    suspend fun markExamPassed(examId: String)

    /** Mock [mock] of [examId] finished with [percent] right (P14). */
    suspend fun recordMockResult(examId: String, mock: Int, percent: Int)

    /** "Reset all" on [examId]'s mock exams. */
    suspend fun resetMocks(examId: String)

    suspend fun dismissNewHunterBanner()

    // Settings
    suspend fun setHunterType(type: HunterType)

    suspend fun setWeapons(weapons: Set<Weapon>)

    suspend fun setHomeProvince(province: Province)

    suspend fun setDailyGoal(goal: Int)

    /** Answers today's photo of the day; it also counts toward the goal, streak and weak spots. */
    suspend fun answerPhotoOfDay(questionId: String, choice: Int, correct: Boolean)

    // Hunter (P11)

    /** Answers one of today's 6 questions. */
    suspend fun recordDaily(questionId: String, correct: Boolean)

    /** The hunter's first hunting day (a day number), or null for "I don't know yet". */
    suspend fun setSeasonStart(day: Long?)

    suspend fun setDailyReminders(enabled: Boolean)

    suspend fun setSeasonReminders(enabled: Boolean)

    /** "Want a nudge tomorrow?" can't lead anywhere (the system dialog refused): it isn't asked again. */
    suspend fun markDailyReminderAsked()

    /** "Want a nudge tomorrow?" was answered "Not now": asked again in a week. */
    suspend fun notNowDailyReminders()

    /** A reminder of [kind] was shown on [day]. */
    suspend fun reminderShown(kind: Reminder.Kind, day: Long)

    /** The app came to the foreground: ignored reminders stop backing off. */
    suspend fun appOpened()
}

@Singleton
internal class DataStoreUserDataRepository @Inject constructor(
    private val store: DataStore<UserProgress>,
    private val today: Today,
) : UserDataRepository {
    override val progress: Flow<UserProgress> = store.data

    override suspend fun record(questionId: String, correct: Boolean) {
        val day = today()
        store.updateData { it.record(questionId, correct, day).withPlayTime(day, minuteNow()) }
    }

    override suspend fun recordAll(results: List<Pair<String, Boolean>>) {
        if (results.isEmpty()) return
        val day = today()
        store.updateData { p -> results.fold(p) { acc, (id, correct) -> acc.record(id, correct, day) } }
    }

    override suspend fun saveProfile(type: HunterType, weapons: Set<Weapon>, home: Province) {
        store.updateData { it.withProfile(type, weapons, home) }
    }

    override suspend fun setProvince(province: Province, enabled: Boolean) {
        store.updateData { it.withProvince(province, enabled) }
    }

    override suspend fun markExamPassed(examId: String) {
        store.updateData { it.withExamPassed(examId) }
    }

    override suspend fun recordMockResult(examId: String, mock: Int, percent: Int) {
        store.updateData { it.withMockResult(examId, mock, percent) }
    }

    override suspend fun resetMocks(examId: String) {
        store.updateData { it.withMocksReset(examId) }
    }

    override suspend fun dismissNewHunterBanner() {
        store.updateData { it.copy(newHunterBanner = false) }
    }

    override suspend fun setHunterType(type: HunterType) {
        store.updateData { it.withHunterType(type) }
    }

    override suspend fun setWeapons(weapons: Set<Weapon>) {
        store.updateData { it.withWeapons(weapons) }
    }

    override suspend fun setHomeProvince(province: Province) {
        store.updateData { it.withHomeProvince(province) }
    }

    override suspend fun setDailyGoal(goal: Int) {
        store.updateData { it.withDailyGoal(goal) }
    }

    override suspend fun answerPhotoOfDay(questionId: String, choice: Int, correct: Boolean) {
        val day = today()
        store.updateData { it.withPhotoOfDayChoice(choice, day).record(questionId, correct, day).withPlayTime(day, minuteNow()) }
    }

    override suspend fun recordDaily(questionId: String, correct: Boolean) {
        val day = today()
        store.updateData { it.recordDaily(questionId, correct, day).withPlayTime(day, minuteNow()) }
    }

    override suspend fun setSeasonStart(day: Long?) {
        store.updateData { it.withSeasonStart(day) }
    }

    override suspend fun setDailyReminders(enabled: Boolean) {
        store.updateData { it.copy(dailyReminders = enabled, dailyReminderAsked = true) }
    }

    override suspend fun setSeasonReminders(enabled: Boolean) {
        store.updateData { it.copy(seasonReminders = enabled) }
    }

    override suspend fun markDailyReminderAsked() {
        store.updateData { it.copy(dailyReminderAsked = true) }
    }

    override suspend fun notNowDailyReminders() {
        store.updateData { it.copy(dailyReminderNotNowDay = today()) }
    }

    override suspend fun reminderShown(kind: Reminder.Kind, day: Long) {
        store.updateData { it.withReminderShown(kind, day) }
    }

    override suspend fun appOpened() {
        store.updateData { it.withAppOpened() }
    }

    /** When the user plays, to time reminders (learned on the device only). */
    private fun minuteNow(): Int = LocalTime.now().let { it.hour * 60 + it.minute }
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
