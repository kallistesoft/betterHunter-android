package ca.kallistesoft.hunt.feature.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.data.UserDataRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** "Yes, I passed" on [examId]: the user becomes a new hunter. */
@HiltViewModel(assistedFactory = PassedExamViewModel.Factory::class)
class PassedExamViewModel @AssistedInject constructor(
    @Assisted private val examId: String,
    private val userData: UserDataRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(examId: String): PassedExamViewModel
    }

    private val _saved = MutableStateFlow(false)

    /**
     * True once the exam is saved as passed: the screen then navigates. A flag rather than a callback, so a rotation
     * during the save doesn't lose the navigation (a callback would point at the old composition's back stack).
     */
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private var saving = false

    fun confirm() {
        if (saving) return
        saving = true
        viewModelScope.launch {
            userData.markExamPassed(examId)
            _saved.value = true
        }
    }
}
