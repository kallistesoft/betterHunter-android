package ca.kallistesoft.hunt.feature.quiz

import ca.kallistesoft.hunt.core.model.QuizSession

// Choices are shuffled in every run (P14): tests answer from the question on screen, not a fixed index.

/** The right choice of the question on screen. */
internal val QuizSession.right: Int get() = question.answerIndex

/** A wrong choice of the question on screen. */
internal val QuizSession.wrong: Int get() = (question.answerIndex + 1) % question.texts.values.first().choices.size

internal val QuizViewModel.session: QuizSession get() = (uiState.value as QuizUiState.Running).session

internal val FieldQuizViewModel.session: QuizSession get() = (uiState.value as FieldQuizUiState.Running).session
