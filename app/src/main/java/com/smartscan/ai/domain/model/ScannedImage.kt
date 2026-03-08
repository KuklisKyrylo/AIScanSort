package com.smartscan.ai.domain.model

enum class ScanStatus {
    PENDING,
    PROCESSED,
    FAILED
}

data class ScannedImage(
    val id: Long = 0L,
    val uri: String,
    val extractedText: String,
    val tags: List<String>,
    val scannedAtEpochMillis: Long,
    val photoCreatedAtEpochMillis: Long? = null,
    val status: ScanStatus = ScanStatus.PENDING
)
