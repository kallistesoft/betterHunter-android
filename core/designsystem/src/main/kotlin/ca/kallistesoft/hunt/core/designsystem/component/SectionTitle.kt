package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import ca.kallistesoft.hunt.core.designsystem.theme.BhTextStyles
import ca.kallistesoft.hunt.core.designsystem.theme.Spacing

/** Small uppercase label ("TODAY", "HOW TO RECOGNISE IT"), orange by default. */
@Composable
fun Overline(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.tertiary) {
    Text(text.uppercase(), modifier, style = BhTextStyles.overline, color = color)
}

/** Section label of a list, with an optional action on the right. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, action: @Composable RowScope.() -> Unit = {}) {
    Row(modifier.padding(top = Spacing.l, bottom = Spacing.s), verticalAlignment = Alignment.CenterVertically) {
        Overline(text, Modifier.weight(1f).semantics { heading() })
        action()
    }
}
