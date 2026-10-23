package ca.kallistesoft.hunt.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.ui.graphics.vector.ImageVector

fun examIcon(examId: String): ImageVector = when (examId) {
    "PAL" -> Icons.Outlined.Shield
    "ICAF" -> Icons.Outlined.Forest
    "ABHE" -> Icons.Outlined.Landscape
    "ID" -> Icons.Outlined.PhotoCamera
    else -> Icons.Outlined.School
}
