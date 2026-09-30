package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.IncidentEntity
import com.example.data.model.SouthGovernorate
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IncidentLogScreen(
    incidents: List<IncidentEntity>,
    onUpdateNotes: (Long, String) -> Unit,
    onToggleResolved: (Long, Boolean) -> Unit,
    onNavigateToReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGovFilter by remember { mutableStateOf<String?>(null) }
    var selectedTypeFilter by remember { mutableStateOf<String?>(null) }

    // Dialog state for adding/editing operator notes
    var editingIncident by remember { mutableStateOf<IncidentEntity?>(null) }

    val filteredIncidents = incidents.filter { item ->
        val matchesGov = selectedGovFilter == null || item.governorate == selectedGovFilter
        val matchesType = selectedTypeFilter == null || item.threatType == selectedTypeFilter
        val matchesSearch = searchQuery.isBlank() ||
                item.targetCode.contains(searchQuery, ignoreCase = true) ||
                item.designation.contains(searchQuery, ignoreCase = true) ||
                item.governorate.contains(searchQuery, ignoreCase = true) ||
                item.sector.contains(searchQuery, ignoreCase = true) ||
                item.operatorNotes.contains(searchQuery, ignoreCase = true)

        matchesGov && matchesType && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TacticalPitchBlack)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("incident_log_screen")
    ) {
        // Screen Header with Generate Report Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "سجل البلاغات والإنذارات الاستخباراتية",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TacticalWhite
                    )
                )
                Text(
                    text = "الأرشيف العملياتي لمديرية الأمن السيبراني الجنوبي",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TacticalTextSecondary,
                        fontSize = 11.sp
                    )
                )
            }

            Button(
                onClick = onNavigateToReport,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TacticalWhite,
                    contentColor = TacticalPitchBlack
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("generate_report_button")
            ) {
                Icon(
                    Icons.Default.Assessment,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = "إعداد التقرير",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث بالكود، المنطقة، أو التهديد...", color = Color.Gray, fontSize = 12.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = TacticalWhite)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "مسح", tint = TacticalWhite)
                    }
                }
            },
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
                .testTag("search_incident_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Threat Type Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedTypeFilter == null,
                onClick = { selectedTypeFilter = null },
                label = { Text("الكل", fontSize = 11.sp) },
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
                    selected = selectedTypeFilter == null
                )
            )

            ThreatType.entries.forEach { type ->
                val isSelected = selectedTypeFilter == type.name
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTypeFilter = if (isSelected) null else type.name },
                    label = { Text(type.arabicName, fontSize = 11.sp) },
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
                        selected = isSelected
                    )
                )
            }
        }

        // Governorate Filter Chips (Scrollable/Wrap)
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SouthGovernorate.entries.forEach { gov ->
                val isSelected = selectedGovFilter == gov.arabicName
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) TacticalWhite else TacticalSurfaceDark,
                            RoundedCornerShape(4.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) TacticalWhite else TacticalBorderSubtle,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable {
                            selectedGovFilter = if (isSelected) null else gov.arabicName
                        }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
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

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 6.dp),
            color = TacticalBorderSubtle
        )

        // Count Indicator
        Text(
            text = "إجمالي البلاغات المسجلة: ${filteredIncidents.size} بلاغ عملياتي",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.LightGray,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Incidents List
        if (filteredIncidents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Radar,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لا توجد بلاغات تطابق معايير التصفية الحالية",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("incident_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredIncidents, key = { it.id }) { incident ->
                    IncidentCard(
                        incident = incident,
                        onEditNotes = { editingIncident = incident },
                        onToggleResolved = { onToggleResolved(incident.id, !incident.isResolved) }
                    )
                }
            }
        }
    }

    // Edit Notes Dialog
    editingIncident?.let { inc ->
        EditNotesDialog(
            incident = inc,
            onDismiss = { editingIncident = null },
            onSave = { updatedNotes ->
                onUpdateNotes(inc.id, updatedNotes)
                editingIncident = null
            }
        )
    }
}

@Composable
private fun IncidentCard(
    incident: IncidentEntity,
    onEditNotes: () -> Unit,
    onToggleResolved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.getDefault()).format(Date(incident.timestamp))

    Surface(
        color = TacticalSurfaceMedium,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderStrong),
        modifier = modifier
            .fillMaxWidth()
            .testTag("incident_card_${incident.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Target Code, Status, and Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = TacticalWhite,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = incident.threatType,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TacticalPitchBlack,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = incident.targetCode,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TacticalWhite,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                // Resolved status chip
                Box(
                    modifier = Modifier
                        .background(
                            if (incident.isResolved) TacticalSurfaceElevated else TacticalWhite,
                            RoundedCornerShape(4.dp)
                        )
                        .border(
                            1.dp,
                            TacticalWhite,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { onToggleResolved() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (incident.isResolved) "تم التوثيق والإنهاء" else "قيد المتابعة النشطة",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (incident.isResolved) TacticalWhite else TacticalPitchBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Designation
            Text(
                text = incident.designation,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TacticalWhite
                )
            )

            // Location
            Text(
                text = "${incident.governorate} // ${incident.sector}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TacticalTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Telemetry Readouts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "إحداثيات: ${String.format("%.3f", incident.latitude)}°N , ${String.format("%.3f", incident.longitude)}°E",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = Color.LightGray
                    )
                )

                if (incident.speedKmh > 0) {
                    Text(
                        text = "السرعة: ${incident.speedKmh} كم/س",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = Color.LightGray
                        )
                    )
                }
            }

            Text(
                text = "المستشعر: ${incident.detectionSensor} | $dateStr",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            )

            // Operator Notes Box
            if (incident.operatorNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = TacticalSurfaceDark,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "ملاحظات الضابط الميداني:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TacticalWhite,
                                fontSize = 10.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = incident.operatorNotes,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TacticalTextPrimary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action row: Edit Notes Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onEditNotes,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TacticalWhite
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderSubtle),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("edit_notes_button_${incident.id}")
                ) {
                    Icon(
                        Icons.Default.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة / تعديل الملاحظات", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun EditNotesDialog(
    incident: IncidentEntity,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var notesText by remember { mutableStateOf(incident.operatorNotes) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = TacticalSurfaceDark,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderStrong),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("edit_notes_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "تدوين ملاحظات استخباراتية ميدانية",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TacticalWhite
                    )
                )
                Text(
                    text = "البلاغ: ${incident.targetCode} - ${incident.governorate}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TacticalTextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("الملاحظات الاستخبارية والعملياتية", color = TacticalTextSecondary) },
                    minLines = 4,
                    maxLines = 8,
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
                        .testTag("notes_textarea")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onSave(notesText) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TacticalWhite,
                            contentColor = TacticalPitchBlack
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_notes_submit_button")
                    ) {
                        Text("حفظ وتوثيق الملاحظات", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TacticalWhite
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderStrong),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("إلغاء")
                    }
                }
            }
        }
    }
}
