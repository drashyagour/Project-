package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.SquadCodeUtils
import com.example.data.SquadMember
import com.example.data.SquadRoom
import com.example.data.SquadSortMode
import com.example.ui.SquadActionFeedback
import com.example.ui.SquadUiState
import com.example.ui.SquadViewModel
import com.example.ui.auth.GoogleSignInButton
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.auth.authStateFlow
import com.example.ui.auth.signOutFromGoogle
import com.example.ui.components.GeminiGradientPillButton
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GeminiGradientColors
import com.example.ui.theme.MonoMetricLarge
import com.example.ui.theme.MonoMetricMedium
import com.example.ui.theme.MonoMetricSmall
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun SquadRoute(
    todayMinutes: Int,
    isStudying: Boolean,
    latestMockScore: Int
) {
    val auth = remember {
        try {
            Firebase.auth
        } catch (_: Exception) {
            null
        }
    }
    if (auth == null) {
        SquadSignInScreen(
            errorMessage = "Firebase is initializing. Please retry in a moment.",
            onAuthSuccess = {},
            onAuthError = {}
        )
        return
    }

    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val credentialManager = remember(context) { CredentialManager.create(context) }
        var authError by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { authError = null },
                onUnauthenticated = {},
                scope = scope
            )
        }

        SquadSignInScreen(
            errorMessage = authError,
            onAuthSuccess = { authError = null },
            onAuthError = { msg -> authError = msg }
        )
    } else {
        key(user.uid) {
            val squadViewModel: SquadViewModel = viewModel(
                key = user.uid,
                factory = SquadViewModel.provideFactory(user.uid)
            )
            SquadAuthenticatedContent(
                user = user,
                viewModel = squadViewModel,
                todayMinutes = todayMinutes,
                isStudying = isStudying,
                latestMockScore = latestMockScore
            )
        }
    }
}

@Composable
private fun SquadSignInScreen(
    errorMessage: String?,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .testTag("squad_sign_in_card")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(GeminiGradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Groups,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Study Squad",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Create a private room with a 6-character code or join friends to track live study status, daily hours, and mock test scores.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                GoogleSignInButton(
                    onAuthSuccess = onAuthSuccess,
                    onAuthError = onAuthError
                )
            }
        }
    }
}

@Composable
private fun SquadAuthenticatedContent(
    user: FirebaseUser,
    viewModel: SquadViewModel,
    todayMinutes: Int,
    isStudying: Boolean,
    latestMockScore: Int
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val feedback by viewModel.feedback.collectAsStateWithLifecycle()

    LaunchedEffect(feedback.statusBanner) {
        if (feedback.statusBanner != null) {
            delay(3000L)
            viewModel.clearFeedback()
        }
    }

    when (val state = uiState) {
        SquadUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        is SquadUiState.NeedsOnboarding -> {
            val defaultName = state.savedDisplayName.ifBlank {
                user.displayName?.trim().orEmpty().take(30)
            }
            SquadOnboardingScreen(
                initialDisplayName = defaultName,
                previousSquads = state.previousSquads,
                feedback = feedback,
                onCreateSquad = { squadName, displayName, code ->
                    viewModel.createSquad(
                        squadName = squadName,
                        displayName = displayName,
                        requestedCode = code,
                        todayMinutes = todayMinutes,
                        isStudying = isStudying,
                        latestMockScore = latestMockScore
                    )
                },
                onJoinSquad = { roomCode, displayName ->
                    viewModel.joinSquad(
                        roomCode = roomCode,
                        displayName = displayName,
                        todayMinutes = todayMinutes,
                        isStudying = isStudying,
                        latestMockScore = latestMockScore
                    )
                },
                onClearError = { viewModel.clearFeedback() }
            )
        }

        is SquadUiState.Active -> {
            // Sync live metrics whenever user enters Active squad view with fresh local values
            LaunchedEffect(state.squad.roomCode, todayMinutes, isStudying, latestMockScore) {
                val me = state.squad.members[user.uid]
                val desiredStatus = if (isStudying) SquadMember.STATUS_STUDYING else SquadMember.STATUS_IDLE
                if (me == null ||
                    me.todayMinutes.toInt() != todayMinutes.coerceIn(0, 1440) ||
                    me.status != desiredStatus ||
                    me.latestMockScore.toInt() != latestMockScore.coerceIn(0, 720)
                ) {
                    viewModel.syncStats(
                        todayMinutes = todayMinutes,
                        isStudying = isStudying,
                        latestMockScore = latestMockScore
                    )
                }
            }

            SquadLeaderboardScreen(
                squad = state.squad,
                currentUserId = user.uid,
                currentDisplayName = state.localProfile.displayName.ifBlank {
                    state.squad.members[user.uid]?.displayName ?: user.displayName ?: "Aspirant"
                },
                sortMode = state.sortMode,
                feedback = feedback,
                onChangeSortMode = { mode -> viewModel.setSortMode(mode) },
                onUpdateDisplayName = { newName ->
                    viewModel.syncStats(
                        displayNameOverride = newName,
                        todayMinutes = todayMinutes,
                        isStudying = isStudying,
                        latestMockScore = latestMockScore
                    )
                },
                onSyncNow = {
                    viewModel.syncStats(
                        todayMinutes = todayMinutes,
                        isStudying = isStudying,
                        latestMockScore = latestMockScore
                    )
                },
                onLeaveSquad = { viewModel.leaveCurrentSquad() }
            )
        }

        is SquadUiState.Error -> {
            SquadErrorScreen(
                message = state.message,
                savedRoomCode = state.savedRoomCode,
                onRetry = {
                    viewModel.refreshJoinedSquads()
                    viewModel.syncStats(
                        todayMinutes = todayMinutes,
                        isStudying = isStudying,
                        latestMockScore = latestMockScore
                    )
                },
                onSwitchRoom = { viewModel.leaveCurrentSquad() }
            )
        }
    }
}

@Composable
private fun SquadOnboardingScreen(
    initialDisplayName: String,
    previousSquads: List<SquadRoom>,
    feedback: SquadActionFeedback,
    onCreateSquad: (squadName: String, displayName: String, roomCode: String) -> Unit,
    onJoinSquad: (roomCode: String, displayName: String) -> Unit,
    onClearError: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var displayName by remember(initialDisplayName) { mutableStateOf(initialDisplayName) }
    var isCreateMode by remember { mutableStateOf(true) }
    var squadName by remember { mutableStateOf("") }
    var createRoomCode by remember { mutableStateOf(SquadCodeUtils.generateRoomCode()) }
    var joinRoomCode by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 580.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Join or Create a Squad",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Share your live study hours and mock scores with friends.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { signOutFromGoogle(credentialManager, scope) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = stringResource(id = R.string.cd_sign_out),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = {
                            displayName = it.take(30)
                            onClearError()
                        },
                        label = { Text("Your Display Name") },
                        placeholder = { Text("e.g. Drashya") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = geminiTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("squad_display_name_input")
                    )

                    // Mode toggle pills: Create vs Join
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SquadSegmentedPill(
                                label = "Create a Squad",
                                selected = isCreateMode,
                                onClick = {
                                    isCreateMode = true
                                    onClearError()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("squad_mode_create")
                            )
                            SquadSegmentedPill(
                                label = "Join a Squad",
                                selected = !isCreateMode,
                                onClick = {
                                    isCreateMode = false
                                    onClearError()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("squad_mode_join")
                            )
                        }
                    }

                    if (isCreateMode) {
                        OutlinedTextField(
                            value = squadName,
                            onValueChange = {
                                squadName = it.take(40)
                                onClearError()
                            },
                            label = { Text("Squad Name") },
                            placeholder = { Text("e.g. AIIMS 2026 Grinders") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = geminiTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("squad_name_input")
                        )

                        OutlinedTextField(
                            value = createRoomCode,
                            onValueChange = {
                                createRoomCode = SquadCodeUtils.normalize(it)
                                onClearError()
                            },
                            label = { Text("6-Char Room Code") },
                            placeholder = { Text("NEET26") },
                            singleLine = true,
                            textStyle = MonoMetricMedium,
                            trailingIcon = {
                                IconButton(
                                    onClick = { createRoomCode = SquadCodeUtils.generateRoomCode() },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Shuffle,
                                        contentDescription = "Generate new room code",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = geminiTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("squad_create_code_input")
                        )
                    } else {
                        OutlinedTextField(
                            value = joinRoomCode,
                            onValueChange = {
                                joinRoomCode = SquadCodeUtils.normalize(it)
                                onClearError()
                            },
                            label = { Text("Friend's Room Code") },
                            placeholder = { Text("e.g. NEET26") },
                            singleLine = true,
                            textStyle = MonoMetricMedium,
                            shape = RoundedCornerShape(16.dp),
                            colors = geminiTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("squad_join_code_input")
                        )
                    }

                    AnimatedVisibility(visible = !feedback.errorMessage.isNullOrBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = feedback.errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (feedback.isBusy) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.5.dp
                            )
                        }
                    } else {
                        GeminiGradientPillButton(
                            text = if (isCreateMode) "Create Squad" else "Join Squad",
                            icon = Icons.Outlined.GroupAdd,
                            onClick = {
                                if (isCreateMode) {
                                    onCreateSquad(
                                        squadName.ifBlank { "NEET Squad" },
                                        displayName,
                                        createRoomCode
                                    )
                                } else {
                                    onJoinSquad(joinRoomCode, displayName)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("squad_submit_button")
                        )
                    }
                }
            }
        }

        if (previousSquads.isNotEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 580.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "YOUR EXISTING SQUADS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        previousSquads.forEach { room ->
                            Surface(
                                onClick = {
                                    onJoinSquad(
                                        room.roomCode,
                                        displayName.ifBlank { "Aspirant" }
                                    )
                                },
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = room.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${room.memberUids.size} members",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.background,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = room.roomCode,
                                            style = MonoMetricSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SquadLeaderboardScreen(
    squad: SquadRoom,
    currentUserId: String,
    currentDisplayName: String,
    sortMode: SquadSortMode,
    feedback: SquadActionFeedback,
    onChangeSortMode: (SquadSortMode) -> Unit,
    onUpdateDisplayName: (String) -> Unit,
    onSyncNow: () -> Unit,
    onLeaveSquad: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val clipboard = LocalClipboardManager.current

    var copiedCode by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(copiedCode) {
        if (copiedCode) {
            delay(2000L)
            copiedCode = false
        }
    }

    val rankedMembers = remember(squad.members, sortMode) {
        squad.sortedMembers(sortMode)
    }

    val topValue = remember(rankedMembers, sortMode) {
        val top = rankedMembers.firstOrNull()
        when (sortMode) {
            SquadSortMode.TODAY_HOURS -> max(1L, top?.todayMinutes ?: 1L)
            SquadSortMode.MOCK_SCORE -> max(1L, top?.latestMockScore ?: 1L)
        }
    }

    val studyingCount = remember(rankedMembers) {
        rankedMembers.count { it.isStudying }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header Card: Squad Name, Copyable Room Code pill, Actions, and Sort Toggle
        item(key = "squad_header") {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = squad.name.ifBlank { "Study Squad" },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("squad_header_title")
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (studyingCount > 0) EmeraldSuccess
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                )
                                Text(
                                    text = "${rankedMembers.size} members · $studyingCount studying now",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onSyncNow,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = "Sync stats now",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { showRenameDialog = true },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit display name",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { showLeaveDialog = true },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                                    contentDescription = stringResource(id = R.string.cd_leave_squad),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Copyable Room Code pill + Signed-in info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            onClick = {
                                clipboard.setText(AnnotatedString(squad.roomCode))
                                copiedCode = true
                            },
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape,
                            modifier = Modifier.testTag("squad_room_code_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (copiedCode) "Copied!" else "Code",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (copiedCode) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = squad.roomCode,
                                    style = MonoMetricMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = if (copiedCode) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                                    contentDescription = stringResource(id = R.string.cd_copy_room_code),
                                    tint = if (copiedCode) EmeraldSuccess else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        TextButton(
                            onClick = { signOutFromGoogle(credentialManager, scope) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Sign Out",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Sort Toggle Pills: Today's Hours vs Mock Score
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SquadSortMode.entries.forEach { mode ->
                                SquadSegmentedPill(
                                    label = mode.label,
                                    selected = sortMode == mode,
                                    onClick = { onChangeSortMode(mode) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("squad_sort_${mode.name.lowercase()}")
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = !feedback.errorMessage.isNullOrBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = feedback.errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // Leaderboard List (#1E1F20 cards, 20.dp radius)
        itemsIndexed(
            items = rankedMembers,
            key = { _, member -> member.uid }
        ) { index, member ->
            val rank = index + 1
            val rawMetric = when (sortMode) {
                SquadSortMode.TODAY_HOURS -> member.todayMinutes
                SquadSortMode.MOCK_SCORE -> member.latestMockScore
            }
            val progressFraction = if (rawMetric <= 0L) 0f else (rawMetric.toFloat() / topValue.toFloat()).coerceIn(0f, 1f)

            SquadMemberLeaderboardCard(
                rank = rank,
                member = member,
                isSelf = member.uid == currentUserId,
                sortMode = sortMode,
                progressVsLeader = progressFraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (showRenameDialog) {
        var editedName by remember(currentDisplayName) { mutableStateOf(currentDisplayName) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Update Display Name") },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it.take(30) },
                    label = { Text("Display Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = geminiTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedName.isNotBlank()) {
                            onUpdateDisplayName(editedName.trim())
                            showRenameDialog = false
                        }
                    },
                    shape = CircleShape
                ) {
                    Text(stringResource(id = R.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text(stringResource(id = R.string.action_cancel))
                }
            }
        )
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Leave ${squad.name}?") },
            text = {
                Text(
                    "You can rejoin anytime using Room Code ${squad.roomCode}, or create/join another squad.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLeaveDialog = false
                        onLeaveSquad()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = CircleShape
                ) {
                    Text("Leave Squad")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text(stringResource(id = R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun SquadMemberLeaderboardCard(
    rank: Int,
    member: SquadMember,
    isSelf: Boolean,
    sortMode: SquadSortMode,
    progressVsLeader: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressVsLeader,
        animationSpec = tween(durationMillis = 450),
        label = "squadProgress"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.testTag("squad_member_card_$rank")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Rank Badge (#1 gets subtle Gemini gradient badge)
                    if (rank == 1) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Brush.linearGradient(GeminiGradientColors))
                                .padding(horizontal = 11.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = "Rank 1",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "#1",
                                    style = MonoMetricSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#$rank",
                                style = MonoMetricSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = member.displayName.ifBlank { "Aspirant" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isSelf) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "You",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Live Status Dot (Studying with green dot, or Idle)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (member.isStudying) EmeraldSuccess
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                    )
                            )
                            Text(
                                text = if (member.isStudying) "Studying" else "Idle",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (member.isStudying) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Tabular Metrics (Today's Hours + Latest Mock Score)
                Column(horizontalAlignment = Alignment.End) {
                    val primaryText = when (sortMode) {
                        SquadSortMode.TODAY_HOURS -> formatStudyMinutes(member.todayMinutes.toInt())
                        SquadSortMode.MOCK_SCORE -> "${member.latestMockScore}/720"
                    }
                    val secondaryText = when (sortMode) {
                        SquadSortMode.TODAY_HOURS -> "Mock ${member.latestMockScore}/720"
                        SquadSortMode.MOCK_SCORE -> "Today ${formatStudyMinutes(member.todayMinutes.toInt())}"
                    }
                    Text(
                        text = primaryText,
                        style = MonoMetricLarge,
                        color = if (rank == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = secondaryText,
                        style = MonoMetricSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Thin comparison progress bar against #1 spot
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(5.dp)
                        .clip(CircleShape)
                        .then(
                            if (rank == 1) {
                                Modifier.background(Brush.linearGradient(GeminiGradientColors))
                            } else {
                                Modifier.background(MaterialTheme.colorScheme.primary)
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun SquadErrorScreen(
    message: String,
    savedRoomCode: String,
    onRetry: () -> Unit,
    onSwitchRoom: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Squad Sync Issue",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onRetry,
                        shape = CircleShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Retry")
                    }
                    if (savedRoomCode.isNotBlank()) {
                        TextButton(
                            onClick = onSwitchRoom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Leave $savedRoomCode")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SquadSegmentedPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
        shape = CircleShape,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun geminiTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedContainerColor = MaterialTheme.colorScheme.background,
    unfocusedContainerColor = MaterialTheme.colorScheme.background,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
)

private fun formatStudyMinutes(totalMinutes: Int): String {
    val mins = totalMinutes.coerceAtLeast(0)
    val h = mins / 60
    val m = mins % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}m"
        h > 0 -> "${h}h 0m"
        else -> "${m}m"
    }
}
