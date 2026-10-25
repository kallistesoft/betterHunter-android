package ca.kallistesoft.hunt.feature.quiz

import androidx.compose.runtime.Composable
import ca.kallistesoft.hunt.core.designsystem.component.BhPreviews
import ca.kallistesoft.hunt.core.designsystem.component.PreviewSurface
import ca.kallistesoft.hunt.core.model.QuizMode
import ca.kallistesoft.hunt.core.model.QuizSession
import ca.kallistesoft.hunt.core.ui.preview.PreviewData

private val practiceSession = QuizSession(
    exam = PreviewData.photoGame,
    mode = QuizMode.PRACTICE,
    questions = listOf(PreviewData.photoQuestion, PreviewData.question),
)

/** Photo question answered wrong: the animal's card is shown under the photo. */
@BhPreviews
@Composable
internal fun QuizPracticeFeedbackPreview() = PreviewSurface {
    QuestionContent(
        QuizUiState.Running(practiceSession.answer(0), secondsLeft = null),
        canReport = true,
        onChoose = {},
        onNext = {},
        onClose = {},
    )
}

/** Mock exam: an answer selected, no feedback, the timer under five minutes. */
@BhPreviews
@Composable
internal fun QuizExamPreview() = PreviewSurface {
    QuestionContent(
        QuizUiState.Running(
            QuizSession(PreviewData.pal, QuizMode.EXAM, listOf(PreviewData.question, PreviewData.question)).answer(2),
            secondsLeft = 4 * 60 + 7,
        ),
        canReport = true,
        onChoose = {},
        onNext = {},
        onClose = {},
    )
}

@BhPreviews
@Composable
internal fun QuizResultFailedPreview() = PreviewSurface {
    val session = QuizSession(PreviewData.pal, QuizMode.EXAM, listOf(PreviewData.question, PreviewData.photoQuestion))
        .answer(1).next().answer(3)
    ResultContent(
        QuizUiState.Finished(session, timeUp = true, secondsLeft = 0),
        dailyGoalReached = false,
        onHome = {},
        onAgain = {},
        mockButtons = MockButtons(onAllMocks = {}, onNext = null),
    )
}

@BhPreviews
@Composable
internal fun QuizResultPassedPreview() = PreviewSurface {
    val session = QuizSession(PreviewData.pal, QuizMode.EXAM, listOf(PreviewData.question)).answer(1)
    ResultContent(
        QuizUiState.Finished(session, timeUp = false, secondsLeft = 17 * 60 + 42),
        dailyGoalReached = false,
        onHome = {},
        onAgain = {},
        mockButtons = MockButtons(onAllMocks = {}, onNext = {}),
    )
}

/** Practice on a hunter course: the daily goal reached, one mistake. */
@BhPreviews
@Composable
internal fun PracticeResultPreview() = PreviewSurface {
    val session = QuizSession(PreviewData.icaf, QuizMode.PRACTICE, listOf(PreviewData.question, PreviewData.question.copy(id = "q2")))
        .answer(1).next().answer(0)
    ResultContent(QuizUiState.Finished(session, timeUp = false), dailyGoalReached = true, onHome = {}, onAgain = {})
}

@BhPreviews
@Composable
internal fun NothingToReviewPreview() = PreviewSurface {
    EmptyQuiz(QuizUiState.Empty(QuizMode.REVIEW, PreviewData.icaf), onClose = {}, onStartPractice = {})
}
