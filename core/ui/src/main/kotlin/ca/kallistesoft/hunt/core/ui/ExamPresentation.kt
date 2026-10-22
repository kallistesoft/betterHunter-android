package ca.kallistesoft.hunt.core.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.ui.graphics.vector.ImageVector
import ca.kallistesoft.hunt.core.model.ExamGroup

fun examIcon(examId: String): ImageVector = when (examId) {
    "PAL" -> Icons.Outlined.Shield
    "CRFSC" -> Icons.Outlined.Lock
    "ICAF" -> Icons.Outlined.Forest
    "ABHE" -> Icons.Outlined.Landscape
    "ID" -> Icons.Outlined.PhotoCamera
    else -> Icons.Outlined.School
}

@get:StringRes
val ExamGroup.titleRes: Int
    get() = when (this) {
        ExamGroup.FIREARMS -> R.string.group_firearms
        ExamGroup.HUNTING -> R.string.group_hunting
        ExamGroup.IDENTIFICATION -> R.string.group_id
    }
