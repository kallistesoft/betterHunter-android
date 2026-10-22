package ca.kallistesoft.hunt.feature.exams

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.data.ContentRepository
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.model.Exam
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PickerExam(val exam: Exam, val questionCount: Int)

data class ExamPickerUiState(
    val exams: List<PickerExam> = emptyList(),
    val chosen: Set<String> = emptySet(),
)

/** "Which exam are you preparing for?" Recognition games aren't exams, so they aren't offered. */
@HiltViewModel
class ExamPickerViewModel @Inject constructor(
    contentRepository: ContentRepository,
    private val userData: UserDataRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    // The selection survives rotation and process death; it starts from the saved choice.
    private val chosen = MutableStateFlow(savedState.get<ArrayList<String>>(KEY_CHOSEN)?.toSet())

    private val exams = MutableStateFlow<List<PickerExam>>(emptyList())

    val uiState: StateFlow<ExamPickerUiState> = combine(exams, chosen) { exams, chosen ->
        ExamPickerUiState(exams, chosen.orEmpty())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExamPickerUiState())

    init {
        viewModelScope.launch {
            val content = contentRepository.content.first()
            exams.value = content.trainingExams.map { PickerExam(it, content.questionsFor(it).size) }
            if (chosen.value == null) update(userData.progress.first().myExams.orEmpty().toSet())
        }
    }

    fun toggle(examId: String) {
        val current = chosen.value.orEmpty()
        update(if (examId in current) current - examId else current + examId)
    }

    /** Saves the chosen exams (in display order); with [all], every exam ("Skip" on first launch). */
    fun save(all: Boolean = false, onSaved: () -> Unit) {
        val selected = chosen.value.orEmpty()
        val ids = exams.value.map { it.exam.id }.filter { all || it in selected }
        viewModelScope.launch {
            userData.setMyExams(ids)
            onSaved()
        }
    }

    private fun update(ids: Set<String>) {
        chosen.value = ids
        savedState[KEY_CHOSEN] = ArrayList(ids)
    }

    private companion object {
        const val KEY_CHOSEN = "chosen"
    }
}
