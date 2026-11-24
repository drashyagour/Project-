package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SquadRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            throw IllegalStateException("Please sign in with Google before accessing Study Squad.")
        }
        return uid
    }

    fun observeSquad(roomCode: String): Flow<SquadRoom?> = flow {
        val uid = auth.currentUser?.uid
        val normalized = SquadCodeUtils.normalize(roomCode)
        if (uid.isNullOrBlank() || !SquadCodeUtils.isValidRoomCode(normalized)) {
            emit(null)
            return@flow
        }
        val path = "squads/$normalized"
        emitAll(
            db.collection("squads")
                .document(normalized)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else snapshot.toObject(
                        SquadRoom::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun findJoinedSquads(): Result<List<SquadRoom>> {
        val path = "squads"
        return try {
            val uid = requireUserId()
            val snapshot = db.collection("squads")
                .whereArrayContains("memberUids", uid)
                .get()
                .await()
            val rooms = snapshot.toObjects(
                SquadRoom::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(rooms)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun createSquad(
        requestedCode: String?,
        squadName: String,
        displayName: String,
        todayMinutes: Int,
        status: String,
        latestMockScore: Int
    ): Result<SquadRoom> {
        var docPath = "squads"
        return try {
            val uid = requireUserId()
            val cleanSquadName = squadName.trim()
            val cleanDisplayName = displayName.trim()
            require(cleanSquadName.length in 2..40) {
                "Squad name must be between 2 and 40 characters."
            }
            require(cleanDisplayName.length in 1..30) {
                "Display name must be between 1 and 30 characters."
            }

            val code = if (!requestedCode.isNullOrBlank()) {
                SquadCodeUtils.normalize(requestedCode).also {
                    require(SquadCodeUtils.isValidRoomCode(it)) {
                        "Room code must be 6 letters or numbers (e.g. NEET26)."
                    }
                }
            } else {
                var candidate = SquadCodeUtils.generateRoomCode()
                repeat(4) {
                    val existingSnap = db.collection("squads").document(candidate).get().await()
                    if (!existingSnap.exists()) return@repeat
                    candidate = SquadCodeUtils.generateRoomCode()
                }
                candidate
            }

            val docRef = db.collection("squads").document(code)
            docPath = docRef.path

            val existing = docRef.get().await()
            require(!existing.exists()) {
                "Room code $code is already taken. Try joining it or generate another code."
            }

            val nowMillis = System.currentTimeMillis()
            val member = SquadMember(
                uid = uid,
                displayName = cleanDisplayName,
                todayMinutes = todayMinutes.coerceIn(0, 1440).toLong(),
                status = if (status == SquadMember.STATUS_STUDYING) {
                    SquadMember.STATUS_STUDYING
                } else {
                    SquadMember.STATUS_IDLE
                },
                latestMockScore = latestMockScore.coerceIn(0, 720).toLong(),
                updatedAtMillis = nowMillis
            )

            val payload = mapOf(
                "roomCode" to code,
                "name" to cleanSquadName,
                "creatorUid" to uid,
                "memberUids" to listOf(uid),
                "members" to mapOf(uid to member.toMap()),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()

            Result.success(
                SquadRoom(
                    id = code,
                    roomCode = code,
                    name = cleanSquadName,
                    creatorUid = uid,
                    memberUids = listOf(uid),
                    members = mapOf(uid to member)
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun joinSquad(
        roomCode: String,
        displayName: String,
        todayMinutes: Int,
        status: String,
        latestMockScore: Int
    ): Result<SquadRoom> {
        val code = SquadCodeUtils.normalize(roomCode)
        val docPath = "squads/$code"
        return try {
            val uid = requireUserId()
            require(SquadCodeUtils.isValidRoomCode(code)) {
                "Enter a valid 6-character Room Code (letters & numbers)."
            }
            val cleanDisplayName = displayName.trim()
            require(cleanDisplayName.length in 1..30) {
                "Display name must be between 1 and 30 characters."
            }

            val docRef = db.collection("squads").document(code)
            val nowMillis = System.currentTimeMillis()
            val member = SquadMember(
                uid = uid,
                displayName = cleanDisplayName,
                todayMinutes = todayMinutes.coerceIn(0, 1440).toLong(),
                status = if (status == SquadMember.STATUS_STUDYING) {
                    SquadMember.STATUS_STUDYING
                } else {
                    SquadMember.STATUS_IDLE
                },
                latestMockScore = latestMockScore.coerceIn(0, 720).toLong(),
                updatedAtMillis = nowMillis
            )

            val joinedSquad = db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists()) {
                    throw IllegalStateException("No Study Squad found for Room Code $code.")
                }
                val squad = snapshot.toObject(
                    SquadRoom::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ) ?: throw IllegalStateException("Could not read squad $code.")

                val alreadyMember = uid in squad.memberUids
                if (!alreadyMember && squad.memberUids.size >= 25) {
                    throw IllegalStateException("This squad is full (max 25 friends).")
                }

                if (alreadyMember) {
                    transaction.update(
                        docRef,
                        mapOf(
                            "members.$uid" to member.toMap(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    squad.copy(
                        members = squad.members + (uid to member)
                    )
                } else {
                    val updatedUids = squad.memberUids + uid
                    transaction.update(
                        docRef,
                        mapOf(
                            "memberUids" to updatedUids,
                            "members.$uid" to member.toMap(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                    squad.copy(
                        memberUids = updatedUids,
                        members = squad.members + (uid to member)
                    )
                }
            }.await()

            Result.success(joinedSquad)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun syncMemberProgress(
        roomCode: String,
        displayName: String,
        todayMinutes: Int,
        status: String,
        latestMockScore: Int
    ): Result<Unit> {
        val code = SquadCodeUtils.normalize(roomCode)
        val docPath = "squads/$code"
        return try {
            val uid = requireUserId()
            if (!SquadCodeUtils.isValidRoomCode(code)) {
                return Result.failure(IllegalArgumentException("Invalid room code"))
            }
            val cleanDisplayName = displayName.trim().ifBlank { "Aspirant" }.take(30)
            val memberMap = SquadMember(
                uid = uid,
                displayName = cleanDisplayName,
                todayMinutes = todayMinutes.coerceIn(0, 1440).toLong(),
                status = if (status == SquadMember.STATUS_STUDYING) {
                    SquadMember.STATUS_STUDYING
                } else {
                    SquadMember.STATUS_IDLE
                },
                latestMockScore = latestMockScore.coerceIn(0, 720).toLong(),
                updatedAtMillis = System.currentTimeMillis()
            ).toMap()

            db.collection("squads")
                .document(code)
                .update(
                    mapOf(
                        "members.$uid" to memberMap,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun leaveSquad(roomCode: String): Result<Unit> {
        val code = SquadCodeUtils.normalize(roomCode)
        val docPath = "squads/$code"
        return try {
            val uid = requireUserId()
            if (!SquadCodeUtils.isValidRoomCode(code)) {
                return Result.success(Unit)
            }
            val docRef = db.collection("squads").document(code)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists()) return@runTransaction null
                val squad = snapshot.toObject(
                    SquadRoom::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ) ?: return@runTransaction null

                if (uid !in squad.memberUids) return@runTransaction null

                if (squad.memberUids.size <= 1) {
                    transaction.delete(docRef)
                } else {
                    val remainingUids = squad.memberUids.filterNot { it == uid }
                    transaction.update(
                        docRef,
                        mapOf(
                            "memberUids" to remainingUids,
                            "members.$uid" to FieldValue.delete(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
                null
            }.await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }
}
