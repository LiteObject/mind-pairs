package com.example.mindpairs.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mindpairs.model.GameDifficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first // Added import
import kotlinx.coroutines.flow.map

// Create a DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val DIFFICULTY = intPreferencesKey("selected_difficulty")
        val EASY_BEST_SCORE = intPreferencesKey("easy_best_score")
        val MEDIUM_BEST_SCORE = intPreferencesKey("medium_best_score")
        val HARD_BEST_SCORE = intPreferencesKey("hard_best_score")
    }

    // Flow to observe difficulty changes
    val selectedDifficulty: Flow<GameDifficulty> = context.dataStore.data
        .map { preferences ->
            val difficultyOrdinal = preferences[PreferencesKeys.DIFFICULTY] ?: GameDifficulty.MEDIUM.ordinal
            GameDifficulty.entries.getOrElse(difficultyOrdinal) { GameDifficulty.MEDIUM }
        }

    // Flow to observe best scores
    val bestScores: Flow<Map<GameDifficulty, Int>> = context.dataStore.data
        .map { preferences ->
            mapOf(
                GameDifficulty.EASY to (preferences[PreferencesKeys.EASY_BEST_SCORE] ?: Int.MAX_VALUE),
                GameDifficulty.MEDIUM to (preferences[PreferencesKeys.MEDIUM_BEST_SCORE] ?: Int.MAX_VALUE),
                GameDifficulty.HARD to (preferences[PreferencesKeys.HARD_BEST_SCORE] ?: Int.MAX_VALUE)
            )
        }

    // Save selected difficulty
    suspend fun saveDifficulty(difficulty: GameDifficulty) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DIFFICULTY] = difficulty.ordinal
        }
    }

    // Save best score for specific difficulty
    suspend fun saveBestScore(difficulty: GameDifficulty, score: Int) {
        context.dataStore.edit { preferences ->
            val key = when (difficulty) {
                GameDifficulty.EASY -> PreferencesKeys.EASY_BEST_SCORE
                GameDifficulty.MEDIUM -> PreferencesKeys.MEDIUM_BEST_SCORE
                GameDifficulty.HARD -> PreferencesKeys.HARD_BEST_SCORE
            }
            val currentBest = preferences[key] ?: Int.MAX_VALUE
            if (score < currentBest) {
                preferences[key] = score
            }
        }
    }

    // Get best score for specific difficulty
    suspend fun getBestScore(difficulty: GameDifficulty): Int {
        // Removed intermediate 'preferences' variable and directly chained operators
        return context.dataStore.data.map { prefs ->
            val key = when (difficulty) {
                GameDifficulty.EASY -> PreferencesKeys.EASY_BEST_SCORE
                GameDifficulty.MEDIUM -> PreferencesKeys.MEDIUM_BEST_SCORE
                GameDifficulty.HARD -> PreferencesKeys.HARD_BEST_SCORE
            }
            prefs[key] ?: Int.MAX_VALUE
        }.first() // Changed .collect { it } to .first()
    }
}
