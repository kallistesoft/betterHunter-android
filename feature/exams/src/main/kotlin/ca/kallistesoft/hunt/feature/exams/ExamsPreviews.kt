package ca.kallistesoft.hunt.feature.exams

import androidx.compose.runtime.Composable
import ca.kallistesoft.hunt.core.designsystem.component.BhPreviews
import ca.kallistesoft.hunt.core.designsystem.component.PreviewSurface
import ca.kallistesoft.hunt.core.model.MOCK_COUNT
import ca.kallistesoft.hunt.core.model.MockResult
import ca.kallistesoft.hunt.core.model.Province
import ca.kallistesoft.hunt.core.ui.preview.PreviewData

internal val previewExamsState = ExamsUiState(
    mine = listOf(PreviewData.icafSummary.copy(readiness = 0.48f, weakCount = 5, mocksPassed = 3, mocksDone = 5), PreviewData.palSummary),
    otherProvinces = listOf(Province.AB, Province.OTHER),
    games = listOf(PreviewData.photoGameSummary),
    loading = false,
)

@BhPreviews
@Composable
internal fun ExamTrainingPreview() = PreviewSurface {
    ExamTrainingContent(previewExamsState, onBack = {}, onOpenProvinces = {}, onStartQuiz = { _, _ -> }, onOpenMocks = {}, onPassedRealExam = {})
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
        onOpenMocks = {},
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
    PassedExamContent(onConfirm = {}, onBack = {})
}

@BhPreviews
@Composable
internal fun RecognitionPreview() = PreviewSurface {
    RecognitionContent(previewExamsState, onBack = {}, onStartQuiz = { _, _ -> })
}

/** Five mocks done, three passed. */
internal val previewMocksState = MockExamsUiState(
    exam = PreviewData.icaf,
    mocks = (1..MOCK_COUNT).map { n ->
        val result = listOf(88, 72, 84, 64, 92).getOrNull(n - 1)?.let { MockResult(best = it, last = it - (n % 2) * 8) }
        MockRow(n, result, passed = result != null && result.best >= 80)
    },
    loading = false,
)

@BhPreviews
@Composable
internal fun MockExamsPreview() = PreviewSurface {
    MockExamsContent(previewMocksState, onBack = {}, onStart = {}, onUnlock = {}, onDismissOffline = {})
}

/** All ten done: "Reset all". */
@BhPreviews
@Composable
internal fun MockExamsAllDonePreview() = PreviewSurface {
    MockExamsContent(
        previewMocksState.copy(
            mocks = (1..MOCK_COUNT).map { n -> MockRow(n, MockResult(best = 70 + n * 2, last = 68 + n * 2), passed = 70 + n * 2 >= 80) },
        ),
        onBack = {},
        onStart = {},
        onUnlock = {},
        onDismissOffline = {},
    )
}
