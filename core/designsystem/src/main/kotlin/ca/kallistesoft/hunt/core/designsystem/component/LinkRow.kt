package ca.kallistesoft.hunt.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import ca.kallistesoft.hunt.core.designsystem.theme.Sizes

/** A row that opens something outside the app (web page, email), marked with the "open in new" icon. */
@Composable
fun LinkRow(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, Modifier.size(Sizes.iconM)) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
    )
}
