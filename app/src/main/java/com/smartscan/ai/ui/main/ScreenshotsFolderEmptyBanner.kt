package com.smartscan.ai.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smartscan.ai.ui.strings.StringResources

@Composable
fun ScreenshotsFolderEmptyBanner(
    onSyncAllGallery: () -> Unit,
    onToggleScreenshotsOnly: (Boolean) -> Unit,
    screenshotsOnlyEnabled: Boolean,
    strings: StringResources
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Info icon + title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = strings.screenshotsFolderEmpty,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            // Explanation
            Text(
                text = strings.screenshotsFolderEmptyHint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Start
            )

            Spacer(Modifier.height(4.dp))

            // Toggle switch
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.syncScreenshotsOnlyTitle,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = if (screenshotsOnlyEnabled)
                                strings.currentlyScreenshotsOnly
                            else
                                strings.currentlyAllGallery,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = screenshotsOnlyEnabled,
                        onCheckedChange = onToggleScreenshotsOnly
                    )
                }
            }

            // Action button
            Button(
                onClick = onSyncAllGallery,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (screenshotsOnlyEnabled)
                        strings.switchToAllGallery
                    else
                        strings.syncAllGallery
                )
            }
        }
    }
}

