package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.MockTestEntity
import com.example.data.NeetDatabase
import com.example.data.NeetSubject
import com.example.data.NeetSyllabusSeed
import com.example.data.StudyMode
import com.example.data.StudySessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<MainActivity>()

  private lateinit var db: NeetDatabase

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, NeetDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Aura", appName)
  }

  @Test
  fun `mock test NTA 720 scoring computes +4 and -1 accurately`() {
    val mock = MockTestEntity(
      title = "Grand Test",
      physicsCorrect = 36, physicsWrong = 5, physicsSkipped = 4,
      chemistryCorrect = 38, chemistryWrong = 4, chemistrySkipped = 3,
      botanyCorrect = 42, botanyWrong = 2, botanySkipped = 1,
      zoologyCorrect = 42, zoologyWrong = 2, zoologySkipped = 1
    )
    assertEquals(139, mock.physicsScore)
    assertEquals(148, mock.chemistryScore)
    assertEquals(166, mock.botanyScore)
    assertEquals(166, mock.zoologyScore)
    assertEquals(619, mock.totalScore)
    assertEquals(13, mock.totalNegativeMarks)
    assertEquals(632, mock.potentialScoreWithoutNegatives)
  }

  @Test
  fun `pomodoro session logs duration and daily subject hours to Room`() = runTest {
    val sessionDao = db.studySessionDao()
    sessionDao.insertSession(
      StudySessionEntity(
        subjectName = NeetSubject.PHYSICS.name,
        chapterName = "Current Electricity",
        durationMinutes = 25,
        modeName = StudyMode.POMODORO.name
      )
    )
    sessionDao.insertSession(
      StudySessionEntity(
        subjectName = NeetSubject.BOTANY.name,
        chapterName = "Cell: Unit of Life & Cell Cycle",
        durationMinutes = 50,
        modeName = StudyMode.DEEP_BLOCK.name
      )
    )

    val sessions = sessionDao.observeAllSessions().first()
    assertEquals(2, sessions.size)
    assertEquals(75, sessions.sumOf { it.durationMinutes })
    assertEquals(25, sessions.filter { it.subject == NeetSubject.PHYSICS }.sumOf { it.durationMinutes })
  }

  @Test
  fun `ncert syllabus checklist persists topic completion in Room`() = runTest {
    val syllabusDao = db.syllabusDao()
    syllabusDao.insertChapters(NeetSyllabusSeed.initialChapters())

    val initial = syllabusDao.observeAllChapters().first()
    assertTrue(initial.isNotEmpty())
    val firstTopic = initial.first()
    assertEquals(false, firstTopic.isCompleted)

    syllabusDao.updateChapter(firstTopic.copy(isCompleted = true))
    val updated = syllabusDao.observeAllChapters().first().first { it.id == firstTopic.id }
    assertTrue(updated.isCompleted)
  }

  @Test
  fun `timer preferences persist startTime accumulatedTime and isRunning across restarts`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = com.example.data.TimerPreferences(context)
    val fakeStart = System.currentTimeMillis() - 90_000L // started 90 seconds ago
    prefs.saveTimerState(
      startTime = fakeStart,
      accumulatedTime = 30_000L, // 30s previously accumulated
      isRunning = true,
      subjectName = NeetSubject.PHYSICS.name,
      chapterName = "Current Electricity",
      modeName = StudyMode.STOPWATCH.name
    )

    val restored = prefs.readSnapshot()
    assertTrue(restored.isRunning)
    assertEquals(fakeStart, restored.startTime)
    assertEquals(30_000L, restored.accumulatedTime)
    val elapsedMs = (System.currentTimeMillis() - restored.startTime) + restored.accumulatedTime
    assertTrue(elapsedMs >= 120_000L)
  }

  @Test
  @Config(sdk = [36], qualifiers = "w412dp-h915dp")
  fun `gemini dashboard displays stopwatch subject cards floating bar and drawer sections`() {
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("focus_timer_ring").assertIsDisplayed()
    composeTestRule.onNodeWithTag("timer_toggle_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("quick_add_task_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dashboard_list")
      .performScrollToNode(androidx.compose.ui.test.hasText("Today's Subject Progress"))
    composeTestRule.onNodeWithText("Today's Subject Progress").assertIsDisplayed()

    composeTestRule.onNodeWithTag("menu_drawer_button").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("drawer_new_session_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("drawer_physics").assertIsDisplayed()
    composeTestRule.onNodeWithTag("drawer_chemistry").assertIsDisplayed()
    composeTestRule.onNodeWithTag("drawer_biology").assertIsDisplayed()
    composeTestRule.onNodeWithTag("drawer_mocks").assertIsDisplayed()
    composeTestRule.onNodeWithTag("drawer_errors").assertIsDisplayed()
    composeTestRule.onNodeWithTag("drawer_squad").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("drawer_theme_toggle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("drawer_developer_credit").assertIsDisplayed()

    composeTestRule.onNodeWithTag("drawer_squad").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("squad_sign_in_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("google_sign_in_button").assertIsDisplayed()
  }

  @Test
  fun `squad room sorting ranks by today hours and mock score accurately`() {
    val code = com.example.data.SquadCodeUtils.generateRoomCode()
    assertTrue(com.example.data.SquadCodeUtils.isValidRoomCode(code))
    assertEquals("NEET26", com.example.data.SquadCodeUtils.normalize(" neet-26 "))

    val room = com.example.data.SquadRoom(
      roomCode = "NEET26",
      name = "AIIMS Squad",
      members = mapOf(
        "u1" to com.example.data.SquadMember("u1", "Drashya", todayMinutes = 240, status = "Studying", latestMockScore = 620),
        "u2" to com.example.data.SquadMember("u2", "Aarav", todayMinutes = 180, status = "Idle", latestMockScore = 675),
        "u3" to com.example.data.SquadMember("u3", "Riya", todayMinutes = 300, status = "Studying", latestMockScore = 590)
      )
    )

    val byHours = room.sortedMembers(com.example.data.SquadSortMode.TODAY_HOURS)
    assertEquals(listOf("u3", "u1", "u2"), byHours.map { it.uid })

    val byScore = room.sortedMembers(com.example.data.SquadSortMode.MOCK_SCORE)
    assertEquals(listOf("u2", "u1", "u3"), byScore.map { it.uid })
  }
}
