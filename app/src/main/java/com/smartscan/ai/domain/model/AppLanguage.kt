package com.smartscan.ai.domain.model

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    RUSSIAN("ru", "Русский"),
    SPANISH("es", "Español"),
    GERMAN("de", "Deutsch");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code == code } ?: ENGLISH
        }
    }
}

