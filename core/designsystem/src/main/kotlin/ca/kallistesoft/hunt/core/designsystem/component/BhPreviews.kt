package ca.kallistesoft.hunt.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ca.kallistesoft.hunt.core.designsystem.theme.BetterHunterTheme

/**
 * Previews every component and screen in the two combinations that catch most problems: light in English and dark
 * in French (longer strings).
 */
@Preview(name = "Light · EN", locale = "en", showBackground = true)
@Preview(name = "Dark · FR", locale = "fr", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class BhPreviews

/** Theme and background for previews and screenshot tests. */
@Composable
fun PreviewSurface(content: @Composable () -> Unit) {
    BetterHunterTheme {
        Surface(color = MaterialTheme.colorScheme.background, content = content)
    }
}
