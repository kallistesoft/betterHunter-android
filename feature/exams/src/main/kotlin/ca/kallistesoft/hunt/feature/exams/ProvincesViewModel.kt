package ca.kallistesoft.hunt.feature.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.model.Province
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One province's switch: on when its quizzes are shown; the last one on can't be switched off. */
data class ProvinceItem(val province: Province, val enabled: Boolean, val isHome: Boolean, val canToggle: Boolean)

data class ProvincesUiState(val provinces: List<ProvinceItem> = emptyList(), val loading: Boolean = true)

/** "My provinces": each province adds its quizzes to Exam training, or hides them. */
@HiltViewModel
class ProvincesViewModel @Inject constructor(private val userData: UserDataRepository) : ViewModel() {
    val uiState: StateFlow<ProvincesUiState> = userData.progress.map { progress ->
        val lastOne = progress.provinces.size <= 1
        ProvincesUiState(
            provinces = Province.entries.map { p ->
                val on = p in progress.provinces
                ProvinceItem(p, on, isHome = p == progress.homeProvince, canToggle = !(on && lastOne))
            },
            loading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProvincesUiState())

    fun setEnabled(province: Province, enabled: Boolean) {
        viewModelScope.launch { userData.setProvince(province, enabled) }
    }
}
