package ca.kallistesoft.hunt.feature.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.data.UserDataRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private var saving = false

    fun confirm(onSaved: () -> Unit) {
        if (saving) return
        saving = true
        viewModelScope.launch {
            userData.markExamPassed(examId)
            onSaved()
        }
    }
}
