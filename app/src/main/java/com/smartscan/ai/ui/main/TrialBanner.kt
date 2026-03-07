package com.smartscan.ai.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartscan.ai.ui.strings.StringResources

data class TrialBannerModel(
    val title: String,
    val subtitle: String,
    val showUpgrade: Boolean,
    val showProgress: Boolean,
    val progress: Float
)

internal fun buildTrialBannerModel(
    isPremium: Boolean,
    scansUsed: Int,
    scansRemaining: Int,
    scansLimit: Int,
    strings: StringResources
): TrialBannerModel {
    if (isPremium) {
        return TrialBannerModel(
            title = strings.premiumLabel,
            subtitle = strings.unlimitedLabel,
            showUpgrade = false,
            showProgress = false,
            progress = 1f
        )
    }

    val safeLimit = scansLimit.coerceAtLeast(1)
    val safeUsed = scansUsed.coerceAtLeast(0)
    val progress = (safeUsed.toFloat() / safeLimit.toFloat()).coerceIn(0f, 1f)

    return TrialBannerModel(
        title = strings.trialUsage.format(scansUsed, scansLimit),
        subtitle = strings.trialRemaining.format(scansRemaining.coerceAtLeast(0)),
        showUpgrade = true,
        showProgress = true,
        progress = progress
    )
}

@Composable
fun TrialBanner(
    isPremium: Boolean,
    scansUsed: Int,
    scansRemaining: Int,
    scansLimit: Int,
    onUpgradeClick: () -> Unit,
    strings: StringResources
) {
    val model = buildTrialBannerModel(
        isPremium = isPremium,
        scansUsed = scansUsed,
        scansRemaining = scansRemaining,
        scansLimit = scansLimit,
        strings = strings
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = model.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            if (model.showUpgrade) {
                TextButton(
                    onClick = onUpgradeClick,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(strings.upgradeCta)
                }
            }
        }

        if (model.showProgress) {
            LinearProgressIndicator(
                progress = { model.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        }
    }
}
