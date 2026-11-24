package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.SquadCodeUtils
import com.example.data.SquadLocalProfile
import com.example.data.SquadMember
import com.example.data.SquadPreferences
import com.example.data.SquadRepository
import com.example.data.SquadRoom
import com.example.data.SquadSortMode
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SquadUiState {
    data object Loading : SquadUiState
    data class NeedsOnboarding(
        val savedDisplayName: String,
        val previousSquads: List<SquadRoom> = emptyList()
    ) : SquadUiState

    data class Active(
        val squad: SquadRoom,
        val localProfile: SquadLocalProfile,
        val sortMode: SquadSortMode
    ) : SquadUiState

    data class Error(val message: String, val savedRoomCode: String = "") : SquadUiState
}

data class SquadActionFeedback(
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
    val statusBanner: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class SquadViewModel(
    private val squadRepository: SquadRepository,
    private val squadPreferences: SquadPreferences,
    private val currentUserId: String
) : ViewModel() {

    private val _sortMode = MutableStateFlow(SquadSortMode.TODAY_HOURS)
    val sortMode: StateFlow<SquadSortMode> = _sortMode.asStateFlow()

    private val _previousSquads = MutableStateFlow<List<SquadRoom>>(emptyList())

    private val _feedback = MutableStateFlow(SquadActionFeedback())
    val feedback: StateFlow<SquadActionFeedback> = _feedback.asStateFlow()

    val uiState: StateFlow<SquadUiState> = squadPreferences.squadProfileFlow
        .flatMapLatest { profile ->
            val code = SquadCodeUtils.normalize(profile.roomCode)
            if (code.isBlank() || !SquadCodeUtils.isValidRoomCode(code)) {
                _previousSquads.map { previous ->
                    SquadUiState.NeedsOnboarding(
                        savedDisplayName = profile.displayName,
                        previousSquads = previous
                    )
                }
            } else {
                combine(
                    squadRepository.observeSquad(code),
                    _sortMode
                ) { room, mode ->
                    if (room == null) {
                        SquadUiState.NeedsOnboarding(
                            savedDisplayName = profile.displayName,
                            previousSquads = _previousSquads.value
                        )
                    } else {
                        SquadUiState.Active(
                            squad = room,
                            localProfile = profile,
                            sortMode = mode
                        )
                    }
                }.catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val rootError = firestoreError ?: error
                    Log.w(TAG, "Error observing squad $code", rootError)
                    emit(
                        SquadUiState.Error(
                            message = rootError.message ?: "Unable to sync Study Squad.",
                            savedRoomCode = code
                        )
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = SquadUiState.Loading
        )

    init {
        refreshJoinedSquads()
    }

    fun refreshJoinedSquads() {
        viewModelScope.launch {
            val result = squadRepository.findJoinedSquads()
            result.onSuccess { list ->
                _previousSquads.value = list
            }
        }
    }

    fun setSortMode(mode: SquadSortMode) {
        _sortMode.value = mode
    }

    fun clearFeedback() {
        _feedback.update { it.copy(errorMessage = null, statusBanner = null) }
    }

    fun createSquad(
        squadName: String,
        displayName: String,
        requestedCode: String?,
        todayMinutes: Int,
        isStudying: Boolean,
        latestMockScore: Int
    ) {
        viewModelScope.launch {
            _feedback.value = SquadActionFeedback(isBusy = true)
            val status = if (isStudying) SquadMember.STATUS_STUDYING else SquadMember.STATUS_IDLE
            val result = squadRepository.createSquad(
                requestedCode = requestedCode,
                squadName = squadName,
                displayName = displayName,
                todayMinutes = todayMinutes,
                status = status,
                latestMockScore = latestMockScore
            )
            result.fold(
                onSuccess = { room ->
                    squadPreferences.saveSquadProfile(
                        displayName = displayName,
                        roomCode = room.roomCode,
                        squadName = room.name
                    )
                    _feedback.value = SquadActionFeedback(
                        statusBanner = "Created ${room.name} (${room.roomCode})"
                    )
                },
                onFailure = { error ->
                    _feedback.value = SquadActionFeedback(
                        errorMessage = error.message ?: "Could not create Study Squad."
                    )
                }
            )
        }
    }

    fun joinSquad(
        roomCode: String,
        displayName: String,
        todayMinutes: Int,
        isStudying: Boolean,
        latestMockScore: Int
    ) {
        viewModelScope.launch {
            _feedback.value = SquadActionFeedback(isBusy = true)
            val status = if (isStudying) SquadMember.STATUS_STUDYING else SquadMember.STATUS_IDLE
            val result = squadRepository.joinSquad(
                roomCode = roomCode,
                displayName = displayName,
                todayMinutes = todayMinutes,
                status = status,
                latestMockScore = latestMockScore
            )
            result.fold(
                onSuccess = { room ->
                    squadPreferences.saveSquadProfile(
                        displayName = displayName,
                        roomCode = room.roomCode,
                        squadName = room.name
                    )
                    _feedback.value = SquadActionFeedback(
                        statusBanner = "Joined ${room.name}"
                    )
                },
                onFailure = { error ->
                    _feedback.value = SquadActionFeedback(
                        errorMessage = error.message ?: "Could not join Study Squad."
                    )
                }
            )
        }
    }

    fun syncStats(
        displayNameOverride: String? = null,
        todayMinutes: Int,
        isStudying: Boolean,
        latestMockScore: Int
    ) {
        viewModelScope.launch {
            val profile = squadPreferences.readSquadProfile()
            val code = SquadCodeUtils.normalize(profile.roomCode)
            if (!SquadCodeUtils.isValidRoomCode(code)) return@launch
            val finalName = (displayNameOverride ?: profile.displayName).trim().ifBlank { "Aspirant" }
            if (displayNameOverride != null && displayNameOverride.isNotBlank()) {
                squadPreferences.updateDisplayName(finalName)
            }
            val status = if (isStudying) SquadMember.STATUS_STUDYING else SquadMember.STATUS_IDLE
            val result = squadRepository.syncMemberProgress(
                roomCode = code,
                displayName = finalName,
                todayMinutes = todayMinutes,
                status = status,
                latestMockScore = latestMockScore
            )
            result.onFailure { error ->
                _feedback.update {
                    it.copy(errorMessage = error.message ?: "Sync failed.")
                }
            }
        }
    }

    fun leaveCurrentSquad() {
        viewModelScope.launch {
            _feedback.value = SquadActionFeedback(isBusy = true)
            val profile = squadPreferences.readSquadProfile()
            val code = SquadCodeUtils.normalize(profile.roomCode)
            val result = squadRepository.leaveSquad(code)
            squadPreferences.clearRoomOnly()
            refreshJoinedSquads()
            result.fold(
                onSuccess = {
                    _feedback.value = SquadActionFeedback(statusBanner = "Left squad $code")
                },
                onFailure = { error ->
                    _feedback.value = SquadActionFeedback(
                        errorMessage = error.message ?: "Could not leave squad cleanly."
                    )
                }
            )
        }
    }

    companion object {
        private const val TAG = "SquadViewModel"

        fun provideFactory(currentUserId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                SquadViewModel(
                    squadRepository = SquadRepository(db),
                    squadPreferences = SquadPreferences(app.applicationContext),
                    currentUserId = currentUserId
                )
            }
        }
    }
}
