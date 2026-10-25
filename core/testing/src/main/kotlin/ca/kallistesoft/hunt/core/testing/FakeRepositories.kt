package ca.kallistesoft.hunt.core.testing

import ca.kallistesoft.hunt.core.data.ContentRepository
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.flags.FeatureFlag
import ca.kallistesoft.hunt.core.flags.FeatureFlags
import ca.kallistesoft.hunt.core.model.Content
import ca.kallistesoft.hunt.core.model.HunterType
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.model.Reminder
import ca.kallistesoft.hunt.core.model.UserProgress
import ca.kallistesoft.hunt.core.model.Weapon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update

/** Day number used by the fakes and tests. */
const val TEST_DAY = 20_000L

class FakeContentRepository(content: Content = TestContent.content) : ContentRepository {
    override val content: Flow<Content> = flowOf(content)
}

/** Applies the real progress rules on [TEST_DAY], and remembers every recorded answer. */
class FakeUserDataRepository(initial: UserProgress = UserProgress()) : UserDataRepository {
    override val progress = MutableStateFlow(initial)
    val recorded = mutableListOf<Pair<String, Boolean>>()

    override suspend fun record(questionId: String, correct: Boolean) {
        recorded += questionId to correct
        progress.update { it.record(questionId, correct, TEST_DAY) }
    }

    override suspend fun recordAll(results: List<Pair<String, Boolean>>) {
        results.forEach { (id, correct) -> record(id, correct) }
    }

    override suspend fun saveProfile(type: HunterType, weapons: Set<Weapon>, home: Province) {
        progress.update { it.withProfile(type, weapons, home) }
    }

    override suspend fun setProvince(province: Province, enabled: Boolean) {
        progress.update { it.withProvince(province, enabled) }
    }

    override suspend fun markExamPassed(examId: String) {
        progress.update { it.withExamPassed(examId) }
    }

    override suspend fun recordMockResult(examId: String, mock: Int, percent: Int) {
        progress.update { it.withMockResult(examId, mock, percent) }
    }

    override suspend fun resetMocks(examId: String) {
        progress.update { it.withMocksReset(examId) }
    }

    override suspend fun dismissNewHunterBanner() {
        progress.update { it.copy(newHunterBanner = false) }
    }

    override suspend fun setHunterType(type: HunterType) {
        progress.update { it.withHunterType(type) }
    }

    override suspend fun setWeapons(weapons: Set<Weapon>) {
        progress.update { it.withWeapons(weapons) }
    }

    override suspend fun setHomeProvince(province: Province) {
        progress.update { it.withHomeProvince(province) }
    }

    override suspend fun setDailyGoal(goal: Int) {
        progress.update { it.withDailyGoal(goal) }
    }

    override suspend fun answerPhotoOfDay(questionId: String, choice: Int, correct: Boolean) {
        recorded += questionId to correct
        progress.update { it.withPhotoOfDayChoice(choice, TEST_DAY).record(questionId, correct, TEST_DAY) }
    }

    override suspend fun recordDaily(questionId: String, correct: Boolean) {
        recorded += questionId to correct
        progress.update { it.recordDaily(questionId, correct, TEST_DAY) }
    }

    override suspend fun setSeasonStart(day: Long?) {
        progress.update { it.withSeasonStart(day) }
    }

    override suspend fun setDailyReminders(enabled: Boolean) {
        progress.update { it.copy(dailyReminders = enabled, dailyReminderAsked = true) }
    }

    override suspend fun setSeasonReminders(enabled: Boolean) {
        progress.update { it.copy(seasonReminders = enabled) }
    }

    override suspend fun markDailyReminderAsked() {
        progress.update { it.copy(dailyReminderAsked = true) }
    }

    override suspend fun notNowDailyReminders() {
        progress.update { it.copy(dailyReminderNotNowDay = TEST_DAY) }
    }

    override suspend fun reminderShown(kind: Reminder.Kind, day: Long) {
        progress.update { it.withReminderShown(kind, day) }
    }

    override suspend fun appOpened() {
        progress.update { it.withAppOpened() }
    }
}

/** Flags that can be flipped during a test. */
class FakeFeatureFlags(vararg off: FeatureFlag) : FeatureFlags {
    override val values = MutableStateFlow(FeatureFlag.entries.associateWith { it !in off && it.defaultValue })

    fun set(flag: FeatureFlag, enabled: Boolean) = values.update { it + (flag to enabled) }

    override suspend fun refresh() = Unit
}
