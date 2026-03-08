package com.smartscan.ai.data.local

import com.smartscan.ai.domain.model.ScanStatus
import com.smartscan.ai.domain.model.ScannedImage

private const val TAG_DELIMITER = "|"

fun ScannedImage.toEntity(): ScanEntity = ScanEntity(
    id = id,
    uri = uri,
    extractedText = extractedText,
    tagsSerialized = tags.joinToString(TAG_DELIMITER),
    scannedAtEpochMillis = scannedAtEpochMillis,
    photoCreatedAtEpochMillis = photoCreatedAtEpochMillis,
    status = status.name
)

fun ScanEntity.toDomain(): ScannedImage = ScannedImage(
    id = id,
    uri = uri,
    extractedText = extractedText,
    tags = if (tagsSerialized.isBlank()) emptyList() else tagsSerialized.split(TAG_DELIMITER),
    scannedAtEpochMillis = scannedAtEpochMillis,
    photoCreatedAtEpochMillis = photoCreatedAtEpochMillis,
    status = runCatching { ScanStatus.valueOf(status) }.getOrDefault(ScanStatus.FAILED)
)
