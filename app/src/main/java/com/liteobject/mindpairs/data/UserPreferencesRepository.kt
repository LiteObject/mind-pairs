package com.liteobject.mindpairs.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val EASY_BEST_SCORE = intPreferencesKey("easy_best_score")
        val MEDIUM_BEST_SCORE = intPreferencesKey("medium_best_score")
        val HARD_BEST_SCORE = intPreferencesKey("hard_best_score")
    }

    fun getBestScore(difficulty: String): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            val key = when (difficulty.lowercase()) {
                "easy" -> PreferencesKeys.EASY_BEST_SCORE
                "medium" -> PreferencesKeys.MEDIUM_BEST_SCORE
                "hard" -> PreferencesKeys.HARD_BEST_SCORE
                else -> PreferencesKeys.MEDIUM_BEST_SCORE
            }
            preferences[key] ?: Int.MAX_VALUE
        }
    }

    suspend fun saveBestScore(difficulty: String, score: Int) {
        val key = when (difficulty.lowercase()) {
            "easy" -> PreferencesKeys.EASY_BEST_SCORE
            "medium" -> PreferencesKeys.MEDIUM_BEST_SCORE
            "hard" -> PreferencesKeys.HARD_BEST_SCORE
            else -> PreferencesKeys.MEDIUM_BEST_SCORE
        }

        context.dataStore.edit { preferences ->
            val currentBest = preferences[key] ?: Int.MAX_VALUE
            if (score < currentBest) {
                preferences[key] = score
            }
        }
    }
}
