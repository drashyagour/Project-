package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AspirantGoals
import com.example.data.DailySubjectHours
import com.example.data.DailySubjectMinutes
import com.example.data.MasteryLevel
import com.example.data.MockTestEntity
import com.example.data.NeetRepository
import com.example.data.NeetSubject
import com.example.data.StudyMode
import com.example.data.StudySessionEntity
import com.example.data.SyllabusChapterEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

data class FocusTimerState(
    val selectedSubject: NeetSubject = NeetSubject.BOTANY,
    val selectedChapter: String = "Molecular Basis of Inheritance",
    val selectedMode: StudyMode = StudyMode.STOPWATCH,
    val startTimeMillis: Long = 0L,
    val accumulatedTimeMillis: Long = 0L,
    val elapsedSeconds: Int = 0,
    val isRunning: Boolean = false,
    val lastLoggedBanner: String? = null
) {
    val targetSeconds: Int
        get() = selectedMode.durationMinutes * 60

    val displaySeconds: Int
        get() = if (selectedMode == StudyMode.STOPWATCH) {
            elapsedSeconds
        } else {
            max(0, targetSeconds - elapsedSeconds)
        }

    val progressFraction: Float
        get() = if (selectedMode == StudyMode.STOPWATCH) {
            ((elapsedSeconds % 3600) / 3600f)
        } else if (targetSeconds > 0) {
            (elapsedSeconds.toFloat() / targetSeconds).coerceIn(0f, 1f)
        } else 0f

    val loggedMinutesSoFar: Int
        get() = max(1, (elapsedSeconds + 59) / 60)
}

data class NeetUiState(
    val isLoading: Boolean = true,
    val goals: AspirantGoals = AspirantGoals(),
    val sessions: List<StudySessionEntity> = emptyList(),
    val mockTests: List<MockTestEntity> = emptyList(),
    val chapters: List<SyllabusChapterEntity> = emptyList(),
    val daysToNeet: Int = 0,
    val todayMinutes: Int = 0,
    val todayHours: Float = 0f,
    val todaySubjectHours: DailySubjectHours = DailySubjectHours(),
    val weeklyHours: Float = 0f,
    val weeklyAvgHours: Float = 0f,
    val streakDays: Int = 0,
    val weeklyBreakdown: List<DailySubjectMinutes> = emptyList(),
    val subjectHours7d: Map<NeetSubject, Float> = emptyMap(),
    val totalMcqsSolved: Int = 0,
    val overallMcqAccuracy: Int = 0,
    val latestMock: MockTestEntity? = null,
    val bestMockScore: Int = 0,
    val avgNegativeMarks: Int = 0,
    val worstNegativeSubject: NeetSubject? = null,
    val syllabusReadinessPct: Int = 0,
    val completedTopicsCount: Int = 0,
    val masteredCount: Int = 0,
    val chapterStudyStats: Map<String, Pair<Int, Int>> = emptyMap(),
    val primaryInsight: String = ""
)

class NeetViewModel(private val repository: NeetRepository) : ViewModel() {

    private val _timerState = MutableStateFlow(FocusTimerState())
    val timerState: StateFlow<FocusTimerState> = _timerState.asStateFlow()

    private val _squadSyncSignal = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    val squadSyncSignal: SharedFlow<Unit> = _squadSyncSignal.asSharedFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureSyllabusSeeded()
            restorePersistedTimer()
        }
    }

    val uiState: StateFlow<NeetUiState> = combine(
        repository.sessionsFlow,
        repository.mockTestsFlow,
        repository.syllabusFlow,
        repository.goalsFlow
    ) { sessions, mocks, chapters, goals ->
        buildUiState(sessions, mocks, chapters, goals)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NeetUiState(isLoading = true)
    )

    private fun buildUiState(
        sessions: List<StudySessionEntity>,
        mocks: List<MockTestEntity>,
        chapters: List<SyllabusChapterEntity>,
        goals: AspirantGoals
    ): NeetUiState {
        val now = System.currentTimeMillis()
        val daysToNeet = max(0, ((goals.examDateMillis - now) / 86_400_000L).toInt())

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = cal.timeInMillis
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        val weeklyBreakdown = (6 downTo 0).map { daysAgo ->
            val dayStart = todayStart - daysAgo * 86_400_000L
            val dayEnd = dayStart + 86_400_000L
            val daySessions = sessions.filter { it.timestampMillis in dayStart until dayEnd }
            DailySubjectMinutes(
                dayLabel = if (daysAgo == 0) "Today" else dayFormat.format(Date(dayStart)),
                dayStartMillis = dayStart,
                physicsMinutes = daySessions.filter { it.subject == NeetSubject.PHYSICS }.sumOf { it.durationMinutes },
                chemistryMinutes = daySessions.filter { it.subject == NeetSubject.CHEMISTRY }.sumOf { it.durationMinutes },
                botanyMinutes = daySessions.filter { it.subject == NeetSubject.BOTANY }.sumOf { it.durationMinutes },
                zoologyMinutes = daySessions.filter { it.subject == NeetSubject.ZOOLOGY }.sumOf { it.durationMinutes }
            )
        }

        val todayMinutes = weeklyBreakdown.lastOrNull()?.totalMinutes ?: 0
        val weeklyTotalMinutes = weeklyBreakdown.sumOf { it.totalMinutes }

        // Compute consecutive study streak up to today/yesterday
        val activeDays = sessions.map { session ->
            val c = Calendar.getInstance().apply {
                timeInMillis = session.timestampMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            c.timeInMillis
        }.toSet()

        var streak = 0
        var checkDay = if (activeDays.contains(todayStart)) todayStart else todayStart - 86_400_000L
        while (activeDays.contains(checkDay)) {
            streak++
            checkDay -= 86_400_000L
        }

        val sevenDaysAgo = todayStart - 6 * 86_400_000L
        val recentSessions = sessions.filter { it.timestampMillis >= sevenDaysAgo }
        val subjectHours7d = NeetSubject.entries.associateWith { subj ->
            recentSessions.filter { it.subject == subj }.sumOf { it.durationMinutes } / 60f
        }

        val totalMcqs = sessions.sumOf { it.questionsSolved }
        val totalMcqsCorrect = sessions.sumOf { it.questionsCorrect }
        val overallAcc = if (totalMcqs > 0) ((totalMcqsCorrect.toFloat() / totalMcqs) * 100).roundToInt() else 0

        val latestMock = mocks.maxByOrNull { it.testDateMillis }
        val bestMockScore = mocks.maxOfOrNull { it.totalScore } ?: 0
        val avgNeg = if (mocks.isNotEmpty()) mocks.map { it.totalNegativeMarks }.average().roundToInt() else 0
        val worstNegSubj = if (mocks.isNotEmpty()) {
            NeetSubject.entries.maxByOrNull { subj -> mocks.sumOf { it.wrongFor(subj) } }
        } else null

        val todayBucket = weeklyBreakdown.lastOrNull()
        val todaySubjectHours = if (todayBucket != null) {
            DailySubjectHours(
                physicsHours = todayBucket.physicsMinutes / 60f,
                chemistryHours = todayBucket.chemistryMinutes / 60f,
                botanyHours = todayBucket.botanyMinutes / 60f,
                zoologyHours = todayBucket.zoologyMinutes / 60f
            )
        } else DailySubjectHours()

        val readinessPct = if (chapters.isNotEmpty()) {
            ((chapters.sumOf {
                if (it.isCompleted) 1.0 else it.mastery.weight.toDouble()
            } / chapters.size) * 100).roundToInt()
        } else 0
        val completedTopicsCount = chapters.count { it.isCompleted }
        val masteredCount = chapters.count { it.mastery == MasteryLevel.MASTERED || it.isCompleted }

        val chapterStats = sessions.groupBy { it.chapterName }.mapValues { (_, list) ->
            Pair(list.sumOf { it.durationMinutes }, list.sumOf { it.questionsSolved })
        }

        val insight = buildDiagnosticInsight(
            recentSessions = recentSessions,
            subjectHours7d = subjectHours7d,
            mocks = mocks,
            worstNegSubj = worstNegSubj,
            goals = goals,
            weeklyAvgHours = (weeklyTotalMinutes / 60f) / 7f
        )

        return NeetUiState(
            isLoading = false,
            goals = goals,
            sessions = sessions,
            mockTests = mocks,
            chapters = chapters,
            daysToNeet = daysToNeet,
            todayMinutes = todayMinutes,
            todayHours = todayMinutes / 60f,
            todaySubjectHours = todaySubjectHours,
            weeklyHours = weeklyTotalMinutes / 60f,
            weeklyAvgHours = (weeklyTotalMinutes / 60f) / 7f,
            streakDays = streak,
            weeklyBreakdown = weeklyBreakdown,
            subjectHours7d = subjectHours7d,
            totalMcqsSolved = totalMcqs,
            overallMcqAccuracy = overallAcc,
            latestMock = latestMock,
            bestMockScore = bestMockScore,
            avgNegativeMarks = avgNeg,
            worstNegativeSubject = worstNegSubj,
            syllabusReadinessPct = readinessPct,
            completedTopicsCount = completedTopicsCount,
            masteredCount = masteredCount,
            chapterStudyStats = chapterStats,
            primaryInsight = insight
        )
    }

    private fun buildDiagnosticInsight(
        recentSessions: List<StudySessionEntity>,
        subjectHours7d: Map<NeetSubject, Float>,
        mocks: List<MockTestEntity>,
        worstNegSubj: NeetSubject?,
        goals: AspirantGoals,
        weeklyAvgHours: Float
    ): String {
        val totalRecentHours = subjectHours7d.values.sum()
        if (recentSessions.isEmpty() && mocks.isEmpty()) {
            return "Target ${goals.targetScore}/720 · Recommended split: 50% Biology, 25% Physics, 25% Chemistry."
        }
        if (totalRecentHours > 0.5f) {
            val phyPct = ((subjectHours7d[NeetSubject.PHYSICS] ?: 0f) / totalRecentHours) * 100f
            val chemPct = ((subjectHours7d[NeetSubject.CHEMISTRY] ?: 0f) / totalRecentHours) * 100f
            val bioPct = (((subjectHours7d[NeetSubject.BOTANY] ?: 0f) + (subjectHours7d[NeetSubject.ZOOLOGY] ?: 0f)) / totalRecentHours) * 100f

            if (phyPct < 15f) {
                return "Physics is ${phyPct.roundToInt()}% of this week's hours (target 25%). Schedule a numerical block."
            }
            if (chemPct < 15f) {
                return "Chemistry is ${chemPct.roundToInt()}% of this week's hours (target 25%). Add Organic/Inorganic NCERT drill."
            }
            if (bioPct < 38f) {
                return "Biology is ${bioPct.roundToInt()}% of study time vs 50% NEET marks (360/720). Boost NCERT Bio revisions."
            }
        }
        val latest = mocks.maxByOrNull { it.testDateMillis }
        if (latest != null && worstNegSubj != null && latest.wrongFor(worstNegSubj) >= 5) {
            return "Negative alert: ${worstNegSubj.displayName} cost -${latest.wrongFor(worstNegSubj)} marks in your latest mock. Skip 50-50 guesses."
        }
        return if (weeklyAvgHours >= goals.dailyGoalHours * 0.85f) {
            "Strong pace at ${String.format(Locale.US, "%.1f", weeklyAvgHours)}h/day. Keep daily NCERT MCQ accuracy above 88%."
        } else {
            "Averaging ${String.format(Locale.US, "%.1f", weeklyAvgHours)}h/day vs ${String.format(Locale.US, "%.1f", goals.dailyGoalHours)}h goal. Add one extra 50m block."
        }
    }

    // Timestamp-based timer & stopwatch controls (persisted across restarts)
    private suspend fun restorePersistedTimer() {
        val snap = repository.readTimerSnapshot() ?: return
        val restoredSubject = NeetSubject.fromName(snap.subjectName)
        val restoredMode = StudyMode.fromName(snap.modeName)
        val now = System.currentTimeMillis()
        val elapsedMs = if (snap.isRunning && snap.startTime > 0L) {
            (now - snap.startTime) + snap.accumulatedTime
        } else {
            snap.accumulatedTime
        }.coerceAtLeast(0L)

        val targetMs = restoredMode.durationMinutes * 60_000L
        if (snap.isRunning && restoredMode != StudyMode.STOPWATCH && targetMs > 0L && elapsedMs >= targetMs) {
            // Countdown finished while app was closed: log full session & reset persisted runtime
            repository.logStudySession(
                StudySessionEntity(
                    subjectName = restoredSubject.name,
                    chapterName = snap.chapterName,
                    durationMinutes = restoredMode.durationMinutes,
                    modeName = restoredMode.name,
                    notes = "${restoredMode.label} completed",
                    timestampMillis = now
                )
            )
            repository.saveTimerSnapshot(
                startTime = 0L,
                accumulatedTime = 0L,
                isRunning = false,
                subjectName = restoredSubject.name,
                chapterName = snap.chapterName,
                modeName = restoredMode.name
            )
            _timerState.value = FocusTimerState(
                selectedSubject = restoredSubject,
                selectedChapter = snap.chapterName,
                selectedMode = restoredMode,
                startTimeMillis = 0L,
                accumulatedTimeMillis = 0L,
                elapsedSeconds = 0,
                isRunning = false,
                lastLoggedBanner = "Logged ${restoredMode.durationMinutes}m ${restoredSubject.displayName} (${snap.chapterName}) while away!"
            )
            return
        }

        _timerState.value = FocusTimerState(
            selectedSubject = restoredSubject,
            selectedChapter = snap.chapterName,
            selectedMode = restoredMode,
            startTimeMillis = if (snap.isRunning) snap.startTime else 0L,
            accumulatedTimeMillis = snap.accumulatedTime,
            elapsedSeconds = (elapsedMs / 1000L).toInt(),
            isRunning = snap.isRunning
        )
        if (snap.isRunning) {
            startTicker()
        }
    }

    fun syncTimerFromClock() {
        val state = _timerState.value
        if (!state.isRunning || state.startTimeMillis <= 0L) return
        val now = System.currentTimeMillis()
        val elapsedMs = ((now - state.startTimeMillis) + state.accumulatedTimeMillis).coerceAtLeast(0L)
        val targetMs = state.targetSeconds * 1000L
        if (state.selectedMode != StudyMode.STOPWATCH && targetMs > 0L && elapsedMs >= targetMs) {
            completeCountdownSession(state)
        } else {
            _timerState.update { it.copy(elapsedSeconds = (elapsedMs / 1000L).toInt()) }
        }
    }

    fun selectTimerSubject(subject: NeetSubject, defaultChapter: String) {
        _timerState.update {
            it.copy(selectedSubject = subject, selectedChapter = defaultChapter)
        }
        persistCurrentTimerState()
    }

    fun selectTimerChapter(chapter: String) {
        _timerState.update { it.copy(selectedChapter = chapter) }
        persistCurrentTimerState()
    }

    fun selectTimerMode(mode: StudyMode) {
        stopTicker()
        _timerState.update {
            it.copy(
                selectedMode = mode,
                startTimeMillis = 0L,
                accumulatedTimeMillis = 0L,
                elapsedSeconds = 0,
                isRunning = false
            )
        }
        persistCurrentTimerState()
    }

    fun toggleTimer() {
        val current = _timerState.value
        val now = System.currentTimeMillis()
        if (current.isRunning) {
            stopTicker()
            val newAccumulated = if (current.startTimeMillis > 0L) {
                ((now - current.startTimeMillis) + current.accumulatedTimeMillis).coerceAtLeast(0L)
            } else {
                current.accumulatedTimeMillis
            }
            _timerState.update {
                it.copy(
                    startTimeMillis = 0L,
                    accumulatedTimeMillis = newAccumulated,
                    elapsedSeconds = (newAccumulated / 1000L).toInt(),
                    isRunning = false
                )
            }
            persistCurrentTimerState()
            _squadSyncSignal.tryEmit(Unit)
        } else {
            _timerState.update {
                it.copy(
                    startTimeMillis = now,
                    isRunning = true,
                    lastLoggedBanner = null
                )
            }
            persistCurrentTimerState()
            startTicker()
            _squadSyncSignal.tryEmit(Unit)
        }
    }

    private fun startTicker() {
        stopTicker()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(500L)
                val state = _timerState.value
                if (!state.isRunning || state.startTimeMillis <= 0L) break

                val now = System.currentTimeMillis()
                val elapsedMs = ((now - state.startTimeMillis) + state.accumulatedTimeMillis).coerceAtLeast(0L)
                val targetMs = state.targetSeconds * 1000L

                if (state.selectedMode != StudyMode.STOPWATCH && targetMs > 0L && elapsedMs >= targetMs) {
                    completeCountdownSession(state)
                    break
                } else {
                    val newSec = (elapsedMs / 1000L).toInt()
                    if (newSec != state.elapsedSeconds) {
                        _timerState.update { it.copy(elapsedSeconds = newSec) }
                    }
                }
            }
        }
    }

    private fun completeCountdownSession(finished: FocusTimerState) {
        stopTicker()
        _timerState.update {
            it.copy(
                startTimeMillis = 0L,
                accumulatedTimeMillis = 0L,
                elapsedSeconds = 0,
                isRunning = false,
                lastLoggedBanner = "Logged ${finished.selectedMode.durationMinutes}m ${finished.selectedSubject.displayName} (${finished.selectedChapter}) to database!"
            )
        }
        viewModelScope.launch {
            repository.logStudySession(
                StudySessionEntity(
                    subjectName = finished.selectedSubject.name,
                    chapterName = finished.selectedChapter,
                    durationMinutes = finished.selectedMode.durationMinutes,
                    modeName = finished.selectedMode.name,
                    notes = "${finished.selectedMode.label} completed",
                    timestampMillis = System.currentTimeMillis()
                )
            )
            persistTimerSnapshotInternal(_timerState.value)
            _squadSyncSignal.tryEmit(Unit)
        }
    }

    fun resetTimer() {
        stopTicker()
        _timerState.update {
            it.copy(
                startTimeMillis = 0L,
                accumulatedTimeMillis = 0L,
                elapsedSeconds = 0,
                isRunning = false
            )
        }
        persistCurrentTimerState()
        _squadSyncSignal.tryEmit(Unit)
    }

    fun clearLastLoggedBanner() {
        _timerState.update { it.copy(lastLoggedBanner = null) }
    }

    private fun stopTicker() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun persistCurrentTimerState() {
        val snapshot = _timerState.value
        viewModelScope.launch {
            persistTimerSnapshotInternal(snapshot)
        }
    }

    private suspend fun persistTimerSnapshotInternal(state: FocusTimerState) {
        repository.saveTimerSnapshot(
            startTime = state.startTimeMillis,
            accumulatedTime = state.accumulatedTimeMillis,
            isRunning = state.isRunning,
            subjectName = state.selectedSubject.name,
            chapterName = state.selectedChapter,
            modeName = state.selectedMode.name
        )
    }

    fun saveTimerSession(questionsSolved: Int, questionsCorrect: Int, notes: String) {
        val state = _timerState.value
        val now = System.currentTimeMillis()
        val elapsedMs = if (state.isRunning && state.startTimeMillis > 0L) {
            ((now - state.startTimeMillis) + state.accumulatedTimeMillis).coerceAtLeast(0L)
        } else {
            state.accumulatedTimeMillis
        }
        val elapsedSec = max(state.elapsedSeconds, (elapsedMs / 1000L).toInt())
        val minutes = max(1, (elapsedSec + 59) / 60)
        stopTicker()
        viewModelScope.launch {
            repository.logStudySession(
                StudySessionEntity(
                    subjectName = state.selectedSubject.name,
                    chapterName = state.selectedChapter,
                    durationMinutes = minutes,
                    questionsSolved = questionsSolved.coerceAtLeast(0),
                    questionsCorrect = questionsCorrect.coerceIn(0, questionsSolved.coerceAtLeast(0)),
                    modeName = state.selectedMode.name,
                    notes = notes.trim(),
                    timestampMillis = now
                )
            )
            _timerState.update {
                it.copy(
                    startTimeMillis = 0L,
                    accumulatedTimeMillis = 0L,
                    elapsedSeconds = 0,
                    isRunning = false,
                    lastLoggedBanner = "Logged ${minutes}m ${state.selectedSubject.displayName} (${state.selectedChapter})"
                )
            }
            persistTimerSnapshotInternal(_timerState.value)
            _squadSyncSignal.tryEmit(Unit)
        }
    }

    fun logManualSession(
        subject: NeetSubject,
        chapterName: String,
        durationMinutes: Int,
        questionsSolved: Int,
        questionsCorrect: Int,
        notes: String
    ) {
        viewModelScope.launch {
            repository.logStudySession(
                StudySessionEntity(
                    subjectName = subject.name,
                    chapterName = chapterName,
                    durationMinutes = durationMinutes.coerceIn(5, 720),
                    questionsSolved = questionsSolved.coerceAtLeast(0),
                    questionsCorrect = questionsCorrect.coerceIn(0, questionsSolved.coerceAtLeast(0)),
                    modeName = StudyMode.DEEP_BLOCK.name,
                    notes = notes.trim(),
                    timestampMillis = System.currentTimeMillis()
                )
            )
            _squadSyncSignal.tryEmit(Unit)
        }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch { repository.deleteStudySession(id) }
    }

    fun addMockTest(test: MockTestEntity) {
        viewModelScope.launch {
            repository.addMockTest(test)
            _squadSyncSignal.tryEmit(Unit)
        }
    }

    fun deleteMockTest(id: Long) {
        viewModelScope.launch { repository.deleteMockTest(id) }
    }

    fun incrementChapterRevision(chapter: SyllabusChapterEntity) {
        viewModelScope.launch {
            val nextMastery = if (chapter.mastery == MasteryLevel.NOT_STARTED) {
                MasteryLevel.READING.name
            } else {
                chapter.masteryName
            }
            repository.updateChapter(
                chapter.copy(
                    ncertRevisions = chapter.ncertRevisions + 1,
                    masteryName = nextMastery,
                    lastRevisedMillis = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleChapterCompletion(chapter: SyllabusChapterEntity, completed: Boolean) {
        viewModelScope.launch {
            val nextMastery = when {
                completed -> MasteryLevel.MASTERED.name
                chapter.mastery == MasteryLevel.MASTERED -> MasteryLevel.PYQS_DONE.name
                else -> chapter.masteryName
            }
            repository.updateChapter(
                chapter.copy(
                    isCompleted = completed,
                    masteryName = nextMastery,
                    lastRevisedMillis = if (completed) System.currentTimeMillis() else chapter.lastRevisedMillis
                )
            )
        }
    }

    fun updateChapterMastery(chapter: SyllabusChapterEntity, mastery: MasteryLevel) {
        viewModelScope.launch {
            repository.updateChapter(
                chapter.copy(
                    masteryName = mastery.name,
                    isCompleted = mastery == MasteryLevel.MASTERED,
                    lastRevisedMillis = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateChapterConfidence(chapter: SyllabusChapterEntity, confidence: Int) {
        viewModelScope.launch {
            repository.updateChapter(chapter.copy(confidence = confidence.coerceIn(1, 5)))
        }
    }

    fun updateGoals(
        targetScore: Int,
        physicsGoalHours: Float,
        chemistryGoalHours: Float,
        biologyGoalHours: Float,
        examDateMillis: Long,
        themeMode: String
    ) {
        viewModelScope.launch {
            repository.updateGoals(
                targetScore = targetScore,
                physicsGoalHours = physicsGoalHours,
                chemistryGoalHours = chemistryGoalHours,
                biologyGoalHours = biologyGoalHours,
                examDateMillis = examDateMillis,
                themeMode = themeMode
            )
        }
    }

    fun updateSubjectGoals(
        physicsGoalHours: Float,
        chemistryGoalHours: Float,
        biologyGoalHours: Float
    ) {
        viewModelScope.launch {
            repository.updateSubjectGoals(
                physicsGoalHours = physicsGoalHours,
                chemistryGoalHours = chemistryGoalHours,
                biologyGoalHours = biologyGoalHours
            )
        }
    }

    fun setThemeMode(themeMode: String) {
        viewModelScope.launch {
            repository.updateThemeMode(themeMode)
        }
    }

    fun loadSampleAnalytics() {
        viewModelScope.launch {
            repository.loadSampleAnalyticsWeek()
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTicker()
    }

    class Factory(private val repository: NeetRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NeetViewModel(repository) as T
        }
    }
}
