package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.ChemistryBranch
import com.example.data.NeetSubject
import com.example.ui.theme.GeminiGradientColors
import com.example.ui.theme.MonoMetricSmall

@Composable
fun GeminiSlideOutDrawer(
    isOpen: Boolean,
    activeNavKey: String,
    physicsProgressPct: Int,
    isDarkTheme: Boolean,
    onClose: () -> Unit,
    onNewStudySession: () -> Unit,
    onSelectDashboard: () -> Unit,
    onSelectPhysics: () -> Unit,
    onSelectChemistryBranch: (ChemistryBranch) -> Unit,
    onSelectBiologyBranch: (NeetSubject) -> Unit,
    onSelectAnalytics: () -> Unit,
    onSelectSyllabus: () -> Unit,
    onSelectMocks: () -> Unit,
    onSelectErrors: () -> Unit,
    onSelectSquad: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val drawerWidth = 304.dp
    val drawerWidthPx = with(LocalDensity.current) { drawerWidth.toPx() }
    val progress by animateFloatAsState(
        targetValue = if (isOpen) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "geminiDrawerSlide"
    )

    var chemistryExpanded by remember { mutableStateOf(false) }
    var biologyExpanded by remember { mutableStateOf(false) }

    BackHandler(enabled = isOpen) {
        onClose()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (progress > 0.01f) {
            // Subtle dimmed backdrop rgba(0, 0, 0, 0.5)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = progress }
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose
                    )
            )

            // Lightweight GPU-translated sidebar panel (#1E1F20 in dark, #F0F4F9 in light)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .width(drawerWidth)
                    .fillMaxHeight()
                    .graphicsLayer {
                        translationX = -drawerWidthPx * (1f - progress)
                    }
                    .pointerInput(Unit) { detectTapGestures {} }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    // Brand Header: Aura logo + name + developer credit
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.aura_icon),
                            contentDescription = "Aura logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                        )
                        Column {
                            Text(
                                text = stringResource(id = R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(id = R.string.developer_credit),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag("drawer_developer_credit")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Top Action: Compact rounded pill button for '+ New Study Session'
                    Surface(
                        onClick = {
                            onClose()
                            onNewStudySession()
                        },
                        shape = CircleShape,
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(Brush.linearGradient(GeminiGradientColors))
                            .testTag("drawer_new_session_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "New Study Session",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    DrawerMenuItem(
                        label = "Dashboard / Home",
                        icon = Icons.Outlined.Home,
                        selected = activeNavKey == "dashboard",
                        onClick = {
                            onClose()
                            onSelectDashboard()
                        },
                        modifier = Modifier.testTag("drawer_dashboard")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        DrawerSectionHeader("SUBJECTS")

                        DrawerMenuItem(
                            label = "Physics",
                            icon = Icons.Outlined.Science,
                            selected = activeNavKey == "syllabus_physics",
                            trailingText = "$physicsProgressPct%",
                            onClick = {
                                onClose()
                                onSelectPhysics()
                            },
                            modifier = Modifier.testTag("drawer_physics")
                        )

                        DrawerMenuItem(
                            label = "Chemistry",
                            icon = Icons.Outlined.Biotech,
                            selected = activeNavKey.startsWith("syllabus_chemistry"),
                            trailingIcon = if (chemistryExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            onClick = { chemistryExpanded = !chemistryExpanded },
                            modifier = Modifier.testTag("drawer_chemistry")
                        )

                        AnimatedVisibility(
                            visible = chemistryExpanded,
                            enter = expandVertically(tween(180)),
                            exit = shrinkVertically(tween(180))
                        ) {
                            Column(
                                modifier = Modifier.padding(start = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                listOf(
                                    ChemistryBranch.PHYSICAL to "Physical",
                                    ChemistryBranch.ORGANIC to "Organic",
                                    ChemistryBranch.INORGANIC to "Inorganic"
                                ).forEach { (branch, label) ->
                                    DrawerSubItem(
                                        label = label,
                                        selected = activeNavKey == "syllabus_chemistry_${branch.name.lowercase()}",
                                        onClick = {
                                            onClose()
                                            onSelectChemistryBranch(branch)
                                        }
                                    )
                                }
                            }
                        }

                        DrawerMenuItem(
                            label = "Biology",
                            icon = Icons.Outlined.Eco,
                            selected = activeNavKey == "syllabus_botany" || activeNavKey == "syllabus_zoology",
                            trailingIcon = if (biologyExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            onClick = { biologyExpanded = !biologyExpanded },
                            modifier = Modifier.testTag("drawer_biology")
                        )

                        AnimatedVisibility(
                            visible = biologyExpanded,
                            enter = expandVertically(tween(180)),
                            exit = shrinkVertically(tween(180))
                        ) {
                            Column(
                                modifier = Modifier.padding(start = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                DrawerSubItem(
                                    label = "Botany",
                                    selected = activeNavKey == "syllabus_botany",
                                    onClick = {
                                        onClose()
                                        onSelectBiologyBranch(NeetSubject.BOTANY)
                                    }
                                )
                                DrawerSubItem(
                                    label = "Zoology",
                                    selected = activeNavKey == "syllabus_zoology",
                                    onClick = {
                                        onClose()
                                        onSelectBiologyBranch(NeetSubject.ZOOLOGY)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        DrawerSectionHeader("PRACTICE & ANALYSIS")

                        DrawerMenuItem(
                            label = "Analytics",
                            icon = Icons.Outlined.Analytics,
                            selected = activeNavKey == "analytics",
                            onClick = {
                                onClose()
                                onSelectAnalytics()
                            },
                            modifier = Modifier.testTag("drawer_analytics")
                        )

                        DrawerMenuItem(
                            label = "Syllabus Checklist",
                            icon = Icons.AutoMirrored.Outlined.MenuBook,
                            selected = activeNavKey == "syllabus_all",
                            onClick = {
                                onClose()
                                onSelectSyllabus()
                            },
                            modifier = Modifier.testTag("drawer_syllabus")
                        )

                        DrawerMenuItem(
                            label = "Mock Tests",
                            icon = Icons.AutoMirrored.Outlined.Assignment,
                            selected = activeNavKey == "mocks",
                            onClick = {
                                onClose()
                                onSelectMocks()
                            },
                            modifier = Modifier.testTag("drawer_mocks")
                        )

                        DrawerMenuItem(
                            label = "Error Log / Mistakes",
                            icon = Icons.Outlined.ErrorOutline,
                            selected = activeNavKey == "errors",
                            onClick = {
                                onClose()
                                onSelectErrors()
                            },
                            modifier = Modifier.testTag("drawer_errors")
                        )

                        DrawerMenuItem(
                            label = "Study Squad",
                            icon = Icons.Outlined.Groups,
                            selected = activeNavKey == "squad",
                            onClick = {
                                onClose()
                                onSelectSquad()
                            },
                            modifier = Modifier.testTag("drawer_squad")
                        )
                    }

                    // Bottom Footer pinned to bottom of drawer
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DrawerMenuItem(
                        label = "Settings",
                        icon = Icons.Outlined.Settings,
                        selected = false,
                        onClick = {
                            onClose()
                            onOpenSettings()
                        },
                        modifier = Modifier.testTag("drawer_settings")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .clickable { onToggleTheme(!isDarkTheme) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("drawer_theme_toggle"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isDarkTheme) "Dark Mode" else "Light Mode",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = onToggleTheme
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.0.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
    )
}

@Composable
private fun DrawerMenuItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    trailingText: String? = null,
    trailingIcon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val iconColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = contentColor
                )
            }
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    style = MonoMetricSmall,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun DrawerSubItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
        )
    }
}
