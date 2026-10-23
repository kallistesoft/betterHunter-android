package ca.kallistesoft.hunt.core.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.ui.graphics.vector.ImageVector
import ca.kallistesoft.hunt.core.model.Province

@get:StringRes
val Province.nameRes: Int
    get() = when (this) {
        Province.QC -> R.string.province_qc
        Province.AB -> R.string.province_ab
        Province.OTHER -> R.string.province_other
    }

/** The quizzes a province adds, in a few words. */
@get:StringRes
val Province.quizzesRes: Int
    get() = when (this) {
        Province.QC -> R.string.province_qc_sub
        Province.AB -> R.string.province_ab_sub
        Province.OTHER -> R.string.province_other_sub
    }

/** Same icons as the province's hunter course ([examIcon]). */
val Province.icon: ImageVector
    get() = when (this) {
        Province.QC -> Icons.Outlined.Forest
        Province.AB -> Icons.Outlined.Landscape
        Province.OTHER -> HuntIcons.Maple
    }
