package ch.schmidlins.mealdiary.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val BM_PROMPT_INTERVAL_HOURS = intPreferencesKey("bm_prompt_interval_hours")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val IS_WEIGHT_TRACKING_ENABLED = booleanPreferencesKey("is_weight_tracking_enabled")
        val WEIGHT_SUGGESTION_DISMISSED = booleanPreferencesKey("weight_suggestion_dismissed")
    }

    val bmPromptIntervalHours: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BM_PROMPT_INTERVAL_HOURS] ?: 24
    }

    val isReminderEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.REMINDER_ENABLED] ?: true
    }

    val isWeightTrackingEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_WEIGHT_TRACKING_ENABLED] ?: false
    }

    val weightSuggestionDismissed: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WEIGHT_SUGGESTION_DISMISSED] ?: false
    }

    suspend fun updateBMPromptInterval(hours: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BM_PROMPT_INTERVAL_HOURS] = hours
        }
    }

    suspend fun updateReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_ENABLED] = enabled
        }
    }

    suspend fun updateWeightTrackingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_WEIGHT_TRACKING_ENABLED] = enabled
        }
    }

    suspend fun dismissWeightSuggestion() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT_SUGGESTION_DISMISSED] = true
        }
    }
}
