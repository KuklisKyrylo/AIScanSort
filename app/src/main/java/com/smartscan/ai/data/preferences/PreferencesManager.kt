package com.smartscan.ai.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smartscan.ai.domain.model.AppLanguage
import com.smartscan.ai.domain.model.SubscriptionType
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
        val SCAN_COUNT = intPreferencesKey("scan_count")
        val SUBSCRIPTION_TYPE = stringPreferencesKey("subscription_type")
        val SUBSCRIPTION_EXPIRY = longPreferencesKey("subscription_expiry")
        val TRIAL_START_DATE = longPreferencesKey("trial_start_date")
    }

    val languageFlow: Flow<AppLanguage> = context.dataStore.data.map { preferences ->
        val code = preferences[Keys.LANGUAGE] ?: AppLanguage.ENGLISH.code
        AppLanguage.fromCode(code)
    }

    val scanCountFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[Keys.SCAN_COUNT] ?: 0
    }

    val subscriptionTypeFlow: Flow<SubscriptionType> = context.dataStore.data.map { preferences ->
        val type = preferences[Keys.SUBSCRIPTION_TYPE] ?: SubscriptionType.FREE.name
        SubscriptionType.valueOf(type)
    }

    val subscriptionExpiryFlow: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[Keys.SUBSCRIPTION_EXPIRY]
    }

    val trialStartDateFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[Keys.TRIAL_START_DATE] ?: System.currentTimeMillis()
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { preferences ->
            preferences[Keys.LANGUAGE] = language.code
        }
    }

    suspend fun addScanCount() {
        context.dataStore.edit { preferences ->
            val current = preferences[Keys.SCAN_COUNT] ?: 0
            preferences[Keys.SCAN_COUNT] = current + 1
        }
    }

    suspend fun setScanCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SCAN_COUNT] = count
        }
    }

    suspend fun setSubscriptionType(type: SubscriptionType, expiryDateMillis: Long? = null) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SUBSCRIPTION_TYPE] = type.name
            if (expiryDateMillis != null) {
                preferences[Keys.SUBSCRIPTION_EXPIRY] = expiryDateMillis
            } else {
                preferences.remove(Keys.SUBSCRIPTION_EXPIRY)
            }
        }
    }

    suspend fun initializeTrialDate() {
        context.dataStore.edit { preferences ->
            if (!preferences.contains(Keys.TRIAL_START_DATE)) {
                preferences[Keys.TRIAL_START_DATE] = System.currentTimeMillis()
            }
        }
    }

    suspend fun resetTrialPeriod() {
        context.dataStore.edit { preferences ->
            preferences[Keys.SCAN_COUNT] = 0
            preferences[Keys.SUBSCRIPTION_TYPE] = SubscriptionType.FREE.name
            preferences.remove(Keys.SUBSCRIPTION_EXPIRY)
        }
    }
}

