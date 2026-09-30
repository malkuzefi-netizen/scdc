package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TacticalBorderStrong
import com.example.ui.theme.TacticalBorderSubtle
import com.example.ui.theme.TacticalPitchBlack
import com.example.ui.theme.TacticalSurfaceDark
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TacticalSurfaceMedium
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary
import com.example.ui.theme.TacticalWhite
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CounterJammingScreen(
    isActive: Boolean,
    powerWatts: Int,
    ecmMode: String,
    targetSector: String,
    frequencyBand: String,
    onToggleActive: () -> Unit,
    onPowerChange: (Int) -> Unit,
    onModeChange: (String) -> Unit,
    onSectorChange: (String) -> Unit,
    onFrequencyBandChange: (String) -> Unit,
    onTriggerEmpPulse: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val infiniteTransition = rememberInfiniteTransition(label = "EcmAnimation")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TacticalPitchBlack)
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("counter_jamming_screen")
    ) {
        // 1. Header Bar
        Surface(
            color = TacticalSurfaceDark,
            border = BorderStroke(1.dp, TacticalBorderStrong),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isActive) TacticalWhite else TacticalSurfaceElevated,
                        border = BorderStroke(1.dp, TacticalBorderStrong),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            Icons.Default.WifiTethering,
                            contentDescription = null,
                            tint = if (isActive) TacticalPitchBlack else Color.LightGray,
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxSize()
                        )
                    }

                    Column {
                        Text(
                            text = "منظومة التشويش الإلكتروني العكسي (ECM)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TacticalWhite
                            )
                        )
                        Text(
                            text = "سلاح الحرب الإلكترونية والتصدي الكهرومغناطيسي // الجنوب العربي",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TacticalTextSecondary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Surface(
                    color = if (isActive) TacticalWhite else TacticalPitchBlack,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, TacticalBorderStrong)
                ) {
                    Text(
                        text = if (isActive) "البث نشط // 100%" else "وضع الاستعداد",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) TacticalPitchBlack else Color.LightGray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Master Toggle & Immediate EMP Pulse Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onToggleActive,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) TacticalWhite else TacticalSurfaceElevated,
                    contentColor = if (isActive) TacticalPitchBlack else TacticalWhite
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, TacticalBorderStrong),
                modifier = Modifier
                    .weight(1.3f)
                    .height(52.dp)
                    .testTag("toggle_counter_jamming_button")
            ) {
                Icon(
                    Icons.Default.PowerSettingsNew,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isActive) "إيقاف التشويش العكسي" else "تفعيل التشويش العكسي فوراً",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = onTriggerEmpPulse,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TacticalPitchBlack,
                    contentColor = TacticalWhite
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.5.dp, TacticalWhite),
                modifier = Modifier
                    .weight(1.1f)
                    .height(52.dp)
                    .testTag("trigger_emp_pulse_button")
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "نبضة إخماد قصوى",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Interactive HUD Dashboards: Power Intensity Gauge + RF Spectrum Analyzer
        Surface(
            color = TacticalSurfaceDark,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, TacticalBorderStrong),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = null,
                            tint = TacticalWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "لوحة قياس شدة وقوة التشويش الكهرومغناطيسي",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TacticalWhite
                            )
                        )
                    }

                    Text(
                        text = "$powerWatts W",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = TacticalWhite
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom Canvas Gauge Dial
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeW = 12f
                        val center = Offset(size.width / 2f, size.height * 0.85f)
                        val radius = (size.width.coerceAtMost(size.height * 2f) / 2f) - 30f

                        // Background Arc (180 degrees from 180 to 360)
                        drawArc(
                            color = Color(0x33FFFFFF),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(strokeW, cap = StrokeCap.Round)
                        )

                        // Active Power Arc
                        val powerFraction = (powerWatts / 2000f).coerceIn(0f, 1f)
                        val sweep = 180f * powerFraction
                        drawArc(
                            color = if (isActive) Color.White else Color(0x77FFFFFF),
                            startAngle = 180f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(strokeW, cap = StrokeCap.Round)
                        )

                        // Needle angle
                        val needleAngle = 180f + sweep
                        val rad = needleAngle * (PI / 180.0)
                        val needleLen = radius - 8f
                        val endX = center.x + (needleLen * cos(rad)).toFloat()
                        val endY = center.y + (needleLen * sin(rad)).toFloat()

                        drawLine(
                            color = Color.White,
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                        drawCircle(color = Color.White, radius = 6f, center = center)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        Text(
                            text = if (isActive) "نسبة الإخماد: ${(85 + (powerWatts / 140)).coerceAtMost(99)}%" else "في وضع الجاهزية",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TacticalWhite,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Power Intensity Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "50W",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color.LightGray
                        )
                    )

                    Slider(
                        value = powerWatts.toFloat(),
                        onValueChange = { onPowerChange(it.toInt()) },
                        valueRange = 50f..2000f,
                        steps = 39,
                        colors = SliderDefaults.colors(
                            thumbColor = TacticalWhite,
                            activeTrackColor = TacticalWhite,
                            inactiveTrackColor = TacticalSurfaceElevated
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("jamming_power_slider")
                    )

                    Text(
                        text = "2000W",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color.LightGray
                        )
                    )
                }

                // Metrics Row (Range, SNR, J/S Ratio)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricBox(
                        label = "مدى التغطية الفعال",
                        value = "${(powerWatts / 32).coerceAtLeast(12)} كم"
                    )
                    MetricBox(
                        label = "نسبة التشويش/الإشارة (J/S)",
                        value = "+${(12 + (powerWatts / 100))} dB"
                    )
                    MetricBox(
                        label = "حالة الهوائيات",
                        value = if (isActive) "نشط وموجه" else "جاهزية"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Live RF Spectrum & Waveform Analyzer
        Surface(
            color = TacticalSurfaceDark,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, TacticalBorderStrong),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = TacticalWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "محلل الطيف الكهرومغناطيسي الحي (RF Spectrum)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TacticalWhite
                            )
                        )
                    }

                    Text(
                        text = frequencyBand.take(14),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.LightGray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Canvas Spectrum Waveform
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(TacticalPitchBlack, RoundedCornerShape(6.dp))
                        .border(1.dp, TacticalBorderSubtle, RoundedCornerShape(6.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Grid lines
                        val gridPath = PathEffect.dashPathEffect(floatArrayOf(3f, 6f), 0f)
                        drawLine(Color(0x22FFFFFF), Offset(0f, h * 0.25f), Offset(w, h * 0.25f), pathEffect = gridPath)
                        drawLine(Color(0x22FFFFFF), Offset(0f, h * 0.5f), Offset(w, h * 0.5f), pathEffect = gridPath)
                        drawLine(Color(0x22FFFFFF), Offset(0f, h * 0.75f), Offset(w, h * 0.75f), pathEffect = gridPath)

                        drawLine(Color(0x22FFFFFF), Offset(w * 0.25f, 0f), Offset(w * 0.25f, h), pathEffect = gridPath)
                        drawLine(Color(0x22FFFFFF), Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), pathEffect = gridPath)
                        drawLine(Color(0x22FFFFFF), Offset(w * 0.75f, 0f), Offset(w * 0.75f, h), pathEffect = gridPath)

                        // Draw live RF waveform
                        val path = Path()
                        val steps = 80
                        val stepW = w / steps

                        for (i in 0..steps) {
                            val x = i * stepW
                            val normX = i / steps.toFloat()
                            val wave1 = sin(normX * 12f + phase) * (if (isActive) 22f else 6f)
                            val wave2 = cos(normX * 24f - phase * 1.5f) * (if (isActive) 14f else 3f)
                            val noise = if (isActive) ((i % 3) - 1) * 8f * pulseGlow else 0f
                            val y = (h / 2f) + wave1 + wave2 + noise

                            if (i == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = if (isActive) Color.White else Color(0x88FFFFFF),
                            style = Stroke(width = if (isActive) 2.2f else 1.2f, cap = StrokeCap.Round)
                        )
                    }

                    Text(
                        text = if (isActive) "● بث طاقة تشويش نشطة (Active Jamming Jammer Signal)" else "○ مراقبة الطيف الساكن (Passive Spectrum)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isActive) TacticalWhite else Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Operational Counter-Jamming Modes (أوضاع التشويش العكسي)
        Surface(
            color = TacticalSurfaceDark,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, TacticalBorderStrong),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "وضع التشويش والتصدي الكهرومغناطيسي:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TacticalWhite
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                val modes = listOf(
                    "إخماد روابط التحكم C2 بالمسيرات" to "قطع اتصال التوجيه بالطائرات المسيرة وإجبارها على الهبوط أو العودة",
                    "تشويش إعماء اتجاهي واسع النطاق" to "إغراق قطاع الهدف بحزمة ضوضاء رادارية تحجب الرؤية الكهرومغناطيسية",
                    "تشويش خداعي كهرومغناطيسي" to "توليد أهداف رادارية وهمية مصطنعة وتضليل مجسات الرصد المعادية",
                    "درع الحماية والتثبيت الملاحي" to "تحصين إشارات الملاحة وتثبيت GPS ضد هجمات التزييف والخداع (Spoofing)"
                )

                modes.forEach { (modeTitle, modeDesc) ->
                    val isSelected = ecmMode == modeTitle
                    Surface(
                        color = if (isSelected) TacticalSurfaceElevated else TacticalSurfaceDark,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (isSelected) TacticalWhite else TacticalBorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onModeChange(modeTitle) }
                            .testTag("mode_${modeTitle.take(6)}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(if (isSelected) TacticalWhite else TacticalPitchBlack, CircleShape)
                                    .border(1.dp, TacticalWhite, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(TacticalPitchBlack, CircleShape)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = modeTitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TacticalWhite
                                    )
                                )
                                Text(
                                    text = modeDesc,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TacticalTextSecondary,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 6. Frequency Band Selector (نطاقات التردد المستهدفة)
        Surface(
            color = TacticalSurfaceDark,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, TacticalBorderStrong),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "نطاق التردد المستهدف بالتشويش:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TacticalWhite
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                val bands = listOf(
                    "2.4 GHz / 5.8 GHz C2",
                    "GPS L1 / GLONASS (1.5 GHz)",
                    "UHF / VHF Tactical Net",
                    "X-Band 8-12 GHz Radar"
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    bands.forEach { band ->
                        val isSelected = frequencyBand == band
                        Surface(
                            color = if (isSelected) TacticalWhite else TacticalSurfaceElevated,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (isSelected) TacticalWhite else TacticalBorderSubtle),
                            modifier = Modifier.clickable { onFrequencyBandChange(band) }
                        ) {
                            Text(
                                text = band,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) TacticalPitchBlack else TacticalWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 7. Target Operational Sector (القطاع المستهدف بالتشويش)
        Surface(
            color = TacticalSurfaceDark,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, TacticalBorderStrong),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "القطاع الميداني المستهدف بالتصدي:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TacticalWhite
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                val sectors = listOf(
                    "العاصمة عدن ومحيطها",
                    "ممر باب المندب ورأس العارة",
                    "محور الضالع وجبهات التماس",
                    "قطاع بيحان وعسيلان - شبوة",
                    "محور ساحل حضرموت والمكلا"
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sectors.forEach { sec ->
                        val isSelected = targetSector == sec
                        Surface(
                            color = if (isSelected) TacticalWhite else TacticalSurfaceElevated,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (isSelected) TacticalWhite else TacticalBorderSubtle),
                            modifier = Modifier.clickable { onSectorChange(sec) }
                        ) {
                            Text(
                                text = sec,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) TacticalPitchBlack else TacticalWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun MetricBox(label: String, value: String) {
    Surface(
        color = TacticalSurfaceElevated,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, TacticalBorderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    color = Color.LightGray
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TacticalWhite,
                    fontSize = 11.sp
                )
            )
        }
    }
}
