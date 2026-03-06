package com.smartscan.ai.ui.detail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ImageDetailScreenRoute(
    imageId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ImageDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    ImageDetailScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onDeleteClick = { viewModel.deleteImage(imageId, onNavigateBack) },
        onShareClick = {
            state.image?.let { image ->
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Scanned text:\n\n${image.extractedText}")
                }
                context.startActivity(Intent.createChooser(shareIntent, state.strings.share))
            }
        },
        onLoadImage = { viewModel.loadImage(imageId) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailScreen(
    state: ImageDetailUiState,
    onNavigateBack: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    onLoadImage: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (state.image == null && !state.isLoading) {
        onLoadImage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.strings.imageDetails) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = state.strings.back
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = state.strings.share
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = state.strings.delete
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.image != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Image preview
                    state.bitmap?.let { bitmap ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = state.strings.imagePreview,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    // Extracted text
                    DetailSection(
                        title = state.strings.extractedText,
                        content = state.image.extractedText.ifBlank { state.strings.noTextRecognized }
                    )

                    // Tags
                    if (state.image.tags.isNotEmpty()) {
                        DetailSection(
                            title = state.strings.tags,
                            content = state.image.tags.joinToString(", ")
                        )
                    }

                    // Metadata
                    DetailSection(
                        title = state.strings.metadata,
                        content = buildMetadataText(state.image, state.strings, state.photoCreatedAtEpochMillis)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(state.strings.confirmDelete) },
            text = { Text(state.strings.deleteMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text(state.strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(state.strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun DetailSection(title: String, content: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

private fun buildMetadataText(image: com.smartscan.ai.domain.model.ScannedImage, strings: com.smartscan.ai.ui.strings.StringResources, photoCreatedAt: Long?): String {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val scannedDate = dateFormat.format(Date(image.scannedAtEpochMillis))

    return buildString {
        if (photoCreatedAt != null) {
            val createdDate = dateFormat.format(Date(photoCreatedAt))
            append("${strings.photoCreatedAt}: $createdDate\n")
        }
        append("${strings.scannedAt}: $scannedDate")
    }
}
