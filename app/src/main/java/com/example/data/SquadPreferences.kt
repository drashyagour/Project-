package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.squadDataStore: DataStore<Preferences> by preferencesDataStore(name = "aura_squad_prefs")

class SquadPreferences(private val context: Context) {
    private object Keys {
        val DISPLAY_NAME = stringPreferencesKey("squad_display_name")
        val ROOM_CODE = stringPreferencesKey("squad_room_code")
        val SQUAD_NAME = stringPreferencesKey("squad_name")
    }

    val squadProfileFlow: Flow<SquadLocalProfile> = context.squadDataStore.data.map { prefs ->
        SquadLocalProfile(
            displayName = prefs[Keys.DISPLAY_NAME] ?: "",
            roomCode = prefs[Keys.ROOM_CODE] ?: "",
            squadName = prefs[Keys.SQUAD_NAME] ?: ""
        )
    }

    suspend fun readSquadProfile(): SquadLocalProfile = squadProfileFlow.first()

    suspend fun saveSquadProfile(displayName: String, roomCode: String, squadName: String) {
        context.squadDataStore.edit { prefs ->
            prefs[Keys.DISPLAY_NAME] = displayName.trim().take(30)
            prefs[Keys.ROOM_CODE] = SquadCodeUtils.normalize(roomCode)
            prefs[Keys.SQUAD_NAME] = squadName.trim().take(40)
        }
    }

    suspend fun updateDisplayName(displayName: String) {
        context.squadDataStore.edit { prefs ->
            prefs[Keys.DISPLAY_NAME] = displayName.trim().take(30)
        }
    }

    suspend fun clearRoomOnly() {
        context.squadDataStore.edit { prefs ->
            prefs.remove(Keys.ROOM_CODE)
            prefs.remove(Keys.SQUAD_NAME)
        }
    }
}
