package ca.kallistesoft.hunt.feature.exams

import androidx.compose.runtime.Composable
import ca.kallistesoft.hunt.core.designsystem.component.BhPreviews
import ca.kallistesoft.hunt.core.designsystem.component.PreviewSurface
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.ui.preview.PreviewData

internal val previewExamsState = ExamsUiState(
    mine = listOf(PreviewData.icafSummary.copy(readiness = 0.48f, weakCount = 5)),
    otherProvinces = listOf(Province.AB, Province.OTHER),
    games = listOf(PreviewData.photoGameSummary),
    loading = false,
)

@BhPreviews
@Composable
internal fun ExamTrainingPreview() = PreviewSurface {
    ExamTrainingContent(previewExamsState, onBack = {}, onOpenProvinces = {}, onStartQuiz = { _, _ -> }, onPassedRealExam = {})
}

/** A Québec hunter who passed ICAF and added Alberta. */
@BhPreviews
@Composable
internal fun ExamTrainingPassedPreview() = PreviewSurface {
    ExamTrainingContent(
        previewExamsState.copy(
            mine = listOf(PreviewData.icafSummary, PreviewData.abheSummary, PreviewData.palSummary),
            passed = setOf("ICAF"),
            otherProvinces = listOf(Province.OTHER),
        ),
        onBack = {},
        onOpenProvinces = {},
        onStartQuiz = { _, _ -> },
        onPassedRealExam = {},
    )
}

@BhPreviews
@Composable
internal fun ProvincesPreview() = PreviewSurface {
    ProvincesContent(
        ProvincesUiState(
            provinces = listOf(
                ProvinceItem(Province.QC, enabled = true, isHome = true, canToggle = false),
                ProvinceItem(Province.AB, enabled = false, isHome = false, canToggle = true),
                ProvinceItem(Province.OTHER, enabled = false, isHome = false, canToggle = true),
            ),
            loading = false,
        ),
        onBack = {},
        onSetEnabled = { _, _ -> },
    )
}

@BhPreviews
@Composable
internal fun PassedExamPreview() = PreviewSurface {
    PassedExamContent("ICAF", onConfirm = {}, onBack = {})
}

@BhPreviews
@Composable
internal fun RecognitionPreview() = PreviewSurface {
    RecognitionContent(previewExamsState, onBack = {}, onStartQuiz = { _, _ -> })
}
