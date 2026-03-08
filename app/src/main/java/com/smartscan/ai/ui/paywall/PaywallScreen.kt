package com.smartscan.ai.ui.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smartscan.ai.ui.strings.StringResources

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    strings: StringResources,
    onNavigateBack: () -> Unit,
    onMonthlyClick: () -> Unit,
    onLifetimeClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onRestoreClick: () -> Unit,
    isBillingInProgress: Boolean,
    billingMessage: String,
    monthlyPrice: String,
    lifetimePrice: String
) {
    val resolvedMonthlyPrice = monthlyPrice.ifBlank { "-" }
    val resolvedLifetimePrice = lifetimePrice.ifBlank { "-" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.paywallTitle) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hero Section
            Text(
                text = strings.paywallTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = strings.paywallSubtitle,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Features
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.large),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureItem("🔓 ${strings.paywallFeatureUnlimited}")
                    FeatureItem("🏷️ ${strings.paywallFeatureNoWatermarks}")
                    FeatureItem("⚡ ${strings.paywallFeatureFaster}")
                    FeatureItem("☁️ ${strings.paywallFeatureCloud}")
                }
            }

            // Pricing Options
            PricingCard(
                title = "📅 ${strings.paywallMonthlyTitle}",
                subtitle = strings.paywallMonthlySubtitle,
                price = resolvedMonthlyPrice,
                period = strings.paywallMonthlyPeriod,
                features = listOf(strings.paywallMonthlyFeatureA, strings.paywallMonthlyFeatureB),
                onClick = onMonthlyClick,
                enabled = !isBillingInProgress,
                isPrimary = false,
                strings = strings
            )

            PricingCard(
                title = "🎁 ${strings.paywallLifetimeTitle}",
                subtitle = strings.paywallLifetimeSubtitle,
                price = resolvedLifetimePrice,
                period = strings.paywallLifetimePeriod,
                features = listOf(strings.paywallLifetimeFeatureA, strings.paywallLifetimeFeatureB),
                onClick = onLifetimeClick,
                enabled = !isBillingInProgress,
                isPrimary = true,
                strings = strings
            )

            if (isBillingInProgress) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                }
            }

            if (billingMessage.isNotBlank()) {
                Text(
                    text = billingMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Legal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onTermsClick) {
                        Text(strings.paywallTerms, style = MaterialTheme.typography.labelSmall)
                    }
                    Text(" • ", style = MaterialTheme.typography.labelSmall)
                    TextButton(onClick = onPrivacyClick) {
                        Text(strings.paywallPrivacy, style = MaterialTheme.typography.labelSmall)
                    }
                    Text(" • ", style = MaterialTheme.typography.labelSmall)
                    TextButton(onClick = onRestoreClick, enabled = !isBillingInProgress) {
                        Text(strings.paywallRestore, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Text(
                    text = strings.paywallPriceNote,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FeatureItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun PricingCard(
    title: String,
    subtitle: String,
    price: String,
    period: String,
    features: List<String>,
    onClick: () -> Unit,
    enabled: Boolean,
    isPrimary: Boolean,
    strings: StringResources
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isPrimary) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.surface
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isPrimary) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = period,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                features.forEach { feature ->
                    Text(
                        text = "✓ $feature",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isPrimary) {
                Button(
                    onClick = onClick,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.paywallChooseLifetime)
                }
            } else {
                OutlinedButton(
                    onClick = onClick,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.paywallTryMonthly)
                }
            }
        }
    }
}
