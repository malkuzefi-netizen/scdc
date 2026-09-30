package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.ManageAccountsDialog
import com.example.ui.components.ManualCoordInputDialog
import com.example.ui.components.TacticalMapComponent
import com.example.ui.components.ThreatDetailSheet
import com.example.ui.screens.CounterJammingScreen
import com.example.ui.screens.IncidentLogScreen
import com.example.ui.screens.IntelligenceReportScreen
import com.example.ui.screens.MilitaryLoginScreen
import com.example.ui.screens.TacticalMessagingScreen
import com.example.ui.theme.TacticalBorderStrong
import com.example.ui.theme.TacticalBorderSubtle
import com.example.ui.theme.TacticalPitchBlack
import com.example.ui.theme.TacticalSurfaceDark
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TacticalSurfaceMedium
import com.example.ui.theme.TacticalWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TacticalMainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()

    // If user is not authenticated, strictly show gatekeeper military login screen
    if (currentUser == null) {
        MilitaryLoginScreen(
            onLogin = { u, p -> viewModel.login(u, p) },
            errorMessage = loginError
        )
        return
    }

    val user = currentUser!!
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val activeThreats by viewModel.activeThreats.collectAsStateWithLifecycle()
    val selectedThreat by viewModel.selectedThreat.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.threatFilter.collectAsStateWithLifecycle()
    val incidents by viewModel.incidents.collectAsStateWithLifecycle()
    val isLiveStreamEnabled by viewModel.isLiveStreamEnabled.collectAsStateWithLifecycle()
    val systemAlertBanner by viewModel.systemAlertBanner.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()

    val isCounterJammingActive by viewModel.isCounterJammingActive.collectAsStateWithLifecycle()
    val jammingPowerWatts by viewModel.jammingPowerWatts.collectAsStateWithLifecycle()
    val selectedEcmMode by viewModel.selectedEcmMode.collectAsStateWithLifecycle()
    val selectedTargetSector by viewModel.selectedTargetSector.collectAsStateWithLifecycle()
    val selectedFrequencyBand by viewModel.selectedFrequencyBand.collectAsStateWithLifecycle()

    var showManualInputDialog by remember { mutableStateOf(false) }
    var showManageAccountsDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TacticalPitchBlack,
        contentColor = TacticalWhite,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = TacticalPitchBlack,
                            border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderStrong),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.cyber_directorate_badge),
                                contentDescription = "شعار مديرية الأمن السيبراني الجنوبي",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "مديرية الأمن السيبراني الجنوبي",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TacticalWhite,
                                        fontSize = 13.sp
                                    )
                                )

                                Surface(
                                    color = TacticalSurfaceElevated,
                                    shape = RoundedCornerShape(3.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderSubtle)
                                ) {
                                    Text(
                                        text = "${user.militaryRank}: ${user.fullName}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TacticalWhite
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            Text(
                                text = "مركز الرصد والاستطلاع // قطاع الجنوب العربي",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.LightGray,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Routine Threat Radar Sweep Button (الهجمات روتينية)
                    IconButton(
                        onClick = { viewModel.triggerRoutineRadarSweep() },
                        modifier = Modifier.testTag("action_routine_sweep")
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "مسح راداري روتيني",
                            tint = TacticalWhite
                        )
                    }

                    // Manage Personnel Accounts Button (Enabled for Commander / Super Admin)
                    if (user.isMasterAdmin || user.role == "COMMANDER") {
                        IconButton(
                            onClick = { showManageAccountsDialog = true },
                            modifier = Modifier.testTag("action_manage_accounts")
                        ) {
                            Icon(
                                Icons.Default.ManageAccounts,
                                contentDescription = "إدارة الحسابات العسكرية ومنح الصلاحيات",
                                tint = TacticalWhite
                            )
                        }
                    }

                    // Manual Coordinate Input Button
                    IconButton(
                        onClick = { showManualInputDialog = true },
                        modifier = Modifier.testTag("action_manual_input")
                    ) {
                        Icon(
                            Icons.Default.AddLocationAlt,
                            contentDescription = "إدخال إحداثيات رصد يدوي",
                            tint = TacticalWhite
                        )
                    }

                    // Sync Alert from Central Server
                    IconButton(
                        onClick = { viewModel.fetchNewServerAlert() },
                        modifier = Modifier.testTag("action_fetch_server_alert")
                    ) {
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = "استقبال إنذار من الخادم المركزي",
                            tint = TacticalWhite
                        )
                    }

                    // Logout / Lock App Button
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("action_logout")
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "قفل التطبيق وتسجيل الخروج",
                            tint = Color.LightGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TacticalPitchBlack,
                    titleContentColor = TacticalWhite,
                    actionIconContentColor = TacticalWhite
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = TacticalSurfaceDark,
                contentColor = TacticalWhite,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = TacticalBorderSubtle
                )
            ) {
                NavigationBarItem(
                    selected = currentScreen == ScreenState.TacticalMap,
                    onClick = { viewModel.navigateTo(ScreenState.TacticalMap) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (activeThreats.isNotEmpty()) {
                                    Badge(
                                        containerColor = TacticalWhite,
                                        contentColor = TacticalPitchBlack
                                    ) {
                                        Text(
                                            "${activeThreats.size}",
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Map, contentDescription = "الخريطة التكتيكية")
                        }
                    },
                    label = { Text("الخريطة التكتيكية", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TacticalPitchBlack,
                        selectedTextColor = TacticalWhite,
                        indicatorColor = TacticalWhite,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_tactical_map")
                )

                NavigationBarItem(
                    selected = currentScreen == ScreenState.IncidentLog,
                    onClick = { viewModel.navigateTo(ScreenState.IncidentLog) },
                    icon = {
                        BadgedBox(
                            badge = {
                                val activeCount = incidents.count { !it.isResolved }
                                if (activeCount > 0) {
                                    Badge(
                                        containerColor = TacticalWhite,
                                        contentColor = TacticalPitchBlack
                                    ) {
                                        Text(
                                            "$activeCount",
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ListAlt, contentDescription = "سجل البلاغات")
                        }
                    },
                    label = { Text("البلاغات", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TacticalPitchBlack,
                        selectedTextColor = TacticalWhite,
                        indicatorColor = TacticalWhite,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_incident_log")
                )

                NavigationBarItem(
                    selected = currentScreen == ScreenState.CounterJamming,
                    onClick = { viewModel.navigateTo(ScreenState.CounterJamming) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (isCounterJammingActive) {
                                    Badge(
                                        containerColor = TacticalWhite,
                                        contentColor = TacticalPitchBlack
                                    ) {
                                        Text(
                                            "نشط",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.WifiTethering, contentDescription = "التشويش العكسي")
                        }
                    },
                    label = { Text("التشويش العكسي", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TacticalPitchBlack,
                        selectedTextColor = TacticalWhite,
                        indicatorColor = TacticalWhite,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_counter_jamming")
                )

                NavigationBarItem(
                    selected = currentScreen == ScreenState.TacticalMessaging,
                    onClick = { viewModel.navigateTo(ScreenState.TacticalMessaging) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (messages.isNotEmpty()) {
                                    Badge(
                                        containerColor = TacticalWhite,
                                        contentColor = TacticalPitchBlack
                                    ) {
                                        Text(
                                            "${messages.size}",
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Forum, contentDescription = "التراسل والبرقيات المشفرة")
                        }
                    },
                    label = { Text("التراسل", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TacticalPitchBlack,
                        selectedTextColor = TacticalWhite,
                        indicatorColor = TacticalWhite,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_tactical_messaging")
                )

                NavigationBarItem(
                    selected = currentScreen == ScreenState.IntelligenceReport,
                    onClick = { viewModel.navigateTo(ScreenState.IntelligenceReport) },
                    icon = {
                        Icon(Icons.Default.Assessment, contentDescription = "التقارير")
                    },
                    label = { Text("التقرير", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TacticalPitchBlack,
                        selectedTextColor = TacticalWhite,
                        indicatorColor = TacticalWhite,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("nav_report")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                is ScreenState.TacticalMap -> {
                    TacticalMapComponent(
                        threats = activeThreats,
                        selectedThreat = selectedThreat,
                        selectedFilter = selectedFilter,
                        onFilterChange = { viewModel.setFilter(it) },
                        onSelectThreat = { viewModel.selectThreat(it) },
                        onClearAllThreats = { viewModel.clearAllActiveThreats() },
                        onPurgeDecoys = { viewModel.purgeDecoysAndGhostTargets() },
                        onResetSystem = { viewModel.resetSystemToCleanState() },
                        onTriggerRoutineSweep = { viewModel.triggerRoutineRadarSweep() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is ScreenState.IncidentLog -> {
                    IncidentLogScreen(
                        incidents = incidents,
                        onUpdateNotes = { id, notes -> viewModel.updateIncidentNotes(id, notes) },
                        onToggleResolved = { id, res -> viewModel.toggleIncidentResolved(id, res) },
                        onNavigateToReport = { viewModel.navigateTo(ScreenState.IntelligenceReport) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is ScreenState.CounterJamming -> {
                    CounterJammingScreen(
                        isActive = isCounterJammingActive,
                        powerWatts = jammingPowerWatts,
                        ecmMode = selectedEcmMode,
                        targetSector = selectedTargetSector,
                        frequencyBand = selectedFrequencyBand,
                        onToggleActive = { viewModel.toggleCounterJamming() },
                        onPowerChange = { viewModel.setJammingPower(it) },
                        onModeChange = { viewModel.setEcmMode(it) },
                        onSectorChange = { viewModel.setTargetSector(it) },
                        onFrequencyBandChange = { viewModel.setFrequencyBand(it) },
                        onTriggerEmpPulse = { viewModel.triggerHighPowerEmpPulse() },
                        onBack = { viewModel.navigateTo(ScreenState.TacticalMap) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is ScreenState.TacticalMessaging -> {
                    TacticalMessagingScreen(
                        currentUser = user,
                        messages = messages,
                        allAccounts = allAccounts,
                        onSendMessage = { channel, content, receiverId, receiverName, classification, isUrgent ->
                            viewModel.sendTacticalMessage(
                                channel = channel,
                                content = content,
                                receiverId = receiverId,
                                receiverName = receiverName,
                                classification = classification,
                                isUrgent = isUrgent
                            )
                        },
                        onDeleteMessage = { viewModel.deleteTacticalMessage(it) },
                        onClearAllMessages = { viewModel.clearAllTacticalMessages() },
                        onBack = { viewModel.navigateTo(ScreenState.TacticalMap) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is ScreenState.IntelligenceReport -> {
                    IntelligenceReportScreen(
                        incidents = incidents,
                        onBack = { viewModel.navigateTo(ScreenState.IncidentLog) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Real-time System Alert HUD Banner (Top Floating notification)
            AnimatedVisibility(
                visible = systemAlertBanner != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                systemAlertBanner?.let { message ->
                    Surface(
                        color = TacticalWhite,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TacticalPitchBlack),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.clearBanner() }
                            .testTag("system_alert_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = TacticalPitchBlack,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TacticalPitchBlack,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            IconButton(
                                onClick = { viewModel.clearBanner() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "إغلاق الإشعار",
                                    tint = TacticalPitchBlack
                                )
                            }
                        }
                    }
                }
            }

            // Selected Threat Bottom Detail Sheet
            selectedThreat?.let { threat ->
                ThreatDetailSheet(
                    threat = threat,
                    onDismiss = { viewModel.selectThreat(null) },
                    onSaveToIncidentLog = { viewModel.saveThreatToIncidentLog(it) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    // Manual Threat Coordinates Input Dialog
    if (showManualInputDialog) {
        ManualCoordInputDialog(
            onDismiss = { showManualInputDialog = false },
            onSubmitThreat = { threat ->
                viewModel.addManualThreat(threat)
                showManualInputDialog = false
            }
        )
    }

    // Commander Accounts & Personnel Management Dialog
    if (showManageAccountsDialog) {
        ManageAccountsDialog(
            currentUser = user,
            accounts = allAccounts,
            onCreateAccount = { username, fullName, title, pass, rank, role, sector ->
                viewModel.createPersonnelAccount(username, fullName, title, pass, rank, role, sector)
            },
            onUpdateAccount = { id, username, fullName, title, pass, rank, role, sector ->
                viewModel.updatePersonnelAccount(id, username, fullName, title, pass, rank, role, sector)
            },
            onDeleteAccount = { viewModel.deletePersonnelAccount(it) },
            onDismiss = { showManageAccountsDialog = false }
        )
    }
}
