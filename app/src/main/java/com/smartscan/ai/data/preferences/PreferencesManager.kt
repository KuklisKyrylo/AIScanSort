package com.smartscan.ai.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smartscan.ai.domain.model.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val LANGUAGE = stringPreferencesKey("app_language")
    }

    val languageFlow: Flow<AppLanguage> = context.dataStore.data.map { preferences ->
        val code = preferences[Keys.LANGUAGE] ?: AppLanguage.ENGLISH.code
        AppLanguage.fromCode(code)
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[Keys.LANGUAGE] = language.code
        }
    }
}

