package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun IntelligenceReportScreen(
    incidents: List<IncidentEntity>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val reportDate = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(Date())

    val totalCount = incidents.size
    val droneCount = incidents.count { it.threatType == ThreatType.DRONE.name }
    val jammingCount = incidents.count { it.threatType == ThreatType.JAMMING.name }
    val missileCount = incidents.count { it.threatType == ThreatType.MISSILE.name }
    val activeCount = incidents.count { !it.isResolved }

    val formattedReportText = buildString {
        appendLine("==================================================")
        appendLine("مديرية الأمن السيبراني الجنوبي")
        appendLine("الإدارة العامة للرصد والإنذار المبكر - قطاع الجنوب العربي")
        appendLine("تقرير الموقف الاستخباري والعملياتي للتهديدات الجوية والإلكترونية")
        appendLine("تصنيف الوثيقة: سري للغاية // للاستخدام العملياتي فقط")
        appendLine("تاريخ وتوقيت الإصدار: $reportDate")
        appendLine("==================================================")
        appendLine()
        appendLine("أولاً: إحصائيات الرصد الشاملة:")
        appendLine("- إجمالي الأهداف والتهديدات المرصودة: $totalCount")
        appendLine("- طيران مسير (UAV): $droneCount")
        appendLine("- عمليات تشويش كهرومغناطيسي وحرب إلكترونية (EW): $jammingCount")
        appendLine("- صواريخ ومقذوفات باليستية وموجهة (BML): $missileCount")
        appendLine("- أهداف قيد المتابعة والإنذار النشط: $activeCount")
        appendLine()
        appendLine("ثانياً: التوزيع الجغرافي حسب المحافظات الجنوبية:")
        SouthGovernorate.entries.forEach { gov ->
            val count = incidents.count { it.governorate == gov.arabicName }
            appendLine("- ${gov.arabicName}: $count بلاغات")
        }
        appendLine()
        appendLine("ثالثاً: تفاصيل البلاغات والأهداف المرصودة:")
        incidents.forEachIndexed { index, inc ->
            appendLine("[$index] كود الهدف: ${inc.targetCode} | النوع: ${inc.threatType}")
            appendLine("     التسمية: ${inc.designation}")
            appendLine("     الموقع: ${inc.governorate} - ${inc.sector}")
            appendLine("     الإحداثيات: ${inc.latitude}°N, ${inc.longitude}°E")
            appendLine("     السرعة والارتفاع: ${inc.speedKmh} كم/س | ${inc.altitudeMeters} م")
            appendLine("     الحزمة الترددية: ${inc.frequencyBand}")
            appendLine("     محطة الرصد: ${inc.detectionSensor}")
            if (inc.operatorNotes.isNotBlank()) {
                appendLine("     ملاحظات الضابط: ${inc.operatorNotes}")
            }
            appendLine("     الحالة: ${if (inc.isResolved) "تم استكمال التوثيق" else "قيد التتبع النشط"}")
            appendLine("--------------------------------------------------")
        }
        appendLine()
        appendLine("رابعاً: التقييم الأمني والتوجيهات الاحترازية:")
        appendLine("1. الحفاظ على أعلى درجات الجاهزية والاستطلاع الكهروبصري والراداري في قطاعات العاصمة عدن، باب المندب، الساحل الغربي، وشبوة وحضرموت.")
        appendLine("2. استمرار فحص الترددات المشبوهة والتصدي لعمليات التشويش على أنظمة الملاحة البحرية والجوية عبر شبكات الاتصال المشفرة البديلة.")
        appendLine("3. تعميم إشعارات الإنذار المبكر على نقاط الدفاع المدني وأمن الموانئ والمطارات.")
        appendLine("==================================================")
        appendLine("صدر عن: غرفة العمليات المركزية - مديرية الأمن السيبراني الجنوبي")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TacticalPitchBlack)
            .padding(14.dp)
            .testTag("intelligence_report_screen")
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("report_back_button")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "الرجوع",
                    tint = TacticalWhite
                )
            }

            Text(
                text = "تقرير استخباراتي رسمي",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TacticalWhite
                )
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Copy Button
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Classified Military Report", formattedReportText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "تم نسخ التقرير بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("copy_report_button")
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "نسخ التقرير",
                        tint = TacticalWhite
                    )
                }

                // Share Button
                IconButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, formattedReportText)
                            putExtra(Intent.EXTRA_SUBJECT, "تقرير الإنذار المبكر - مديرية الأمن السيبراني الجنوبي")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "مشاركة التقرير الاستخباراتي"))
                    },
                    modifier = Modifier.testTag("share_report_button")
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "مشاركة وتصدير",
                        tint = TacticalWhite
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Printable / Displayable Tactical Document Sheet
        Surface(
            color = TacticalSurfaceDark,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderStrong),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .testTag("report_document_container")
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Document Official Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "مديرية الأمن السيبراني الجنوبي",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TacticalWhite
                            )
                        )
                        Text(
                            text = "الإدارة العامة للرصد والإنذار المبكر - الجنوب العربي",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TacticalTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "تاريخ التقرير: $reportDate",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        )
                    }

                    // Redacted / Classified Stamp
                    Box(
                        modifier = Modifier
                            .background(TacticalWhite, RoundedCornerShape(4.dp))
                            .border(1.dp, TacticalWhite, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "سري للغاية",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TacticalPitchBlack,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = TacticalWhite,
                    thickness = 2.dp
                )

                // Metric Cards Grid
                Text(
                    text = "1. المؤشرات الإحصائية العامة:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TacticalWhite
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportStatBox(title = "إجمالي التهديدات", value = "$totalCount", modifier = Modifier.weight(1f))
                    ReportStatBox(title = "طيران مسير", value = "$droneCount", modifier = Modifier.weight(1f))
                    ReportStatBox(title = "حرب إلكترونية", value = "$jammingCount", modifier = Modifier.weight(1f))
                    ReportStatBox(title = "صواريخ ومقذوفات", value = "$missileCount", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Governorate Breakdown Table
                Text(
                    text = "2. التوزيع الجغرافي للأهداف (محافظات الجنوب):",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TacticalWhite
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = TacticalSurfaceMedium,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        SouthGovernorate.entries.forEach { gov ->
                            val count = incidents.count { it.governorate == gov.arabicName }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = gov.arabicName,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TacticalWhite,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = "$count بلاغ",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (count > 0) TacticalWhite else Color.Gray,
                                        fontWeight = if (count > 0) FontWeight.Bold else FontWeight.Normal,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detailed Incidents Chronological Table
                Text(
                    text = "3. تفاصيل الأهداف المرصودة وملاحظات الاستطلاع:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TacticalWhite
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                incidents.forEachIndexed { idx, inc ->
                    Surface(
                        color = TacticalSurfaceElevated,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "#${idx + 1} - ${inc.targetCode} [${inc.threatType}]",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TacticalWhite,
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Text(
                                    text = inc.governorate,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TacticalWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Text(
                                text = "${inc.designation} (${inc.sector})",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TacticalTextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            )

                            Text(
                                text = "الإحداثيات: ${inc.latitude}°N, ${inc.longitude}°E | المستشعر: ${inc.detectionSensor}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.LightGray,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp
                                )
                            )

                            if (inc.operatorNotes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "ملاحظات الرصد: ${inc.operatorNotes}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TacticalWhite,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Strategic Directives
                Text(
                    text = "4. الخلاصة والتوجيهات العملياتية:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TacticalWhite
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = TacticalSurfaceMedium,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "• استمرار الرصد والمسح الكهرومغناطيسي للممرات الملاحية في خليج عدن وباب المندب.\n• استنفار منظومات الإنذار المبكر التابعة للدفاع المدني بالمحافظات الجنوبية.\n• الالتزام الصارم بقواعد الاتصال المؤمن والترددات البديلة عند رصد أنشطة تشويش إلكتروني.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TacticalTextPrimary,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Export Button Inside Document
                Button(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, formattedReportText)
                            putExtra(Intent.EXTRA_SUBJECT, "تقرير الإنذار المبكر - مديرية الأمن السيبراني الجنوبي")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "مشاركة التقرير الاستخباراتي"))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TacticalWhite,
                        contentColor = TacticalPitchBlack
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_report_button")
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "مشاركة وتصدير التقرير الاستخباري الكامل",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportStatBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = TacticalSurfaceMedium,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderStrong),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TacticalWhite,
                    fontFamily = FontFamily.Monospace
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TacticalTextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
        }
    }
}
