package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SouthGovernorate
import com.example.data.model.ThreatItem
import com.example.data.model.ThreatType
import com.example.ui.theme.TacticalBorderStrong
import com.example.ui.theme.TacticalBorderSubtle
import com.example.ui.theme.TacticalPitchBlack
import com.example.ui.theme.TacticalSurfaceDark
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TacticalSurfaceMedium
import com.example.ui.theme.TacticalWhite
import kotlin.math.cos
import kotlin.math.sin

// Precise Bounds of Southern Yemen / South Arabian territory
private const val MIN_LNG = 42.5
private const val MAX_LNG = 54.8
private const val MIN_LAT = 11.5
private const val MAX_LAT = 17.8

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TacticalMapComponent(
    threats: List<ThreatItem>,
    selectedThreat: ThreatItem?,
    selectedFilter: ThreatType?,
    onFilterChange: (ThreatType?) -> Unit,
    onSelectThreat: (ThreatItem) -> Unit,
    onClearAllThreats: () -> Unit,
    onPurgeDecoys: () -> Unit,
    onResetSystem: () -> Unit,
    onTriggerRoutineSweep: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // Radar sweep rotation
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAngle"
    )

    // Pulse effect for EW jamming & alert rings
    val pulseRatio by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRatio"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TacticalPitchBlack)
            .testTag("tactical_map_container")
    ) {
        // Interactive Canvas - Clean Tactical Map (WITHOUT watermarks)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.8f, 5.0f)
                        offset += pan
                    }
                }
                .pointerInput(threats, scale, offset) {
                    detectTapGestures { tapOffset ->
                        val canvasWidth = size.width.toFloat()
                        val canvasHeight = size.height.toFloat()

                        for (threat in threats) {
                            val markerPos = projectGeoToCanvas(
                                threat.latitude,
                                threat.longitude,
                                canvasWidth,
                                canvasHeight,
                                scale,
                                offset
                            )
                            val dist = (tapOffset - markerPos).getDistance()
                            if (dist < 40f * scale.coerceAtMost(2f)) {
                                onSelectThreat(threat)
                                return@detectTapGestures
                            }
                        }
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 1. Draw minimal tactical coordinate grid lines
            drawTacticalGrid(canvasWidth, canvasHeight, scale, offset)

            // 2. Draw South Arabia geographic landmass & northern borders (NO watermarks)
            drawSouthArabiaGeography(canvasWidth, canvasHeight, scale, offset)

            // 3. Draw All Governorate Boundaries & Strategic Radar Nodes
            drawGovernoratesInternalBoundaries(canvasWidth, canvasHeight, scale, offset)

            // 4. Draw Northern Launch Origin Nodes (المخاء، الحديدة، صنعاء)
            drawNorthernLaunchNodes(canvasWidth, canvasHeight, scale, offset)

            // 5. Draw Command Radar Sweep (Centered on Aden Command post)
            val adenCommandPos = projectGeoToCanvas(
                12.80,
                45.03,
                canvasWidth,
                canvasHeight,
                scale,
                offset
            )
            drawRadarSweep(adenCommandPos, sweepAngle, canvasWidth, canvasHeight)

            // 6. Draw Threats on Map
            val filteredThreats = if (selectedFilter == null) threats else threats.filter { it.type == selectedFilter }
            for (threat in filteredThreats) {
                val pos = projectGeoToCanvas(
                    threat.latitude,
                    threat.longitude,
                    canvasWidth,
                    canvasHeight,
                    scale,
                    offset
                )

                val isSelected = selectedThreat?.id == threat.id

                drawThreatMarker(
                    threat = threat,
                    position = pos,
                    isSelected = isSelected,
                    pulseRatio = pulseRatio,
                    scale = scale
                )
            }
        }

        // Top HUD Overlay: Quick Operations Bar & Filter chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .align(Alignment.TopCenter)
        ) {
            // One-Click Fast Tactical Action Buttons Bar
            Surface(
                color = TacticalSurfaceMedium.copy(alpha = 0.94f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderStrong),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "لوحة التحكم السريع بالرادار والإنذارات",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TacticalWhite,
                                fontSize = 11.sp
                            )
                        )

                        Text(
                            text = "الأهداف النشطة: ${threats.size}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TacticalWhite,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Buttons Row: Clear Alerts, Purge Decoys, North Scenarios, Reset System
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. One-click Clear Alerts
                        Button(
                            onClick = onClearAllThreats,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TacticalWhite,
                                contentColor = TacticalPitchBlack
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("action_clear_alerts_btn")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حذف الإنذارات بضغطة واحدة", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // 2. Clean Radar of Decoys / Ghost targets
                        OutlinedButton(
                            onClick = onPurgeDecoys,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TacticalWhite
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderStrong),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("action_purge_decoys_btn")
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تنظيف الأهداف الوهمية والمخادعة", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // 3. Routine Radar Scan / Surveillance Sweep (الهجمات روتينية)
                        OutlinedButton(
                            onClick = onTriggerRoutineSweep,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TacticalWhite
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderStrong),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("action_routine_sweep_btn")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مسح راداري روتيني", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // 4. System Reset
                        OutlinedButton(
                            onClick = { showResetConfirmDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TacticalWhite
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderSubtle),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("action_reset_system_btn")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إعادة تهيئة النظام", fontSize = 10.sp)
                        }
                    }
                }
            }

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { onFilterChange(null) },
                    label = { Text("كافة الأهداف (${threats.size})", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TacticalWhite,
                        selectedLabelColor = TacticalPitchBlack,
                        containerColor = TacticalSurfaceElevated,
                        labelColor = TacticalWhite
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = TacticalBorderSubtle,
                        selectedBorderColor = TacticalWhite,
                        enabled = true,
                        selected = selectedFilter == null
                    ),
                    modifier = Modifier.testTag("filter_all")
                )

                FilterChip(
                    selected = selectedFilter == ThreatType.DRONE,
                    onClick = { onFilterChange(ThreatType.DRONE) },
                    label = { Text("طيران مسير", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TacticalWhite,
                        selectedLabelColor = TacticalPitchBlack,
                        containerColor = TacticalSurfaceElevated,
                        labelColor = TacticalWhite
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = TacticalBorderSubtle,
                        selectedBorderColor = TacticalWhite,
                        enabled = true,
                        selected = selectedFilter == ThreatType.DRONE
                    ),
                    modifier = Modifier.testTag("filter_drone")
                )

                FilterChip(
                    selected = selectedFilter == ThreatType.JAMMING,
                    onClick = { onFilterChange(ThreatType.JAMMING) },
                    label = { Text("تشويش", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TacticalWhite,
                        selectedLabelColor = TacticalPitchBlack,
                        containerColor = TacticalSurfaceElevated,
                        labelColor = TacticalWhite
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = TacticalBorderSubtle,
                        selectedBorderColor = TacticalWhite,
                        enabled = true,
                        selected = selectedFilter == ThreatType.JAMMING
                    ),
                    modifier = Modifier.testTag("filter_jamming")
                )

                FilterChip(
                    selected = selectedFilter == ThreatType.MISSILE,
                    onClick = { onFilterChange(ThreatType.MISSILE) },
                    label = { Text("صواريخ", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TacticalWhite,
                        selectedLabelColor = TacticalPitchBlack,
                        containerColor = TacticalSurfaceElevated,
                        labelColor = TacticalWhite
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = TacticalBorderSubtle,
                        selectedBorderColor = TacticalWhite,
                        enabled = true,
                        selected = selectedFilter == ThreatType.MISSILE
                    ),
                    modifier = Modifier.testTag("filter_missile")
                )
            }
        }

        // Map Control Floating Buttons (Zoom In, Zoom Out, Reset Center)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { scale = (scale * 1.3f).coerceAtMost(5.0f) },
                containerColor = TacticalSurfaceElevated,
                contentColor = TacticalWhite,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("map_zoom_in")
            ) {
                Icon(Icons.Default.Add, contentDescription = "تكبير الخريطة")
            }

            FloatingActionButton(
                onClick = { scale = (scale / 1.3f).coerceAtLeast(0.8f) },
                containerColor = TacticalSurfaceElevated,
                contentColor = TacticalWhite,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("map_zoom_out")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "تصغير الخريطة")
            }

            FloatingActionButton(
                onClick = {
                    scale = 1.0f
                    offset = Offset.Zero
                },
                containerColor = TacticalSurfaceElevated,
                contentColor = TacticalWhite,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("map_reset_center")
            ) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = "إعادة ضبط الخريطة")
            }
        }

        // Bottom Map Footer
        Surface(
            color = TacticalPitchBlack.copy(alpha = 0.9f),
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderSubtle),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, bottom = 10.dp)
        ) {
            Text(
                text = "خريطة قطاع الجنوب العربي | التكبير: ${String.format("%.1f", scale)}X",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = TacticalWhite
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }

    // System Reset Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text(
                    text = "تأكيد إعادة تهيئة النظام",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TacticalWhite
                    )
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في إعادة تهيئة النظام بالكامل؟ سيتم مسح كافة الأهداف الحالية وسجلات البلاغات ليعود النظام خالياً تماماً من أي بيانات.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TacticalWhite)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetSystem()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TacticalWhite,
                        contentColor = TacticalPitchBlack
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("نعم، إعادة التهيئة ومسح البيانات", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showResetConfirmDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TacticalWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderSubtle),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("إلغاء")
                }
            },
            containerColor = TacticalSurfaceDark,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

// Convert Geo coordinates (Lat, Lng) to Canvas coordinates with Pan & Zoom
fun projectGeoToCanvas(
    lat: Double,
    lng: Double,
    canvasWidth: Float,
    canvasHeight: Float,
    scale: Float,
    offset: Offset
): Offset {
    val normX = ((lng - MIN_LNG) / (MAX_LNG - MIN_LNG)).toFloat()
    val normY = ((MAX_LAT - lat) / (MAX_LAT - MIN_LAT)).toFloat()

    val centerX = canvasWidth / 2f
    val centerY = canvasHeight / 2f

    val rawX = normX * canvasWidth
    val rawY = normY * canvasHeight

    val transformedX = (rawX - centerX) * scale + centerX + offset.x
    val transformedY = (rawY - centerY) * scale + centerY + offset.y

    return Offset(transformedX, transformedY)
}

// Drawing Tactical Grid
private fun DrawScope.drawTacticalGrid(
    width: Float,
    height: Float,
    scale: Float,
    offset: Offset
) {
    val parallels = listOf(12.0, 13.0, 14.0, 15.0, 16.0, 17.0)
    val meridians = listOf(44.0, 46.0, 48.0, 50.0, 52.0, 54.0)

    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f)

    for (lat in parallels) {
        val p1 = projectGeoToCanvas(lat, MIN_LNG, width, height, scale, offset)
        val p2 = projectGeoToCanvas(lat, MAX_LNG, width, height, scale, offset)
        drawLine(
            color = Color(0x18FFFFFF),
            start = p1,
            end = p2,
            strokeWidth = 1f,
            pathEffect = dashEffect
        )
    }

    for (lng in meridians) {
        val p1 = projectGeoToCanvas(MIN_LAT, lng, width, height, scale, offset)
        val p2 = projectGeoToCanvas(MAX_LAT, lng, width, height, scale, offset)
        drawLine(
            color = Color(0x18FFFFFF),
            start = p1,
            end = p2,
            strokeWidth = 1f,
            pathEffect = dashEffect
        )
    }
}

// High Precision Geography of South Arabia & Demarcated Northern Border (NO watermarks)
private fun DrawScope.drawSouthArabiaGeography(
    width: Float,
    height: Float,
    scale: Float,
    offset: Offset
) {
    // 1. Coastline points along the Arabian Sea and Gulf of Aden
    val coastlinePoints = listOf(
        Pair(12.60, 43.45), // Bab el-Mandeb Strait
        Pair(12.65, 43.70), // Ras al-Arah / Subayhah
        Pair(12.72, 44.20), // Lahij coast
        Pair(12.76, 44.85), // Little Aden / Buraiqa
        Pair(12.80, 45.03), // Aden Peninsula / Crater & Shamsan
        Pair(12.90, 45.10), // Khor Maksar
        Pair(13.10, 45.40), // Abyan coast - Zinjibar
        Pair(13.35, 45.72), // Shuqrah
        Pair(13.55, 46.70), // Ahwar
        Pair(13.98, 48.18), // Balhaf LNG / Bir Ali (Shabwah)
        Pair(14.30, 48.95), // Burum
        Pair(14.54, 49.13), // Al-Mukalla port
        Pair(14.76, 49.60), // Ash-Shihr
        Pair(14.95, 50.15), // Al-Hami & Ad-Dis
        Pair(15.20, 51.25), // Sayhut (Al-Mahrah)
        Pair(15.42, 51.70), // Qishn
        Pair(15.65, 52.25), // Ras Fartak
        Pair(16.21, 52.18), // Nishtun & Al-Ghaydah
        Pair(16.65, 53.10)  // Hawf / Oman Border
    )

    // 2. Official Northern Historical Border points of South Arabia (حدود الجنوب العربي مع الشمال)
    val northernDemarcationBorder = listOf(
        Pair(16.65, 53.10), // Hawf
        Pair(17.30, 53.00), // North Mahrah / Shahan
        Pair(17.50, 51.50), // Northern Empty Quarter frontier
        Pair(17.10, 49.50), // North Hadramawt / Thamud
        Pair(16.50, 48.50), // Wadi Hadramawt north
        Pair(15.50, 46.50), // North Shabwah / Ramlat as-Sab'atayn
        Pair(14.90, 45.80), // Beihan / Harib border
        Pair(14.10, 45.40), // Mukayras / Lawdar border
        Pair(13.85, 44.75), // Moris / Qa'atabah (North Ad-Dali')
        Pair(13.35, 44.35), // Kirsh / Subayhah border
        Pair(12.60, 43.45)  // Back to Bab el-Mandeb
    )

    // Complete Polygon (Coast + Northern border)
    val completeSouthPoints = coastlinePoints + northernDemarcationBorder.drop(1)

    val landPath = Path()
    completeSouthPoints.forEachIndexed { index, (lat, lng) ->
        val pos = projectGeoToCanvas(lat, lng, width, height, scale, offset)
        if (index == 0) {
            landPath.moveTo(pos.x, pos.y)
        } else {
            landPath.lineTo(pos.x, pos.y)
        }
    }
    landPath.close()

    // Draw Land Mass fill
    drawPath(
        path = landPath,
        color = Color(0xFF101010)
    )

    // Draw Coastline (Crisp white stroke)
    val coastPath = Path()
    coastlinePoints.forEachIndexed { idx, (lat, lng) ->
        val pos = projectGeoToCanvas(lat, lng, width, height, scale, offset)
        if (idx == 0) coastPath.moveTo(pos.x, pos.y) else coastPath.lineTo(pos.x, pos.y)
    }
    drawPath(
        path = coastPath,
        color = Color(0xFFE0E0E0),
        style = Stroke(
            width = (2.0f * scale).coerceIn(1.5f, 3.5f),
            cap = StrokeCap.Round
        )
    )

    // Draw Prominent Northern Demarcation Border of South Arabia (حدود الجنوب العربي)
    val northBorderPath = Path()
    northernDemarcationBorder.forEachIndexed { idx, (lat, lng) ->
        val pos = projectGeoToCanvas(lat, lng, width, height, scale, offset)
        if (idx == 0) northBorderPath.moveTo(pos.x, pos.y) else northBorderPath.lineTo(pos.x, pos.y)
    }
    drawPath(
        path = northBorderPath,
        color = Color.White,
        style = Stroke(
            width = (2.8f * scale).coerceIn(2.0f, 4.5f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f),
            cap = StrokeCap.Square
        )
    )

    // Draw Border Label on the northern demarcation line
    val borderLabelPt = projectGeoToCanvas(14.3, 45.2, width, height, scale, offset)
    val borderPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = (9f * scale).coerceIn(8f, 13f)
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawText("=== حدود الجنوب العربي ===", borderLabelPt.x, borderLabelPt.y, borderPaint)

    // Islands:
    // 1. Socotra Archipelago (سقطرى)
    val socotraPoints = listOf(
        Pair(12.35, 53.30),
        Pair(12.55, 53.60),
        Pair(12.68, 54.00),
        Pair(12.55, 54.45),
        Pair(12.35, 54.20),
        Pair(12.25, 53.80),
        Pair(12.35, 53.30)
    )
    val socotraPath = Path()
    socotraPoints.forEachIndexed { idx, (lat, lng) ->
        val pt = projectGeoToCanvas(lat, lng, width, height, scale, offset)
        if (idx == 0) socotraPath.moveTo(pt.x, pt.y) else socotraPath.lineTo(pt.x, pt.y)
    }
    socotraPath.close()
    drawPath(path = socotraPath, color = Color(0xFF141414))
    drawPath(path = socotraPath, color = Color.White, style = Stroke(width = 1.8f * scale))

    // 2. Abd al Kuri island
    val abdKuriPt = projectGeoToCanvas(12.18, 52.23, width, height, scale, offset)
    drawCircle(color = Color.White, radius = 3.5f * scale, center = abdKuriPt)

    // 3. Mayyun (Perim) island in Bab el-Mandeb
    val mayyunPt = projectGeoToCanvas(12.65, 43.41, width, height, scale, offset)
    drawCircle(color = Color.White, radius = 3.5f * scale, center = mayyunPt)
}

// Draw internal boundaries separating the 8 Southern Governorates
private fun DrawScope.drawGovernoratesInternalBoundaries(
    width: Float,
    height: Float,
    scale: Float,
    offset: Offset
) {
    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)

    // Accurate divider paths between all 8 Southern governorates
    val governorateBoundaries = listOf(
        // Aden - Lahij border
        listOf(Pair(12.92, 44.80), Pair(12.82, 45.05), Pair(12.95, 45.18)),
        // Lahij - Ad Dali border
        listOf(Pair(13.25, 44.40), Pair(13.48, 44.65), Pair(13.60, 44.80)),
        // Lahij - Abyan border
        listOf(Pair(13.05, 45.05), Pair(13.35, 45.20), Pair(13.65, 45.30)),
        // Ad Dali - Abyan border
        listOf(Pair(13.60, 44.85), Pair(13.80, 45.10)),
        // Abyan - Shabwah border
        listOf(Pair(13.65, 46.60), Pair(14.10, 46.30), Pair(14.70, 46.00)),
        // Shabwah - Hadramawt border
        listOf(Pair(14.00, 48.20), Pair(14.90, 47.90), Pair(16.50, 47.60)),
        // Hadramawt - Al Mahrah border
        listOf(Pair(15.20, 51.00), Pair(16.00, 51.10), Pair(17.40, 51.30))
    )

    for (line in governorateBoundaries) {
        val p1 = projectGeoToCanvas(line[0].first, line[0].second, width, height, scale, offset)
        for (i in 1 until line.size) {
            val p2 = projectGeoToCanvas(line[i].first, line[i].second, width, height, scale, offset)
            drawLine(
                color = Color(0x99FFFFFF),
                start = p1,
                end = p2,
                strokeWidth = (1.5f * scale).coerceIn(1.2f, 2.5f),
                pathEffect = dashedEffect
            )
        }
    }

    // Strategic Radar Nodes & Early Warning Sites
    val strategicRadars = listOf(
        Triple("رادار مطار عدن الدولي [RADAR-ADE]", 12.8295, 45.0288),
        Triple("رادار جبل شمسان [RADAR-SHM]", 12.78, 45.02),
        Triple("مرصد رأس العارة [RADAR-ARA]", 12.62, 43.55),
        Triple("رادار قاعدة العند [RADAR-AND]", 13.19, 44.75)
    )

    val radarPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = (8.5f * scale).coerceIn(8f, 13f)
        typeface = android.graphics.Typeface.MONOSPACE
        isAntiAlias = true
    }

    for ((radarName, rLat, rLng) in strategicRadars) {
        val rPos = projectGeoToCanvas(rLat, rLng, width, height, scale, offset)

        // Draw tactical radar diamond with dual concentric pulse
        drawCircle(
            color = Color.White,
            radius = 4f * scale.coerceAtMost(2f),
            center = rPos
        )
        drawCircle(
            color = Color(0xBBFFFFFF),
            radius = 9f * scale.coerceAtMost(2f),
            center = rPos,
            style = Stroke(1.4f)
        )
        drawCircle(
            color = Color(0x44FFFFFF),
            radius = 16f * scale.coerceAtMost(2f),
            center = rPos,
            style = Stroke(1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f))
        )

        drawContext.canvas.nativeCanvas.drawText(
            radarName,
            rPos.x + 10f,
            rPos.y + 4f,
            radarPaint
        )
    }

    // Governorate Command Centers & Sector Labels
    val govCenters = listOf(
        Triple("العاصمة عدن [HQ]", 12.80, 45.03),
        Triple("لحج", 13.06, 44.88),
        Triple("الضالع", 13.70, 44.73),
        Triple("أبين", 13.50, 45.80),
        Triple("شبوة", 14.53, 46.83),
        Triple("حضرموت - المكلا", 14.54, 49.13),
        Triple("وادي حضرموت - سيئون", 15.93, 48.79),
        Triple("المهرة - الغيضة", 16.21, 52.18),
        Triple("سقطرى - حديبو", 12.46, 53.82)
    )

    val labelPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = (9.5f * scale).coerceIn(9f, 15f)
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    for ((name, lat, lng) in govCenters) {
        val pos = projectGeoToCanvas(lat, lng, width, height, scale, offset)

        // Draw radar node diamond
        drawCircle(
            color = Color.White,
            radius = 3.5f * scale.coerceAtMost(2f),
            center = pos
        )
        drawCircle(
            color = Color(0x88FFFFFF),
            radius = 7f * scale.coerceAtMost(2f),
            center = pos,
            style = Stroke(1.2f)
        )

        drawContext.canvas.nativeCanvas.drawText(
            name,
            pos.x + 8f,
            pos.y - 3f,
            labelPaint
        )
    }
}

// Draw Northern Launch Origin Nodes (المخاء، الحديدة، صنعاء)
private fun DrawScope.drawNorthernLaunchNodes(
    width: Float,
    height: Float,
    scale: Float,
    offset: Offset
) {
    val northNodes = listOf(
        Triple("منصة المخاء [شمال]", 13.31, 43.25),
        Triple("منصة الحديدة [شمال]", 14.79, 42.95),
        Triple("منصة صنعاء [شمال]", 15.35, 44.20)
    )

    val northPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = (8.5f * scale).coerceIn(8f, 13f)
        typeface = android.graphics.Typeface.MONOSPACE
        isAntiAlias = true
    }

    for ((label, lat, lng) in northNodes) {
        val pos = projectGeoToCanvas(lat, lng, width, height, scale, offset)

        // Crosshairs target icon
        val r = 5f * scale.coerceAtMost(2f)
        drawLine(Color.White, Offset(pos.x - r, pos.y), Offset(pos.x + r, pos.y), 1.2f)
        drawLine(Color.White, Offset(pos.x, pos.y - r), Offset(pos.x, pos.y + r), 1.2f)
        drawCircle(Color(0x88FFFFFF), radius = r, center = pos, style = Stroke(1f))

        drawContext.canvas.nativeCanvas.drawText(
            label,
            pos.x + 8f,
            pos.y + 3f,
            northPaint
        )
    }
}

private fun DrawScope.drawRadarSweep(
    center: Offset,
    angle: Float,
    canvasWidth: Float,
    canvasHeight: Float
) {
    val radarRadius = (canvasWidth.coerceAtLeast(canvasHeight) * 0.45f)

    // Concentric tactical range rings
    val ringR = listOf(0.25f, 0.5f, 0.75f, 1.0f)
    for (rRatio in ringR) {
        drawCircle(
            color = Color(0x18FFFFFF),
            radius = radarRadius * rRatio,
            center = center,
            style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f))
        )
    }

    // Crosshair axis
    drawLine(
        color = Color(0x20FFFFFF),
        start = Offset(center.x - radarRadius, center.y),
        end = Offset(center.x + radarRadius, center.y),
        strokeWidth = 1f
    )
    drawLine(
        color = Color(0x20FFFFFF),
        start = Offset(center.x, center.y - radarRadius),
        end = Offset(center.x, center.y + radarRadius),
        strokeWidth = 1f
    )

    // Sweeping beam line with phosphor trail
    rotate(degrees = angle, pivot = center) {
        drawLine(
            color = Color(0x99FFFFFF),
            start = center,
            end = Offset(center.x + radarRadius, center.y),
            strokeWidth = 1.8f
        )

        drawArc(
            color = Color(0x12FFFFFF),
            startAngle = -45f,
            sweepAngle = 45f,
            useCenter = true,
            topLeft = Offset(center.x - radarRadius, center.y - radarRadius),
            size = Size(radarRadius * 2, radarRadius * 2)
        )
    }
}

private fun DrawScope.drawThreatMarker(
    threat: ThreatItem,
    position: Offset,
    isSelected: Boolean,
    pulseRatio: Float,
    scale: Float
) {
    val markerSize = (14f * scale).coerceIn(12f, 26f)

    if (threat.isDecoy) {
        // Distinct Ghost / Decoy drawing (Dashed Box with Question/Ghost icon)
        drawRect(
            color = Color.White,
            topLeft = Offset(position.x - markerSize / 2, position.y - markerSize / 2),
            size = Size(markerSize, markerSize),
            style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f), 0f))
        )

        drawCircle(
            color = Color(0x66FFFFFF),
            radius = markerSize * 0.9f * pulseRatio,
            center = position,
            style = Stroke(1f)
        )

        val ghostPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = (9f * scale).coerceIn(8f, 13f)
            typeface = android.graphics.Typeface.MONOSPACE
            isAntiAlias = true
        }

        drawContext.canvas.nativeCanvas.drawText(
            "[وهمي/مخادع] ${threat.designation}",
            position.x + markerSize + 4f,
            position.y + 4f,
            ghostPaint
        )
        return
    }

    when (threat.type) {
        ThreatType.DRONE -> {
            val headingRad = Math.toRadians((threat.headingDegrees - 90.0)).toFloat()
            val tipX = position.x + cos(headingRad) * (markerSize * 1.5f)
            val tipY = position.y + sin(headingRad) * (markerSize * 1.5f)

            drawLine(
                color = Color.White,
                start = position,
                end = Offset(tipX, tipY),
                strokeWidth = 2f
            )

            drawRect(
                color = if (isSelected) Color.White else Color(0xEEFFFFFF),
                topLeft = Offset(position.x - markerSize / 2, position.y - markerSize / 2),
                size = Size(markerSize, markerSize),
                style = Stroke(width = if (isSelected) 3f else 1.8f)
            )

            drawCircle(
                color = Color.White,
                radius = 3f,
                center = position
            )

            drawCircle(
                color = Color(0x88FFFFFF),
                radius = markerSize * (1.2f + pulseRatio * 0.5f),
                center = position,
                style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f), 0f))
            )
        }

        ThreatType.JAMMING -> {
            val maxJammingRadius = markerSize * 2.8f
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = position
            )

            drawCircle(
                color = Color(0x99FFFFFF),
                radius = maxJammingRadius * pulseRatio,
                center = position,
                style = Stroke(width = 1.5f)
            )

            val secondPulse = (pulseRatio + 0.5f) % 1.0f
            drawCircle(
                color = Color(0x55FFFFFF),
                radius = maxJammingRadius * secondPulse,
                center = position,
                style = Stroke(width = 1f)
            )

            drawLine(
                color = Color.White,
                start = Offset(position.x - markerSize / 2, position.y - markerSize / 2),
                end = Offset(position.x + markerSize / 2, position.y + markerSize / 2),
                strokeWidth = 1.5f
            )
            drawLine(
                color = Color.White,
                start = Offset(position.x - markerSize / 2, position.y + markerSize / 2),
                end = Offset(position.x + markerSize / 2, position.y - markerSize / 2),
                strokeWidth = 1.5f
            )
        }

        ThreatType.MISSILE -> {
            val trajectoryRad = Math.toRadians((threat.headingDegrees - 90.0)).toFloat()
            val leadX = position.x + cos(trajectoryRad) * (markerSize * 2.8f)
            val leadY = position.y + sin(trajectoryRad) * (markerSize * 2.8f)

            drawLine(
                color = Color.White,
                start = position,
                end = Offset(leadX, leadY),
                strokeWidth = 2.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f), 0f)
            )

            val p1 = Offset(position.x, position.y - markerSize)
            val p2 = Offset(position.x - markerSize * 0.7f, position.y + markerSize * 0.7f)
            val p3 = Offset(position.x + markerSize * 0.7f, position.y + markerSize * 0.7f)

            val missilePath = Path().apply {
                moveTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
                close()
            }

            drawPath(path = missilePath, color = Color.White)
            drawPath(path = missilePath, color = Color.Black, style = Stroke(width = 1.2f))

            drawCircle(
                color = Color(0xCCFFFFFF),
                radius = markerSize * (1.5f + pulseRatio * 0.5f),
                center = position,
                style = Stroke(width = 2f)
            )
        }

        ThreatType.SURVEILLANCE_RADAR -> {
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = position
            )
            drawArc(
                color = Color.White,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(position.x - markerSize, position.y - markerSize),
                size = Size(markerSize * 2, markerSize * 2),
                style = Stroke(width = 1.5f)
            )
        }
    }

    val tagPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = (9.5f * scale).coerceIn(8.5f, 13f)
        typeface = android.graphics.Typeface.MONOSPACE
        isAntiAlias = true
    }

    val originTag = if (threat.launchOrigin.isNotBlank()) "[من:${threat.launchOrigin}] " else ""
    val tagText = "$originTag${threat.type.code}: ${threat.designation}"
    drawContext.canvas.nativeCanvas.drawText(
        tagText,
        position.x + markerSize + 4f,
        position.y + 4f,
        tagPaint
    )
}
