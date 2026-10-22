package ca.kallistesoft.hunt.core.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.kallistesoft.hunt.core.designsystem.component.BhCard
import ca.kallistesoft.hunt.core.designsystem.component.BhPreviews
import ca.kallistesoft.hunt.core.designsystem.component.PreviewSurface
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing
import ca.kallistesoft.hunt.core.ui.BackTopAppBar
import ca.kallistesoft.hunt.core.ui.ExamCard
import ca.kallistesoft.hunt.core.ui.ExamListItem
import ca.kallistesoft.hunt.core.ui.QuestionPhoto
import ca.kallistesoft.hunt.core.ui.SourceLinks

// Previews of the shared UI components. Internal so the screenshot tests render the same composables.

@BhPreviews
@Composable
internal fun ExamCardPreview() = PreviewSurface {
    Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        ExamCard(PreviewData.palSummary, onPractice = {}, onExam = {}, onReview = {})
        ExamCard(PreviewData.icafSummary.copy(questionCount = 0), onPractice = {}, onExam = {}, onReview = {})
    }
}

@BhPreviews
@Composable
internal fun ExamListItemPreview() = PreviewSurface {
    Column {
        BackTopAppBar("Exam training", onBack = {})
        BhCard(Modifier.padding(Spacing.l)) {
            ExamListItem(PreviewData.crfsc, questionCount = 187)
            ExamListItem(PreviewData.icaf, questionCount = 241, trailing = { Icon(Icons.Rounded.CheckCircle, null) })
        }
    }
}

@BhPreviews
@Composable
internal fun QuestionPhotoPreview() = PreviewSurface {
    Column(Modifier.padding(Spacing.l)) {
        QuestionPhoto(PreviewData.photoQuestion)
        SourceLinks(PreviewData.question)
    }
}
