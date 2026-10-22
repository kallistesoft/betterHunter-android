package ca.kallistesoft.hunt.feature.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.data.ContentRepository
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.ui.ExamSummary
import ca.kallistesoft.hunt.core.ui.examSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ExamsUiState(
    /** The user's exams, shown as full cards. */
    val mine: List<ExamSummary> = emptyList(),
    /** The other training exams, shown as compact rows. */
    val others: List<ExamSummary> = emptyList(),
    /** Recognition games ("What is it?"). */
    val games: List<ExamSummary> = emptyList(),
    val loading: Boolean = true,
)

/** Exam training and recognition games: every exam with the user's readiness. */
@HiltViewModel
class ExamsViewModel @Inject constructor(
    contentRepository: ContentRepository,
    userData: UserDataRepository,
) : ViewModel() {
    val uiState: StateFlow<ExamsUiState> = combine(contentRepository.content, userData.progress) { content, progress ->
        val chosen = progress.myExams.orEmpty()
        val training = content.trainingExams.map { examSummary(content, progress, it) }
        ExamsUiState(
            mine = training.filter { it.exam.id in chosen },
            others = training.filterNot { it.exam.id in chosen },
            games = content.games.map { examSummary(content, progress, it) },
            loading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExamsUiState())
}
