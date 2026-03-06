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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartscan.ai.domain.model.ScannedImage

@Composable
fun MainScreenRoute(
    onNavigateToSettings: () -> Unit,
    onImageClick: (Long) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findActivity()
    val mediaPermission = requiredMediaPermission()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onMediaPermissionChanged(granted)
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            mediaPermission
        ) == PackageManager.PERMISSION_GRANTED

        viewModel.onMediaPermissionChanged(granted)
    }

    MainScreen(
        state = state,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onRequestPermission = { permissionLauncher.launch(mediaPermission) },
        onSyncNow = viewModel::onSyncNowClick,
        onBuyProClick = {
            if (activity != null) {
                viewModel.onBuyProClick(activity)
            }
        },
        onNavigateToSettings = onNavigateToSettings,
        onImageClick = onImageClick
    )
}

@Composable
fun MainScreen(
    state: MainUiState,
    onSearchQueryChange: (String) -> Unit,
    onRequestPermission: () -> Unit,
    onSyncNow: () -> Unit,
    onBuyProClick: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onImageClick: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SearchTopBar(
            query = state.searchQuery,
            onQueryChange = onSearchQueryChange,
            onSyncNow = onSyncNow,
            onNavigateToSettings = onNavigateToSettings,
            isSyncing = state.isSyncing,
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
                scannedCount = state.scannedCount,
                onBuyProClick = onBuyProClick,
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

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSyncNow: () -> Unit,
    onNavigateToSettings: () -> Unit,
    isSyncing: Boolean,
    strings: com.smartscan.ai.ui.strings.StringResources
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
                label = { Text(strings.searchHint) }
            )
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = strings.settings
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onSyncNow, enabled = !isSyncing) {
                Text(if (isSyncing) strings.syncing else strings.syncNow)
            }
        }
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
    scannedCount: Int,
    onBuyProClick: () -> Unit,
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
            text = strings.paywallMessage.format(scannedCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onBuyProClick) {
            Text(strings.buyPro)
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
                text = image.uri,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
