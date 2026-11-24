package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.timerDataStore: DataStore<Preferences> by preferencesDataStore(name = "neet_timer_state")

data class PersistedTimerSnapshot(
    val startTime: Long = 0L,
    val accumulatedTime: Long = 0L,
    val isRunning: Boolean = false,
    val subjectName: String = NeetSubject.BOTANY.name,
    val chapterName: String = "Molecular Basis of Inheritance",
    val modeName: String = StudyMode.STOPWATCH.name
)

class TimerPreferences(private val context: Context) {
    private object Keys {
        val START_TIME = longPreferencesKey("start_time")
        val ACCUMULATED_TIME = longPreferencesKey("accumulated_time")
        val IS_RUNNING = booleanPreferencesKey("is_running")
        val SUBJECT_NAME = stringPreferencesKey("subject_name")
        val CHAPTER_NAME = stringPreferencesKey("chapter_name")
        val MODE_NAME = stringPreferencesKey("mode_name")
    }

    suspend fun readSnapshot(): PersistedTimerSnapshot {
        val prefs = context.timerDataStore.data.first()
        return PersistedTimerSnapshot(
            startTime = prefs[Keys.START_TIME] ?: 0L,
            accumulatedTime = prefs[Keys.ACCUMULATED_TIME] ?: 0L,
            isRunning = prefs[Keys.IS_RUNNING] ?: false,
            subjectName = prefs[Keys.SUBJECT_NAME] ?: NeetSubject.BOTANY.name,
            chapterName = prefs[Keys.CHAPTER_NAME] ?: "Molecular Basis of Inheritance",
            modeName = prefs[Keys.MODE_NAME] ?: StudyMode.STOPWATCH.name
        )
    }

    suspend fun saveTimerState(
        startTime: Long,
        accumulatedTime: Long,
        isRunning: Boolean,
        subjectName: String,
        chapterName: String,
        modeName: String
    ) {
        context.timerDataStore.edit { prefs ->
            prefs[Keys.START_TIME] = startTime
            prefs[Keys.ACCUMULATED_TIME] = accumulatedTime.coerceAtLeast(0L)
            prefs[Keys.IS_RUNNING] = isRunning
            prefs[Keys.SUBJECT_NAME] = subjectName
            prefs[Keys.CHAPTER_NAME] = chapterName
            prefs[Keys.MODE_NAME] = modeName
        }
    }

    suspend fun clearRunningTimer(
        subjectName: String,
        chapterName: String,
        modeName: String
    ) {
        saveTimerState(
            startTime = 0L,
            accumulatedTime = 0L,
            isRunning = false,
            subjectName = subjectName,
            chapterName = chapterName,
            modeName = modeName
        )
    }
}
