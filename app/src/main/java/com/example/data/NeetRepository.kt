package com.example.data

import kotlinx.coroutines.flow.Flow

class NeetRepository(
    private val studySessionDao: StudySessionDao,
    private val mockTestDao: MockTestDao,
    private val syllabusDao: SyllabusDao,
    private val goalsPreferences: GoalsPreferences,
    private val timerPreferences: TimerPreferences? = null
) {
    val sessionsFlow: Flow<List<StudySessionEntity>> = studySessionDao.observeAllSessions()
    val mockTestsFlow: Flow<List<MockTestEntity>> = mockTestDao.observeAllTests()
    val syllabusFlow: Flow<List<SyllabusChapterEntity>> = syllabusDao.observeAllChapters()
    val goalsFlow: Flow<AspirantGoals> = goalsPreferences.goalsFlow

    suspend fun readTimerSnapshot(): PersistedTimerSnapshot? = timerPreferences?.readSnapshot()

    suspend fun saveTimerSnapshot(
        startTime: Long,
        accumulatedTime: Long,
        isRunning: Boolean,
        subjectName: String,
        chapterName: String,
        modeName: String
    ) {
        timerPreferences?.saveTimerState(
            startTime = startTime,
            accumulatedTime = accumulatedTime,
            isRunning = isRunning,
            subjectName = subjectName,
            chapterName = chapterName,
            modeName = modeName
        )
    }

    suspend fun ensureSyllabusSeeded() {
        if (syllabusDao.getChapterCount() == 0) {
            syllabusDao.insertChapters(NeetSyllabusSeed.initialChapters())
        }
    }

    suspend fun logStudySession(session: StudySessionEntity) {
        studySessionDao.insertSession(session)
    }

    suspend fun deleteStudySession(id: Long) {
        studySessionDao.deleteSessionById(id)
    }

    suspend fun addMockTest(test: MockTestEntity) {
        mockTestDao.insertMockTest(test)
    }

    suspend fun deleteMockTest(id: Long) {
        mockTestDao.deleteMockTestById(id)
    }

    suspend fun updateChapter(chapter: SyllabusChapterEntity) {
        syllabusDao.updateChapter(chapter)
    }

    suspend fun updateGoals(
        targetScore: Int,
        physicsGoalHours: Float,
        chemistryGoalHours: Float,
        biologyGoalHours: Float,
        examDateMillis: Long,
        themeMode: String
    ) {
        goalsPreferences.updateGoals(
            targetScore = targetScore,
            physicsGoalHours = physicsGoalHours,
            chemistryGoalHours = chemistryGoalHours,
            biologyGoalHours = biologyGoalHours,
            examDateMillis = examDateMillis,
            themeMode = themeMode
        )
    }

    suspend fun updateSubjectGoals(
        physicsGoalHours: Float,
        chemistryGoalHours: Float,
        biologyGoalHours: Float
    ) {
        goalsPreferences.updateSubjectGoals(
            physicsGoalHours = physicsGoalHours,
            chemistryGoalHours = chemistryGoalHours,
            biologyGoalHours = biologyGoalHours
        )
    }

    suspend fun updateThemeMode(themeMode: String) {
        goalsPreferences.updateThemeMode(themeMode)
    }

    suspend fun resetAllData() {
        studySessionDao.clearAllSessions()
        mockTestDao.clearAllMockTests()
        syllabusDao.resetAllProgress()
    }

    suspend fun loadSampleAnalyticsWeek() {
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L
        val sampleSessions = listOf(
            StudySessionEntity(subjectName = NeetSubject.BOTANY.name, chapterName = "Molecular Basis of Inheritance", durationMinutes = 110, questionsSolved = 60, questionsCorrect = 54, modeName = StudyMode.DEEP_BLOCK.name, notes = "NCERT line-by-line + Lac Operon PYQs", timestampMillis = now - 2 * 3600_000L),
            StudySessionEntity(subjectName = NeetSubject.PHYSICS.name, chapterName = "Current Electricity", durationMinutes = 95, questionsSolved = 40, questionsCorrect = 31, modeName = StudyMode.DEEP_BLOCK.name, notes = "Kirchhoff bridge & potentiometer numericals", timestampMillis = now - 5 * 3600_000L),
            StudySessionEntity(subjectName = NeetSubject.CHEMISTRY.name, chapterName = "General Organic Chemistry (GOC)", durationMinutes = 80, questionsSolved = 45, questionsCorrect = 38, modeName = StudyMode.POMODORO.name, notes = "Resonance & acidity order exceptions", timestampMillis = now - dayMs - 3 * 3600_000L),
            StudySessionEntity(subjectName = NeetSubject.ZOOLOGY.name, chapterName = "Biotechnology: Principles & Apps", durationMinutes = 120, questionsSolved = 70, questionsCorrect = 65, modeName = StudyMode.DEEP_BLOCK.name, notes = "pBR322 restriction sites & PCR cycles", timestampMillis = now - dayMs - 7 * 3600_000L),
            StudySessionEntity(subjectName = NeetSubject.PHYSICS.name, chapterName = "Ray & Wave Optics", durationMinutes = 105, questionsSolved = 35, questionsCorrect = 26, modeName = StudyMode.DEEP_BLOCK.name, notes = "YDSE fringe shift & lens maker formula", timestampMillis = now - 2 * dayMs - 4 * 3600_000L),
            StudySessionEntity(subjectName = NeetSubject.BOTANY.name, chapterName = "Principles of Inheritance & Variation", durationMinutes = 90, questionsSolved = 55, questionsCorrect = 49, modeName = StudyMode.POMODORO.name, notes = "Pedigree analysis & linkage maps", timestampMillis = now - 3 * dayMs - 2 * 3600_000L),
            StudySessionEntity(subjectName = NeetSubject.CHEMISTRY.name, chapterName = "d & f Block & Coordination Comp.", durationMinutes = 115, questionsSolved = 50, questionsCorrect = 44, modeName = StudyMode.DEEP_BLOCK.name, notes = "CFT splitting & IUPAC isomerism", timestampMillis = now - 4 * dayMs - 6 * 3600_000L),
            StudySessionEntity(subjectName = NeetSubject.ZOOLOGY.name, chapterName = "Human Reproduction & Health", durationMinutes = 85, questionsSolved = 50, questionsCorrect = 46, modeName = StudyMode.POMODORO.name, notes = "Gametogenesis hormones chart", timestampMillis = now - 5 * dayMs - 3 * 3600_000L),
            StudySessionEntity(subjectName = NeetSubject.PHYSICS.name, chapterName = "Electrostatics & Capacitance", durationMinutes = 75, questionsSolved = 30, questionsCorrect = 24, modeName = StudyMode.DEEP_BLOCK.name, notes = "Dielectric slab energy loss", timestampMillis = now - 6 * dayMs - 4 * 3600_000L)
        )
        val sampleMocks = listOf(
            MockTestEntity(
                title = "Part Test #1 — Mechanics & GOC",
                testDateMillis = now - 21 * dayMs,
                physicsCorrect = 29, physicsWrong = 10, physicsSkipped = 6,
                chemistryCorrect = 32, chemistryWrong = 8, chemistrySkipped = 5,
                botanyCorrect = 37, botanyWrong = 5, botanySkipped = 3,
                zoologyCorrect = 36, zoologyWrong = 6, zoologySkipped = 3,
                mistakeNotes = "Rushed last 15 Physics MCQs; need to skip tricky Rotational problems first."
            ),
            MockTestEntity(
                title = "Major Test #2 — Class 11 Full",
                testDateMillis = now - 14 * dayMs,
                physicsCorrect = 31, physicsWrong = 8, physicsSkipped = 6,
                chemistryCorrect = 34, chemistryWrong = 6, chemistrySkipped = 5,
                botanyCorrect = 39, botanyWrong = 4, botanySkipped = 2,
                zoologyCorrect = 38, zoologyWrong = 4, zoologySkipped = 3,
                mistakeNotes = "Better negative control in Bio. Review Ionic Equilibrium buffer formulas."
            ),
            MockTestEntity(
                title = "AITS Mock #3 — High-Yield Mix",
                testDateMillis = now - 7 * dayMs,
                physicsCorrect = 34, physicsWrong = 6, physicsSkipped = 5,
                chemistryCorrect = 36, chemistryWrong = 5, chemistrySkipped = 4,
                botanyCorrect = 41, botanyWrong = 2, botanySkipped = 2,
                zoologyCorrect = 40, zoologyWrong = 3, zoologySkipped = 2,
                mistakeNotes = "Crossed 580! Silly sign error in Optics & 2 Inorganic NCERT exceptions."
            ),
            MockTestEntity(
                title = "NTA Full Syllabus Grand Test #4",
                testDateMillis = now - 2 * dayMs,
                physicsCorrect = 36, physicsWrong = 5, physicsSkipped = 4,
                chemistryCorrect = 38, chemistryWrong = 4, chemistrySkipped = 3,
                botanyCorrect = 42, botanyWrong = 2, botanySkipped = 1,
                zoologyCorrect = 42, zoologyWrong = 2, zoologySkipped = 1,
                mistakeNotes = "619/720. Strong Botany/Zoology accuracy (95%). Target 40+ in Physics next."
            )
        )
        studySessionDao.insertSessions(sampleSessions)
        mockTestDao.insertMockTests(sampleMocks)
    }
}
