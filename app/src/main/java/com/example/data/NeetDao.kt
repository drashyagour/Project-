package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY timestampMillis DESC")
    fun observeAllSessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE timestampMillis >= :dayStartMillis ORDER BY timestampMillis DESC")
    fun observeSessionsSince(dayStartMillis: Long): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<StudySessionEntity>)

    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM study_sessions")
    suspend fun clearAllSessions()
}

@Dao
interface MockTestDao {
    @Query("SELECT * FROM mock_tests ORDER BY testDateMillis ASC")
    fun observeAllTests(): Flow<List<MockTestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockTest(test: MockTestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockTests(tests: List<MockTestEntity>)

    @Query("DELETE FROM mock_tests WHERE id = :id")
    suspend fun deleteMockTestById(id: Long)

    @Query("DELETE FROM mock_tests")
    suspend fun clearAllMockTests()
}

@Dao
interface SyllabusDao {
    @Query("SELECT * FROM syllabus_chapters ORDER BY classLevel ASC, weightagePct DESC")
    fun observeAllChapters(): Flow<List<SyllabusChapterEntity>>

    @Query("SELECT COUNT(*) FROM syllabus_chapters")
    suspend fun getChapterCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<SyllabusChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: SyllabusChapterEntity)

    @Query("UPDATE syllabus_chapters SET isCompleted = 0, masteryName = :masteryName, ncertRevisions = 0, confidence = 1, lastRevisedMillis = NULL")
    suspend fun resetAllProgress(masteryName: String = MasteryLevel.NOT_STARTED.name)
}
