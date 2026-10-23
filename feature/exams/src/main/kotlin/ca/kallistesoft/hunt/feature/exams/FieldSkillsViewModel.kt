package ca.kallistesoft.hunt.feature.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.kallistesoft.hunt.core.data.ContentRepository
import ca.kallistesoft.hunt.core.data.Today
import ca.kallistesoft.hunt.core.data.UserDataRepository
import ca.kallistesoft.hunt.core.model.FieldSkill
import ca.kallistesoft.hunt.core.model.FieldTraining
import ca.kallistesoft.hunt.core.model.SkillSummary
import ca.kallistesoft.hunt.core.model.skillPools
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class FieldSkillsUiState(
    val loading: Boolean = true,
    val skills: List<SkillSummary> = emptyList(),
    /** Missed field skill questions ("My mistakes"). */
    val mistakes: Int = 0,
) {
    /** Every skill mastered: the mix becomes "Keep your edge". */
    val allMastered: Boolean get() = skills.isNotEmpty() && skills.all { it.mastered }

    /** Questions from mastered skills kept in today's mix. */
    val keptInMix: Int get() = if (allMastered) 0 else minOf(FieldTraining.KEEP_SHARP, skills.count { it.mastered })
}

/** Field skills (P11): today's mix and each skill's readiness, with mastered and faded skills. */
@HiltViewModel
class FieldSkillsViewModel @Inject constructor(
    contentRepository: ContentRepository,
    userData: UserDataRepository,
    today: Today,
) : ViewModel() {
    val uiState: StateFlow<FieldSkillsUiState> = combine(contentRepository.content, userData.progress) { content, progress ->
        val day = today()
        val pools = content.skillPools(FieldSkill.forWeapons(progress.weapons), progress.provinces)
            .filterValues { it.isNotEmpty() }
        FieldSkillsUiState(
            loading = false,
            skills = pools.map { (skill, pool) -> progress.skillSummary(skill, pool, day) },
            mistakes = pools.values.flatten().distinctBy { it.id }.count { it.id in progress.weak },
        )
    }.flowOn(Dispatchers.Default) // readiness over every question: off the main thread
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FieldSkillsUiState())
}
