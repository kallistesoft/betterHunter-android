package ca.kallistesoft.hunt.feature.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.data.ContentRepository
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.model.ExamGroup
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.ui.ExamSummary
import ca.kallistesoft.hunt.core.ui.examSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ExamsUiState(
    /** The user's exams (set by their provinces), the home province's hunter course first. */
    val mine: List<ExamSummary> = emptyList(),
    /** Hunter courses the user said they passed for real. */
    val passed: Set<String> = emptySet(),
    /** Provinces not added yet, offered under "Add more". */
    val otherProvinces: List<Province> = emptyList(),
    /** Recognition games ("What is it?"). */
    val games: List<ExamSummary> = emptyList(),
    val loading: Boolean = true,
) {
    /** "I passed the real exam" is offered on hunter courses, the exams that make a hunter. */
    fun canMarkPassed(summary: ExamSummary): Boolean =
        summary.available && summary.exam.group == ExamGroup.HUNTING && summary.exam.id !in passed
}

/** Exam training and recognition games: the user's exams with their readiness. */
@HiltViewModel
class ExamsViewModel @Inject constructor(
    contentRepository: ContentRepository,
    userData: UserDataRepository,
) : ViewModel() {
    val uiState: StateFlow<ExamsUiState> = combine(contentRepository.content, userData.progress) { content, progress ->
        // Readiness over every question: off the main thread. withContext, not flowOn, so stateIn keeps
        // setting the state on the main thread.
        withContext(Dispatchers.Default) {
            val mine = progress.myExamIds.mapNotNull { id -> content.trainingExams.firstOrNull { it.id == id } }
            ExamsUiState(
                mine = mine.map { examSummary(content, progress, it) },
                passed = progress.passedExams,
                otherProvinces = Province.entries.filterNot { it in progress.provinces },
                games = content.games.map { examSummary(content, progress, it) },
                loading = false,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExamsUiState())
}
