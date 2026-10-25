package ca.kallistesoft.hunt.feature.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.ads.RewardOutcome
import ca.kallistesoft.hunt.core.ads.RewardedAds
import ca.kallistesoft.hunt.core.data.ContentRepository
import ca.kallistesoft.hunt.core.data.Now
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.model.Exam
import ca.kallistesoft.hunt.core.model.MOCK_COUNT
import ca.kallistesoft.hunt.core.model.MockResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One mock exam: its number and, once finished, its scores. */
data class MockRow(val number: Int, val result: MockResult?, val passed: Boolean)

/** What a rewarded ad unlocks: a new run of one mock, or all of them reset. */
sealed interface MockUnlock {
    data class Retake(val mock: Int) : MockUnlock

    data object ResetAll : MockUnlock
}

data class MockExamsUiState(
    val exam: Exam? = null,
    val mocks: List<MockRow> = emptyList(),
    /** A rewarded ad is loading or on screen: the buttons wait. */
    val watchingAd: Boolean = false,
    /** The ad couldn't load for lack of network: "You're offline", and nothing is unlocked. */
    val offline: Boolean = false,
    /** A retake was unlocked: the screen starts this mock, then calls [MockExamsViewModel.started]. */
    val start: Int? = null,
    /** The user paid to remove ads (P15): retakes and "Reset all" start at once, without asking for an ad. */
    val adFree: Boolean = false,
    val loading: Boolean = true,
) {
    val passedCount: Int get() = mocks.count { it.passed }
    val allDone: Boolean get() = mocks.isNotEmpty() && mocks.all { it.result != null }
}

/**
 * Mock exams of [examId] (P14): ten, each with its best and last score. A mock never taken is free; a retake, and
 * "Reset all" once all ten are done, need a rewarded ad. Offline means no ad, so nothing is unlocked.
 */
@HiltViewModel(assistedFactory = MockExamsViewModel.Factory::class)
class MockExamsViewModel @AssistedInject constructor(
    @Assisted private val examId: String,
    contentRepository: ContentRepository,
    private val userData: UserDataRepository,
    private val ads: RewardedAds,
    private val now: Now,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(examId: String): MockExamsViewModel
    }

    private data class AdState(val watching: Boolean = false, val offline: Boolean = false, val start: Int? = null)

    private val adState = MutableStateFlow(AdState())

    val uiState: StateFlow<MockExamsUiState> =
        combine(contentRepository.content, userData.progress, adState) { content, progress, ad ->
            val exam = content.exam(examId)
            val results = progress.mockResults(examId)
            MockExamsUiState(
                exam = exam,
                mocks = (1..MOCK_COUNT).map { n ->
                    val result = results[n]
                    MockRow(n, result, passed = exam != null && result?.passed(exam) == true)
                },
                watchingAd = ad.watching,
                offline = ad.offline,
                start = ad.start,
                adFree = progress.isAdFree(now()),
                loading = false,
            )
        }
            // The ad loads while the user looks at the list, so "Watch ad" rarely waits.
            .onStart { ads.preload() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MockExamsUiState())

    /** "Watch ad" on a retake or "Reset all": the reward is given once the ad was watched (or none is available). */
    fun unlock(what: MockUnlock) {
        if (adState.value.watching) return
        adState.update { it.copy(watching = true, offline = false) }
        viewModelScope.launch {
            val outcome = ads.showForReward()
            val unlocked = outcome == RewardOutcome.EARNED || outcome == RewardOutcome.NO_ADS
            if (unlocked && what == MockUnlock.ResetAll) userData.resetMocks(examId)
            adState.update {
                AdState(
                    offline = outcome == RewardOutcome.OFFLINE,
                    start = (what as? MockUnlock.Retake)?.mock?.takeIf { unlocked },
                )
            }
        }
    }

    /** The unlocked retake was opened. */
    fun started() = adState.update { it.copy(start = null) }

    fun dismissOffline() = adState.update { it.copy(offline = false) }
}
