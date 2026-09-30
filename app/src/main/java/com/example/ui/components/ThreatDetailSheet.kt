package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ThreatItem
import com.example.data.model.ThreatType
import com.example.ui.theme.TacticalBorderStrong
import com.example.ui.theme.TacticalBorderSubtle
import com.example.ui.theme.TacticalPitchBlack
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TacticalSurfaceMedium
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary
import com.example.ui.theme.TacticalWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ThreatDetailSheet(
    threat: ThreatItem,
    onDismiss: () -> Unit,
    onSaveToIncidentLog: (ThreatItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = TacticalSurfaceElevated,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderStrong),
        modifier = modifier
            .fillMaxWidth()
            .testTag("threat_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.Gray, RoundedCornerShape(2.dp))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Header Row: Target Designation, Type badge & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = TacticalWhite,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = threat.level.militaryCode,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TacticalPitchBlack,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = threat.designation,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TacticalWhite,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    Text(
                        text = threat.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TacticalTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_threat_sheet")
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "إغلاق نافذة التفاصيل",
                        tint = TacticalWhite
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = 1.dp,
                color = TacticalBorderSubtle
            )

            // Telemetry Grid
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryCard(
                        icon = Icons.Default.Explore,
                        title = "الموقع والقطاع",
                        value = "${threat.governorate.arabicName} - ${threat.sector}",
                        subValue = "LAT: ${String.format("%.4f", threat.latitude)}°N | LNG: ${String.format("%.4f", threat.longitude)}°E",
                        modifier = Modifier.weight(1f)
                    )

                    TelemetryCard(
                        icon = Icons.Default.Radar,
                        title = "محطة الرصد والاستطلاع",
                        value = threat.detectionSensor,
                        subValue = "وقت الرصد: " + SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(threat.detectedAt)),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (threat.type == ThreatType.DRONE || threat.type == ThreatType.MISSILE) {
                        TelemetryCard(
                            icon = Icons.Default.Speed,
                            title = "السرعة والارتفاع",
                            value = "${threat.speedKmh} كم/ساعـة",
                            subValue = "الارتفاع: ${threat.altitudeMeters} م | زاوية: ${threat.headingDegrees.toInt()}°",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    TelemetryCard(
                        icon = Icons.Default.Waves,
                        title = if (threat.type == ThreatType.JAMMING) "حزمة تردد التشويش" else "تردد الاتصال والتوجيه",
                        value = threat.frequencyBand.ifBlank { "غير محدد" },
                        subValue = if (threat.isVerified) "مؤكد سيبرانياً (100%)" else "قيد التحليل الطيفي",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Intelligence Notes / Threat Summary Box
            Surface(
                color = TacticalSurfaceMedium,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "التقييم الاستخباري الميداني:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TacticalWhite
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = threat.notes,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TacticalTextSecondary,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions Row: Save to incident log & Dismiss
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        onSaveToIncidentLog(threat)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TacticalWhite,
                        contentColor = TacticalPitchBlack
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_threat_to_log_button")
                ) {
                    Icon(
                        Icons.Default.BookmarkAdd,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = "توثيق في سجل البلاغات",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TacticalWhite
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderStrong),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("dismiss_threat_sheet_button")
                ) {
                    Text("إغلاق", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun TelemetryCard(
    icon: ImageVector,
    title: String,
    value: String,
    subValue: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = TacticalSurfaceMedium,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderSubtle),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = TacticalWhite,
                    modifier = Modifier.height(14.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TacticalTextSecondary,
                        fontSize = 10.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TacticalWhite
                ),
                maxLines = 1
            )
            Text(
                text = subValue,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.LightGray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                ),
                maxLines = 1
            )
        }
    }
}
