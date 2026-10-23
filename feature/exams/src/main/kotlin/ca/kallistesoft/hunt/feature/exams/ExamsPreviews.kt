package ca.kallistesoft.hunt.feature.exams

import androidx.compose.runtime.Composable
import ca.kallistesoft.hunt.core.designsystem.component.BhPreviews
import ca.kallistesoft.hunt.core.designsystem.component.PreviewSurface
import ca.kallistesoft.hunt.core.ui.preview.PreviewData

internal val previewExamsState = ExamsUiState(
    mine = listOf(PreviewData.palSummary, PreviewData.icafSummary),
    others = listOf(PreviewData.abheSummary),
    games = listOf(PreviewData.photoGameSummary),
    loading = false,
)

@BhPreviews
@Composable
internal fun ExamTrainingPreview() = PreviewSurface {
    ExamTrainingContent(previewExamsState, onBack = {}, onChangeExams = {}, onStartQuiz = { _, _ -> })
}

@BhPreviews
@Composable
internal fun ExamPickerPreview() = PreviewSurface {
    ExamPickerContent(
        state = ExamPickerUiState(
            exams = listOf(
                PickerExam(PreviewData.pal, 303),
                PickerExam(PreviewData.icaf, 228),
                PickerExam(PreviewData.abhe, 285),
            ),
            chosen = setOf("PAL"),
        ),
        firstRun = true,
        onToggle = {},
        onContinue = {},
        onSkip = {},
        onBack = {},
    )
}

@BhPreviews
@Composable
internal fun RecognitionPreview() = PreviewSurface {
    RecognitionContent(previewExamsState, onBack = {}, onStartQuiz = { _, _ -> })
}
