package com.smartscan.ai.ui.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartscan.ai.domain.model.ScannedImage
import com.smartscan.ai.ui.onboarding.OnboardingScreen
import kotlinx.coroutines.delay

@Composable
fun MainScreenRoute(
    onNavigateToSettings: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    onImageClick: (Long) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val showOnboarding by viewModel.showOnboarding.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findActivity()
    val mediaPermission = requiredMediaPermission()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onMediaPermissionChanged(granted)
    }

    // Check permission only once on initial composition
    LaunchedEffect(key1 = viewModel) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            mediaPermission
        ) == PackageManager.PERMISSION_GRANTED

        viewModel.onMediaPermissionChanged(granted)
    }

    if (showOnboarding) {
        OnboardingScreen(
            onComplete = { viewModel.onOnboardingComplete() },
            onSkip = { viewModel.onOnboardingComplete() },
            strings = state.strings
        )
    } else {
        MainScreen(
            state = state,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onRequestPermission = { permissionLauncher.launch(mediaPermission) },
            onSyncNow = viewModel::onSyncNowClick,
            onStopSync = viewModel::onStopSyncClick,
            onContinueSync = viewModel::onContinueSyncClick,
            onRestartSync = viewModel::onRestartSyncClick,
            onClearAllScans = viewModel::onClearAllScansConfirmed,
            onSwitchToAllGalleryAndSync = viewModel::onSwitchToAllGalleryAndSync,
            onToggleScreenshotsOnly = viewModel::onToggleScreenshotsOnly,
            onBuyPremiumClick = {
                if (activity != null) {
                    viewModel.onBuyPremiumClick(activity)
                }
            },
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToPaywall = onNavigateToPaywall,
            onImageClick = onImageClick,
            onToggleEmptyScansFilter = viewModel::toggleEmptyScansFilter
        )
    }
}

@Composable
fun MainScreen(
    state: MainUiState,
    onSearchQueryChange: (String) -> Unit,
    onRequestPermission: () -> Unit,
    onSyncNow: () -> Unit,
    onStopSync: () -> Unit,
    onContinueSync: () -> Unit,
    onRestartSync: () -> Unit,
    onClearAllScans: () -> Unit,
    onSwitchToAllGalleryAndSync: () -> Unit,
    onToggleScreenshotsOnly: (Boolean) -> Unit,
    onBuyPremiumClick: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    onImageClick: (Long) -> Unit,
    onToggleEmptyScansFilter: () -> Unit
) {
    var showClearAllDialog by remember { mutableStateOf(false) }
    var showStartOverDialog by remember { mutableStateOf(false) }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showClearAllDialog = false
                    onClearAllScans()
                }) {
                    Text(state.strings.clearAllScans)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text(state.strings.cancel)
                }
            },
            title = { Text(state.strings.clearAllScansTitle) },
            text = { Text(state.strings.clearAllScansMessage) }
        )
    }

    if (showStartOverDialog) {
        AlertDialog(
            onDismissRequest = { showStartOverDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showStartOverDialog = false
                    onRestartSync()
                }) {
                    Text(state.strings.startOverConfirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartOverDialog = false }) {
                    Text(state.strings.cancel)
                }
            },
            title = { Text(state.strings.startOverConfirmTitle) },
            text = { Text(state.strings.startOverConfirmMessage) }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Show scanning progress or empty state
        when {
            state.isLoading && state.images.isEmpty() -> {
                ScanningInProgressView(
                    scannedCount = state.syncProcessed,
                    isFirstTime = true,
                    strings = state.strings,
                    onStopSync = onStopSync,
                    showStopButton = state.syncPhase == SyncPhase.RUNNING
                )
            }
            state.images.isEmpty() && !state.isLoading -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Show permission banner if needed
                    if (!state.hasMediaPermission) {
                        PermissionRequiredBanner(
                            onRequestPermission = onRequestPermission,
                            strings = state.strings
                        )
                    }

                    // Show screenshots folder empty banner if applicable
                    if (state.syncStatusMessage == state.strings.screenshotsFolderEmpty && state.screenshotsOnly) {
                        ScreenshotsFolderEmptyBanner(
                            onSyncAllGallery = onSwitchToAllGalleryAndSync,
                            onToggleScreenshotsOnly = onToggleScreenshotsOnly,
                            screenshotsOnlyEnabled = state.screenshotsOnly,
                            strings = state.strings
                        )
                    } else {
                        EmptyStateView(
                            onScanClick = onSyncNow,
                            strings = state.strings
                        )
                    }
                }
            }
            else -> {
                // Normal main screen content
                SearchTopBar(
                    query = state.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSyncNow = onSyncNow,
                    onStopSync = onStopSync,
                    onContinueSync = onContinueSync,
                    onRestartSync = { showStartOverDialog = true },
                    onNavigateToSettings = onNavigateToSettings,
                    isSyncing = state.isSyncing,
                    syncPhase = state.syncPhase,
                    strings = state.strings,
                    showEmptyScans = state.showEmptyScans,
                    onToggleEmptyScansFilter = onToggleEmptyScansFilter,
                    onClearAllScansClick = { showClearAllDialog = true },
                    clearAllScansCount = state.scannedCount,
                    canClearAllScans = state.scannedCount > 0,
                    isSyncEnabled = state.isSyncAllowed,
                    sessionDocumentCount = state.sessionDocumentCount,
                    syncStartTimeMs = state.syncStartTimeMs,
                    sessionElapsedSeconds = state.sessionElapsedSeconds,
                    lastSuccessfulSyncTimeMs = state.lastSuccessfulSyncTimeMs
                )

                // Always show subscription status: Trial for free users, Pro for active subscribers.
                TrialBanner(
                    isPremium = state.isPremium,
                    scansUsed = state.trialScansUsed,
                    scansRemaining = state.trialScansRemaining,
                    scansLimit = state.trialScansLimit,
                    onUpgradeClick = onNavigateToPaywall,
                    strings = state.strings
                )

                if (!state.hasMediaPermission) {
                    PermissionRequiredBanner(
                        onRequestPermission = onRequestPermission,
                        strings = state.strings
                    )
                }

                if (state.showPaywall) {
                    PaywallBanner(
                        onBuyPremiumClick = onNavigateToPaywall,
                        strings = state.strings
                    )
                }

                if (state.syncStatusMessage.isNotBlank()) {
                    Text(
                        text = state.syncStatusMessage,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                GalleryGrid(
                    images = state.images,
                    strings = state.strings,
                    onImageClick = onImageClick
                )
            }
        }
    }
}

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSyncNow: () -> Unit,
    onStopSync: () -> Unit,
    onContinueSync: () -> Unit,
    onRestartSync: () -> Unit,
    onNavigateToSettings: () -> Unit,
    isSyncing: Boolean,
    syncPhase: SyncPhase,
    strings: com.smartscan.ai.ui.strings.StringResources,
    showEmptyScans: Boolean,
    onToggleEmptyScansFilter: () -> Unit,
    onClearAllScansClick: () -> Unit,
    clearAllScansCount: Int,
    canClearAllScans: Boolean,
    isSyncEnabled: Boolean,
    sessionDocumentCount: Int = 0,
    syncStartTimeMs: Long = 0L,
    sessionElapsedSeconds: Int = 0,
    lastSuccessfulSyncTimeMs: Long = 0L
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text(strings.searchHint) },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear"
                            )
                        }
                    }
                }
            )
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = strings.settings
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left: compact sync summary block (does not consume full width)
                if (sessionDocumentCount > 0 || lastSuccessfulSyncTimeMs > 0) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        if (sessionDocumentCount > 0) {
                            Card(
                                modifier = Modifier.fillMaxWidth(0.62f),
                                colors = androidx.compose.material3.CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = strings.syncMetricsSummaryTitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = strings.syncMetricsScanned.format(sessionDocumentCount),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    if (sessionDocumentCount > 0) {
                                        val elapsedSeconds = if (isSyncing && syncStartTimeMs > 0) {
                                            // While syncing, show accumulated session time + current active run time.
                                            (sessionElapsedSeconds + ((System.currentTimeMillis() - syncStartTimeMs) / 1000).toInt())
                                                .coerceAtLeast(0)
                                        } else {
                                            sessionElapsedSeconds
                                        }
                                        Text(
                                            text = strings.syncMetricsElapsedTime.format(formatElapsedTime(elapsedSeconds)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                                        )
                                    }
                                }
                            }
                        }

                        if (lastSuccessfulSyncTimeMs > 0) {
                            Text(
                                text = strings.syncMetricsLastSync.format(formatLastSyncTime(lastSuccessfulSyncTimeMs, strings)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Right: main sync controls + running indicator
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        when (syncPhase) {
                            SyncPhase.RUNNING -> {
                                FilledTonalButton(
                                    onClick = onStopSync,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors()
                                ) {
                                    Text(
                                        text = strings.stopSync,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            SyncPhase.PAUSED -> {
                                FilledTonalButton(
                                    onClick = onContinueSync,
                                    enabled = isSyncEnabled,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = strings.continueSync,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                OutlinedButton(
                                    onClick = onRestartSync,
                                    enabled = isSyncEnabled,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = strings.startOver,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            SyncPhase.IDLE -> {
                                Button(
                                    onClick = onSyncNow,
                                    enabled = !isSyncing && isSyncEnabled,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = strings.syncNow,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    if (isSyncing || syncPhase == SyncPhase.RUNNING) {
                        Row(
                            modifier = Modifier.padding(top = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = strings.syncing,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onToggleEmptyScansFilter,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(
                        text = if (showEmptyScans) strings.hideEmptyScans else strings.showEmptyScans,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                FilledTonalButton(
                    onClick = onClearAllScansClick,
                    enabled = canClearAllScans && !isSyncing,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.padding(start = 8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(
                        text = buildClearAllScansLabel(strings.clearAllScans, clearAllScansCount),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

internal fun buildClearAllScansLabel(baseLabel: String, count: Int): String {
    val safeCount = count.coerceAtLeast(0)
    return "$baseLabel ($safeCount)"
}

private fun formatElapsedTime(seconds: Int): String {
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}

private fun formatLastSyncTime(
    timestampMs: Long,
    strings: com.smartscan.ai.ui.strings.StringResources
): String {
    val now = System.currentTimeMillis()
    val diffMs = now - timestampMs
    val diffSeconds = diffMs / 1000

    return when {
        diffSeconds < 60 -> strings.syncMetricsNow
        diffSeconds < 3600 -> strings.syncMetricsMinutesAgo.format(diffSeconds / 60)
        diffSeconds < 86400 -> strings.syncMetricsHoursAgo.format(diffSeconds / 3600)
        else -> strings.syncMetricsDaysAgo.format(diffSeconds / 86400)
    }
}




@Composable
private fun PermissionRequiredBanner(
    onRequestPermission: () -> Unit,
    strings: com.smartscan.ai.ui.strings.StringResources
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = strings.permissionRequired,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onRequestPermission) {
            Text(strings.grantPermission)
        }
    }
}

@Composable
private fun PaywallBanner(
    onBuyPremiumClick: () -> Unit,
    strings: com.smartscan.ai.ui.strings.StringResources
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = strings.paywallMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onBuyPremiumClick) {
            Text(strings.buyPremium)
        }
    }
}

@Composable
private fun GalleryGrid(
    images: List<ScannedImage>,
    strings: com.smartscan.ai.ui.strings.StringResources,
    onImageClick: (Long) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(images, key = { it.id }) { image ->
            GalleryTile(
                image = image,
                noTextMessage = strings.noTextRecognized,
                onClick = { onImageClick(image.id) }
            )
        }
    }
}

@Composable
private fun GalleryTile(image: ScannedImage, noTextMessage: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = image.extractedText.ifBlank { noTextMessage },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (image.tags.isNotEmpty()) {
                Text(
                    text = image.tags.joinToString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun requiredMediaPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

private fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
