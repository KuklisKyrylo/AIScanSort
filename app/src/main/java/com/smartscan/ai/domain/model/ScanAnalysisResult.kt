package com.smartscan.ai.domain.model

data class ScanAnalysisResult(
    val text: String,
    val tags: List<String>,
    val detectedLanguage: String? = null,  // ISO 639-1 code (e.g., "en", "ru", "zh")
    val isLanguageSupported: Boolean = true  // false if detected but not supported by app
)

// Supported languages by the app
val SUPPORTED_LANGUAGES = setOf("en", "ru", "es", "de", "zh")

fun String?.getLanguageName(): String = when (this) {
    "en" -> "English"
    "ru" -> "Русский"
    "es" -> "Español"
    "de" -> "Deutsch"
    "zh" -> "中文"
    else -> this?.uppercase() ?: "Unknown"
}
