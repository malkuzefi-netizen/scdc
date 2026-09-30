package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SouthGovernorate
import com.example.data.model.ThreatItem
import com.example.data.model.ThreatLevel
import com.example.data.model.ThreatType
import com.example.ui.theme.TacticalBorderStrong
import com.example.ui.theme.TacticalBorderSubtle
import com.example.ui.theme.TacticalPitchBlack
import com.example.ui.theme.TacticalSurfaceDark
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TacticalSurfaceMedium
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary
import com.example.ui.theme.TacticalWhite
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManualCoordInputDialog(
    onDismiss: () -> Unit,
    onSubmitThreat: (ThreatItem) -> Unit
) {
    var threatType by remember { mutableStateOf(ThreatType.DRONE) }
    var threatLevel by remember { mutableStateOf(ThreatLevel.CRITICAL) }
    var selectedGov by remember { mutableStateOf(SouthGovernorate.ADEN) }

    var designation by remember { mutableStateOf("UAV-MAN-${Random.nextInt(100, 999)}") }
    var customName by remember { mutableStateOf("طائرة مسيرة انتحارية") }
    var sectorName by remember { mutableStateOf(selectedGov.keySectors.firstOrNull() ?: "القطاع المركزي") }

    var latText by remember { mutableStateOf(selectedGov.defaultLat.toString()) }
    var lngText by remember { mutableStateOf(selectedGov.defaultLng.toString()) }

    var speedText by remember { mutableStateOf("180") }
    var altText by remember { mutableStateOf("2100") }
    var freqText by remember { mutableStateOf("2.4 GHz FHSS") }
    var sensorText by remember { mutableStateOf("محطة رصد ميدانية - الجنوب") }
    var notesText by remember { mutableStateOf("تم إدخال الإحداثيات يدوياً من غرفة العمليات الميدانية.") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = TacticalSurfaceDark,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderStrong),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("manual_coord_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إدخال إحداثيات رصد يدوي",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TacticalWhite
                            )
                        )
                        Text(
                            text = "نظام توجيه البلاغات التكتيكية المباشرة",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TacticalTextSecondary
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_manual_coord_dialog")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "إلغاء",
                            tint = TacticalWhite
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = TacticalBorderSubtle
                )

                // Threat Type Selection
                Text(
                    text = "نوع التهديد:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TacticalWhite
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(ThreatType.DRONE, ThreatType.JAMMING, ThreatType.MISSILE).forEach { type ->
                        val isSelected = threatType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) TacticalWhite else TacticalSurfaceMedium,
                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) TacticalWhite else TacticalBorderSubtle,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    threatType = type
                                    when (type) {
                                        ThreatType.DRONE -> {
                                            customName = "طائرة مسيرة انتحارية"
                                            speedText = "180"
                                            altText = "2200"
                                            freqText = "2.4 GHz FHSS"
                                        }
                                        ThreatType.JAMMING -> {
                                            customName = "تشويش إلكتروني على الملاحة"
                                            speedText = "0"
                                            altText = "0"
                                            freqText = "GPS L1 (1575.42 MHz)"
                                        }
                                        ThreatType.MISSILE -> {
                                            customName = "صاروخ باليستي متوسط المدى"
                                            speedText = "2600"
                                            altText = "11500"
                                            freqText = "توجيه باليستي"
                                        }
                                        else -> {}
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.arabicName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) TacticalPitchBlack else TacticalWhite,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Governorate Selector
                Text(
                    text = "المحافظة الجنوبية المستهدفة / القطاع:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TacticalWhite
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SouthGovernorate.entries.forEach { gov ->
                        val isSelected = selectedGov == gov
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) TacticalWhite else TacticalSurfaceElevated,
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) TacticalWhite else TacticalBorderSubtle,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    selectedGov = gov
                                    latText = gov.defaultLat.toString()
                                    lngText = gov.defaultLng.toString()
                                    sectorName = gov.keySectors.firstOrNull() ?: "القطاع المركزي"
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = gov.arabicName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) TacticalPitchBlack else TacticalWhite,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sector / Landmark input
                OutlinedTextField(
                    value = sectorName,
                    onValueChange = { sectorName = it },
                    label = { Text("القطاع أو المنطقة المحددة", color = TacticalTextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TacticalWhite,
                        unfocusedTextColor = TacticalWhite,
                        focusedBorderColor = TacticalWhite,
                        unfocusedBorderColor = TacticalBorderSubtle,
                        focusedContainerColor = TacticalSurfaceMedium,
                        unfocusedContainerColor = TacticalSurfaceMedium
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_sector")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Coordinates Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it },
                        label = { Text("خط العرض (°N)", color = TacticalTextSecondary, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TacticalWhite,
                            unfocusedTextColor = TacticalWhite,
                            focusedBorderColor = TacticalWhite,
                            unfocusedBorderColor = TacticalBorderSubtle,
                            focusedContainerColor = TacticalSurfaceMedium,
                            unfocusedContainerColor = TacticalSurfaceMedium
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_lat")
                    )

                    OutlinedTextField(
                        value = lngText,
                        onValueChange = { lngText = it },
                        label = { Text("خط الطول (°E)", color = TacticalTextSecondary, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TacticalWhite,
                            unfocusedTextColor = TacticalWhite,
                            focusedBorderColor = TacticalWhite,
                            unfocusedBorderColor = TacticalBorderSubtle,
                            focusedContainerColor = TacticalSurfaceMedium,
                            unfocusedContainerColor = TacticalSurfaceMedium
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_lng")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Telemetry (Altitude / Speed / Frequency)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = speedText,
                        onValueChange = { speedText = it },
                        label = { Text("السرعة (كم/س)", color = TacticalTextSecondary, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TacticalWhite,
                            unfocusedTextColor = TacticalWhite,
                            focusedBorderColor = TacticalWhite,
                            unfocusedBorderColor = TacticalBorderSubtle,
                            focusedContainerColor = TacticalSurfaceMedium,
                            unfocusedContainerColor = TacticalSurfaceMedium
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_speed")
                    )

                    OutlinedTextField(
                        value = altText,
                        onValueChange = { altText = it },
                        label = { Text("الارتفاع (م)", color = TacticalTextSecondary, fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TacticalWhite,
                            unfocusedTextColor = TacticalWhite,
                            focusedBorderColor = TacticalWhite,
                            unfocusedBorderColor = TacticalBorderSubtle,
                            focusedContainerColor = TacticalSurfaceMedium,
                            unfocusedContainerColor = TacticalSurfaceMedium
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_alt")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Frequency / Signal Band
                OutlinedTextField(
                    value = freqText,
                    onValueChange = { freqText = it },
                    label = { Text("تردد الإشارة / حزمة التشويش", color = TacticalTextSecondary, fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TacticalWhite,
                        unfocusedTextColor = TacticalWhite,
                        focusedBorderColor = TacticalWhite,
                        unfocusedBorderColor = TacticalBorderSubtle,
                        focusedContainerColor = TacticalSurfaceMedium,
                        unfocusedContainerColor = TacticalSurfaceMedium
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_frequency")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Notes
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("ملاحظات البلاغ والاستطلاع", color = TacticalTextSecondary, fontSize = 11.sp) },
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TacticalWhite,
                        unfocusedTextColor = TacticalWhite,
                        focusedBorderColor = TacticalWhite,
                        unfocusedBorderColor = TacticalBorderSubtle,
                        focusedContainerColor = TacticalSurfaceMedium,
                        unfocusedContainerColor = TacticalSurfaceMedium
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_notes")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage!!,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val lat = latText.toDoubleOrNull()
                            val lng = lngText.toDoubleOrNull()

                            if (lat == null || lng == null) {
                                errorMessage = "يرجى إدخال إحداثيات صحيحة (أرقام عشرية)."
                                return@Button
                            }

                            if (lat < 11.0 || lat > 18.5 || lng < 42.0 || lng > 55.0) {
                                errorMessage = "الإحداثيات يجب أن تكون ضمن النطاق الجغرافي للجنوب العربي."
                                return@Button
                            }

                            val newThreat = ThreatItem(
                                id = "THR-MAN-${System.currentTimeMillis() % 10000}",
                                type = threatType,
                                level = threatLevel,
                                designation = designation,
                                name = customName,
                                latitude = lat,
                                longitude = lng,
                                altitudeMeters = altText.toIntOrNull() ?: 1500,
                                speedKmh = speedText.toIntOrNull() ?: 180,
                                frequencyBand = freqText,
                                headingDegrees = Random.nextInt(0, 360).toFloat(),
                                governorate = selectedGov,
                                sector = sectorName,
                                detectionSensor = sensorText,
                                detectedAt = System.currentTimeMillis(),
                                isVerified = true,
                                notes = notesText
                            )

                            onSubmitThreat(newThreat)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TacticalWhite,
                            contentColor = TacticalPitchBlack
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_manual_threat_button")
                    ) {
                        Text(
                            text = "إدراج وتعميم الإنذار",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TacticalWhite
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderStrong),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("cancel_manual_threat_button")
                    ) {
                        Text("إلغاء")
                    }
                }
            }
        }
    }
}
