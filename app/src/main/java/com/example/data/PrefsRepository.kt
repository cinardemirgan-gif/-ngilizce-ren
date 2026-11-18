package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.lingoDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "lingobuddy_prefs"
)

data class UserPrefsSnapshot(
    val selectedGrade: Int? = null,
    val isDarkTheme: Boolean = false,
    val totalScore: Int = 0,
    val bestStreak: Int = 0
)

class PrefsRepository(private val context: Context) {

    private object Keys {
        val SELECTED_GRADE = intPreferencesKey("selected_grade")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val TOTAL_SCORE = intPreferencesKey("total_score")
        val BEST_STREAK = intPreferencesKey("best_streak")
    }

    val prefsFlow: Flow<UserPrefsSnapshot> = context.lingoDataStore.data.map { prefs ->
        UserPrefsSnapshot(
            selectedGrade = prefs[Keys.SELECTED_GRADE],
            isDarkTheme = prefs[Keys.IS_DARK_THEME] ?: false,
            totalScore = prefs[Keys.TOTAL_SCORE] ?: 0,
            bestStreak = prefs[Keys.BEST_STREAK] ?: 0
        )
    }

    suspend fun setSelectedGrade(grade: Int) {
        context.lingoDataStore.edit { prefs ->
            prefs[Keys.SELECTED_GRADE] = grade.coerceIn(1, 8)
        }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.lingoDataStore.edit { prefs ->
            prefs[Keys.IS_DARK_THEME] = isDark
        }
    }

    suspend fun updateScoreAndBestStreak(totalScore: Int, bestStreak: Int) {
        context.lingoDataStore.edit { prefs ->
            prefs[Keys.TOTAL_SCORE] = totalScore
            val currentBest = prefs[Keys.BEST_STREAK] ?: 0
            if (bestStreak > currentBest) {
                prefs[Keys.BEST_STREAK] = bestStreak
            }
        }
    }
}
