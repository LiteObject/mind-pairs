package com.example.mindpairs.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mindpairs.model.GameDifficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create a DataStore instance, tied to the Context's lifecycle
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val SELECTED_DIFFICULTY = stringPreferencesKey("selected_difficulty")
    }

    val difficultyPreferenceFlow: Flow<GameDifficulty> = context.dataStore.data
        .map { preferences ->
            val difficultyName = preferences[PreferencesKeys.SELECTED_DIFFICULTY] ?: GameDifficulty.MEDIUM.name
            try {
                GameDifficulty.valueOf(difficultyName)
            } catch (e: IllegalArgumentException) {
                GameDifficulty.MEDIUM // Default fallback
            }
        }

    suspend fun saveDifficultyPreference(difficulty: GameDifficulty) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_DIFFICULTY] = difficulty.name
        }
    }
}
