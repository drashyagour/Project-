package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlin.random.Random

enum class SquadSortMode(val label: String) {
    TODAY_HOURS("Today's Hours"),
    MOCK_SCORE("Mock Score")
}

@Keep
@IgnoreExtraProperties
data class SquadMember(
    val uid: String = "",
    val displayName: String = "",
    val todayMinutes: Long = 0L,
    val status: String = STATUS_IDLE,
    val latestMockScore: Long = 0L,
    val updatedAtMillis: Long = 0L
) {
    @get:Exclude
    val isStudying: Boolean
        get() = status == STATUS_STUDYING

    fun toMap(): Map<String, Any> = mapOf(
        "uid" to uid,
        "displayName" to displayName.trim().take(30),
        "todayMinutes" to todayMinutes.coerceIn(0L, 1440L),
        "status" to if (status == STATUS_STUDYING) STATUS_STUDYING else STATUS_IDLE,
        "latestMockScore" to latestMockScore.coerceIn(0L, 720L),
        "updatedAtMillis" to updatedAtMillis.coerceAtLeast(0L)
    )

    companion object {
        const val STATUS_STUDYING = "Studying"
        const val STATUS_IDLE = "Idle"
    }
}

@Keep
@IgnoreExtraProperties
data class SquadRoom(
    @DocumentId val id: String = "",
    val roomCode: String = "",
    val name: String = "",
    val creatorUid: String = "",
    val memberUids: List<String> = emptyList(),
    val members: Map<String, SquadMember> = emptyMap(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun sortedMembers(sortMode: SquadSortMode): List<SquadMember> {
        val list = members.values.toList()
        return when (sortMode) {
            SquadSortMode.TODAY_HOURS -> list.sortedWith(
                compareByDescending<SquadMember> { it.todayMinutes }
                    .thenByDescending { it.isStudying }
                    .thenByDescending { it.latestMockScore }
                    .thenBy { it.displayName.lowercase() }
            )
            SquadSortMode.MOCK_SCORE -> list.sortedWith(
                compareByDescending<SquadMember> { it.latestMockScore }
                    .thenByDescending { it.todayMinutes }
                    .thenByDescending { it.isStudying }
                    .thenBy { it.displayName.lowercase() }
            )
        }
    }
}

data class SquadLocalProfile(
    val displayName: String = "",
    val roomCode: String = "",
    val squadName: String = ""
)

object SquadCodeUtils {
    private val ROOM_CODE_REGEX = Regex("^[A-Z0-9]{6}$")
    private const val CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    fun normalize(input: String): String = input.trim().uppercase().filter { it.isLetterOrDigit() }.take(6)

    fun isValidRoomCode(code: String): Boolean = ROOM_CODE_REGEX.matches(code)

    fun generateRoomCode(): String {
        val chars = CharArray(6) { CODE_ALPHABET[Random.nextInt(CODE_ALPHABET.length)] }
        return String(chars)
    }
}
