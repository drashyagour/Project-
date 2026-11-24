package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.goalsDataStore: DataStore<Preferences> by preferencesDataStore(name = "neet_goals")

class GoalsPreferences(private val context: Context) {
    private object Keys {
        val TARGET_SCORE = intPreferencesKey("target_score")
        val DAILY_GOAL_HOURS = floatPreferencesKey("daily_goal_hours")
        val PHYSICS_GOAL_HOURS = floatPreferencesKey("physics_goal_hours")
        val CHEMISTRY_GOAL_HOURS = floatPreferencesKey("chemistry_goal_hours")
        val BIOLOGY_GOAL_HOURS = floatPreferencesKey("biology_goal_hours")
        val EXAM_DATE_MILLIS = longPreferencesKey("exam_date_millis")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val goalsFlow: Flow<AspirantGoals> = context.goalsDataStore.data.map { prefs ->
        val phy = prefs[Keys.PHYSICS_GOAL_HOURS] ?: 2.0f
        val chem = prefs[Keys.CHEMISTRY_GOAL_HOURS] ?: 2.0f
        val bio = prefs[Keys.BIOLOGY_GOAL_HOURS] ?: 4.0f
        val total = prefs[Keys.DAILY_GOAL_HOURS] ?: (phy + chem + bio)
        AspirantGoals(
            targetScore = prefs[Keys.TARGET_SCORE] ?: 650,
            dailyGoalHours = total,
            physicsGoalHours = phy,
            chemistryGoalHours = chem,
            biologyGoalHours = bio,
            examDateMillis = prefs[Keys.EXAM_DATE_MILLIS] ?: AspirantGoals.DEFAULT_NEET_DATE_MILLIS,
            themeMode = prefs[Keys.THEME_MODE] ?: "DARK"
        )
    }

    suspend fun updateGoals(
        targetScore: Int,
        physicsGoalHours: Float,
        chemistryGoalHours: Float,
        biologyGoalHours: Float,
        examDateMillis: Long,
        themeMode: String
    ) {
        val p = physicsGoalHours.coerceIn(0.5f, 8.0f)
        val c = chemistryGoalHours.coerceIn(0.5f, 8.0f)
        val b = biologyGoalHours.coerceIn(0.5f, 10.0f)
        context.goalsDataStore.edit { prefs ->
            prefs[Keys.TARGET_SCORE] = targetScore.coerceIn(100, 720)
            prefs[Keys.PHYSICS_GOAL_HOURS] = p
            prefs[Keys.CHEMISTRY_GOAL_HOURS] = c
            prefs[Keys.BIOLOGY_GOAL_HOURS] = b
            prefs[Keys.DAILY_GOAL_HOURS] = p + c + b
            prefs[Keys.EXAM_DATE_MILLIS] = examDateMillis
            prefs[Keys.THEME_MODE] = themeMode
        }
    }

    suspend fun updateSubjectGoals(
        physicsGoalHours: Float,
        chemistryGoalHours: Float,
        biologyGoalHours: Float
    ) {
        val p = physicsGoalHours.coerceIn(0.5f, 8.0f)
        val c = chemistryGoalHours.coerceIn(0.5f, 8.0f)
        val b = biologyGoalHours.coerceIn(0.5f, 10.0f)
        context.goalsDataStore.edit { prefs ->
            prefs[Keys.PHYSICS_GOAL_HOURS] = p
            prefs[Keys.CHEMISTRY_GOAL_HOURS] = c
            prefs[Keys.BIOLOGY_GOAL_HOURS] = b
            prefs[Keys.DAILY_GOAL_HOURS] = p + c + b
        }
    }

    suspend fun updateThemeMode(themeMode: String) {
        context.goalsDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = themeMode
        }
    }
}
