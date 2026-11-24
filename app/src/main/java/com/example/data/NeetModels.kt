package com.example.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ui.theme.BotanyColor
import com.example.ui.theme.ChemistryColor
import com.example.ui.theme.PhysicsColor
import com.example.ui.theme.ZoologyColor

enum class NeetSubject(
    val displayName: String,
    val shortName: String,
    val maxMarks: Int,
    val examQuestions: Int,
    val accentColor: Color
) {
    PHYSICS("Physics", "PHY", 180, 45, PhysicsColor),
    CHEMISTRY("Chemistry", "CHEM", 180, 45, ChemistryColor),
    BOTANY("Botany", "BOT", 180, 45, BotanyColor),
    ZOOLOGY("Zoology", "ZOO", 180, 45, ZoologyColor);

    companion object {
        fun fromName(raw: String): NeetSubject =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: BIOLOGY_FALLBACK

        private val BIOLOGY_FALLBACK = BOTANY
    }
}

enum class MasteryLevel(val label: String, val weight: Float) {
    NOT_STARTED("Not Started", 0f),
    READING("NCERT Reading", 0.35f),
    PYQS_DONE("PYQs Solved", 0.7f),
    MASTERED("Mastered", 1.0f);

    companion object {
        fun fromName(raw: String): MasteryLevel =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: NOT_STARTED
    }
}

enum class StudyMode(val label: String, val durationMinutes: Int) {
    POMODORO("Pomodoro 25m", 25),
    DEEP_BLOCK("Deep Focus 50m", 50),
    NTA_MOCK("NTA Block 200m", 200),
    STOPWATCH("Open Stopwatch", 0);

    companion object {
        fun fromName(raw: String): StudyMode =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: DEEP_BLOCK
    }
}

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val subjectName: String,
    val chapterName: String,
    val durationMinutes: Int,
    val questionsSolved: Int = 0,
    val questionsCorrect: Int = 0,
    val modeName: String = StudyMode.DEEP_BLOCK.name,
    val notes: String = "",
    val timestampMillis: Long = System.currentTimeMillis()
) {
    val subject: NeetSubject
        get() = NeetSubject.fromName(subjectName)

    val accuracyPercent: Int
        get() = if (questionsSolved > 0) ((questionsCorrect.toFloat() / questionsSolved) * 100).toInt() else 0
}

@Entity(tableName = "mock_tests")
data class MockTestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val testDateMillis: Long = System.currentTimeMillis(),
    val physicsCorrect: Int = 0,
    val physicsWrong: Int = 0,
    val physicsSkipped: Int = 45,
    val chemistryCorrect: Int = 0,
    val chemistryWrong: Int = 0,
    val chemistrySkipped: Int = 45,
    val botanyCorrect: Int = 0,
    val botanyWrong: Int = 0,
    val botanySkipped: Int = 45,
    val zoologyCorrect: Int = 0,
    val zoologyWrong: Int = 0,
    val zoologySkipped: Int = 45,
    val mistakeNotes: String = ""
) {
    val physicsScore: Int get() = (physicsCorrect * 4) - physicsWrong
    val chemistryScore: Int get() = (chemistryCorrect * 4) - chemistryWrong
    val botanyScore: Int get() = (botanyCorrect * 4) - botanyWrong
    val zoologyScore: Int get() = (zoologyCorrect * 4) - zoologyWrong

    val totalScore: Int get() = physicsScore + chemistryScore + botanyScore + zoologyScore
    val totalCorrect: Int get() = physicsCorrect + chemistryCorrect + botanyCorrect + zoologyCorrect
    val totalWrong: Int get() = physicsWrong + chemistryWrong + botanyWrong + zoologyWrong
    val totalAttempted: Int get() = totalCorrect + totalWrong
    val totalNegativeMarks: Int get() = totalWrong
    val potentialScoreWithoutNegatives: Int get() = totalScore + totalNegativeMarks

    val accuracyPercent: Int
        get() = if (totalAttempted > 0) ((totalCorrect.toFloat() / totalAttempted) * 100).toInt() else 0

    fun scoreFor(subject: NeetSubject): Int = when (subject) {
        NeetSubject.PHYSICS -> physicsScore
        NeetSubject.CHEMISTRY -> chemistryScore
        NeetSubject.BOTANY -> botanyScore
        NeetSubject.ZOOLOGY -> zoologyScore
    }

    fun wrongFor(subject: NeetSubject): Int = when (subject) {
        NeetSubject.PHYSICS -> physicsWrong
        NeetSubject.CHEMISTRY -> chemistryWrong
        NeetSubject.BOTANY -> botanyWrong
        NeetSubject.ZOOLOGY -> zoologyWrong
    }
}

@Entity(tableName = "syllabus_chapters")
data class SyllabusChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val subjectName: String,
    val classLevel: Int,
    val chapterName: String,
    val weightagePct: Float,
    val isHighYield: Boolean = false,
    val isCompleted: Boolean = false,
    val masteryName: String = MasteryLevel.NOT_STARTED.name,
    val ncertRevisions: Int = 0,
    val confidence: Int = 1,
    val lastRevisedMillis: Long? = null
) {
    val subject: NeetSubject
        get() = NeetSubject.fromName(subjectName)

    val mastery: MasteryLevel
        get() = MasteryLevel.fromName(masteryName)
}

data class DailySubjectHours(
    val physicsHours: Float = 0f,
    val chemistryHours: Float = 0f,
    val botanyHours: Float = 0f,
    val zoologyHours: Float = 0f
) {
    val biologyHours: Float get() = botanyHours + zoologyHours
    val totalHours: Float get() = physicsHours + chemistryHours + biologyHours
}

data class AspirantGoals(
    val targetScore: Int = 650,
    val dailyGoalHours: Float = 8.0f,
    val physicsGoalHours: Float = 2.0f,
    val chemistryGoalHours: Float = 2.0f,
    val biologyGoalHours: Float = 4.0f,
    val examDateMillis: Long = DEFAULT_NEET_DATE_MILLIS,
    val themeMode: String = "DARK"
) {
    companion object {
        // May 2, 2027 UTC noon
        const val DEFAULT_NEET_DATE_MILLIS = 1809259200000L
    }
}

enum class ChemistryBranch(val label: String) {
    ALL("All Chemistry"),
    PHYSICAL("Physical"),
    ORGANIC("Organic"),
    INORGANIC("Inorganic");

    fun matches(chapterName: String): Boolean = when (this) {
        ALL -> true
        PHYSICAL -> chapterName.contains("Mole", ignoreCase = true) ||
            chapterName.contains("Thermodynamics", ignoreCase = true) ||
            chapterName.contains("Equilibrium", ignoreCase = true) ||
            chapterName.contains("Solutions", ignoreCase = true) ||
            chapterName.contains("Kinetics", ignoreCase = true)
        INORGANIC -> chapterName.contains("Atomic", ignoreCase = true) ||
            chapterName.contains("Bonding", ignoreCase = true) ||
            chapterName.contains("d & f", ignoreCase = true) ||
            chapterName.contains("Coordination", ignoreCase = true)
        ORGANIC -> chapterName.contains("Organic", ignoreCase = true) ||
            chapterName.contains("Hydrocarbons", ignoreCase = true) ||
            chapterName.contains("Aldehydes", ignoreCase = true) ||
            chapterName.contains("Alcohols", ignoreCase = true) ||
            chapterName.contains("Biomolecules", ignoreCase = true)
    }
}

data class DailySubjectMinutes(
    val dayLabel: String,
    val dayStartMillis: Long,
    val physicsMinutes: Int,
    val chemistryMinutes: Int,
    val botanyMinutes: Int,
    val zoologyMinutes: Int
) {
    val totalMinutes: Int get() = physicsMinutes + chemistryMinutes + botanyMinutes + zoologyMinutes
    val totalHours: Float get() = totalMinutes / 60f
}
