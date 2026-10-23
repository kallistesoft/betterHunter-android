package ca.kallistesoft.hunt.feature.home

import androidx.compose.runtime.Composable
import ca.kallistesoft.hunt.core.designsystem.component.BhPreviews
import ca.kallistesoft.hunt.core.designsystem.component.PreviewSurface
import ca.kallistesoft.hunt.core.ui.preview.PreviewData

internal val previewHomeState = HomeUiState(
    loading = false,
    answeredToday = 12,
    streak = 4,
    mainExam = PreviewData.icaf,
    trainingExamCount = 4,
    readiness = 0.45f,
    photoGameId = PreviewData.photoGame.id,
    photoReadiness = 0.4f,
    gameCount = 1,
    animalCount = 66,
    photoOfDay = PhotoOfDayState(PreviewData.photoGame.id, PreviewData.photoQuestion, choice = null),
)

@BhPreviews
@Composable
internal fun HomePreview() = PreviewSurface {
    HomeContent(previewHomeState, {}, {}, {}, {}, { _, _ -> }, {}, {}, {})
}

/** Goal reached, sample-content warning, photo of the day answered wrong. */
@BhPreviews
@Composable
internal fun HomeAnsweredPreview() = PreviewSurface {
    HomeContent(
        previewHomeState.copy(
            hasSampleContent = true,
            answeredToday = 20,
            photoOfDay = previewHomeState.photoOfDay?.copy(choice = 0),
        ),
        {}, {}, {}, {}, { _, _ -> }, {}, {}, {},
    )
}

/** A new hunter just back from "Yes, I passed": the welcome, the goal on "What is it?", ICAF passed. */
@BhPreviews
@Composable
internal fun HomeNewHunterPreview() = PreviewSurface {
    HomeContent(
        previewHomeState.copy(hunter = true, newHunterBanner = true, answeredToday = 5, streak = 6, passedExamId = "ICAF"),
        {}, {}, {}, {}, { _, _ -> }, {}, {}, {},
    )
}

@BhPreviews
@Composable
internal fun PhotoOfDayAnsweredPreview() = PreviewSurface {
    PhotoOfDayCard(previewHomeState.photoOfDay!!.copy(choice = 1), onAnswer = {}, onPlay = {})
}
