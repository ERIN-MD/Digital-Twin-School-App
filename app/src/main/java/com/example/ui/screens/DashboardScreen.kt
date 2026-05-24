package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SchoolViewModel

@Composable
fun DashboardScreen(
    viewModel: SchoolViewModel,
    onNavigateToTwin: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToSchedule: () -> Unit
) {
    val studentsList by viewModel.students.collectAsState()
    val teachersList by viewModel.teachers.collectAsState()
    val powerCons by viewModel.totalPowerKW.collectAsState()
    val avgTemp by viewModel.averageTemperature.collectAsState()
    val activeDevices by viewModel.activeDevicesCount.collectAsState()

    val presentCount = studentsList.count { it.attendanceStatus == "حاضر" }
    val totalStudents = studentsList.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        // Image of the school on the left (since Arabic is RTL, Left is start, Right is end)
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img),
                            contentDescription = "School Logo",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(6.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            modifier = Modifier.weight(1.5f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "مرحباً بكم في منصة المدرسة الذكية",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Right
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "النظام الرقمي والتوأم التفاعلي المتكامل لمدرستنا",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }
            }
        }

        // Live Environment Metrics (Digital Twin Panel)
        item {
            Text(
                text = "مؤشرات التوأم الرقمي والبيئة الذكية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Power usage telemetry
                TelemetryItemCard(
                    modifier = Modifier.weight(1f),
                    title = "معدل استهلاك الطاقة",
                    value = "$powerCons kW",
                    icon = Icons.Default.Bolt,
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    description = "استهلاك نشط للأجهزة"
                )
                // Temp telemetry
                TelemetryItemCard(
                    modifier = Modifier.weight(1f),
                    title = "متوسط الحرارة",
                    value = "$avgTemp °C",
                    icon = Icons.Default.DeviceThermostat,
                    iconColor = Color(0xFFE11D48),
                    description = "تكييف القاعات مركزي"
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Active Devices
                TelemetryItemCard(
                    modifier = Modifier.weight(1f),
                    title = "الأجهزة النشطة",
                    value = "$activeDevices / 10",
                    icon = Icons.Default.SettingsInputComponent,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    description = "شاشات، تكييف، إضاءة"
                )
                // Students attendance status
                TelemetryItemCard(
                    modifier = Modifier.weight(1f),
                    title = "الحضور اليومي",
                    value = if (totalStudents > 0) "$presentCount / $totalStudents" else "0",
                    icon = Icons.Default.HowToReg,
                    iconColor = Color(0xFF10B981),
                    description = if (totalStudents > 0) "${(presentCount * 100 / totalStudents)}% نسبة حضور الطلاب" else "0%"
                )
            }
        }

        // Dynamic chart representation using Canvas
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "منحنى استهلاك الطاقة اليومي (الافتراضي)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        EnergyWaveChart(
                            lineColor = MaterialTheme.colorScheme.primary,
                            areaColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "04:00 م",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "12:00 م",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "08:00 ص",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // Fast Action Buttons
        item {
            Text(
                text = "وصول سريع للخدمات",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "التحكم بالتوأم وتوفير الطاقة",
                    icon = Icons.Default.CompassCalibration,
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.secondary,
                    onClick = onNavigateToTwin
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "تسجيل الحضور والغياب",
                    icon = Icons.Default.AssignmentInd,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToStudents
                )
            }
        }

        // Live Notices
        item {
            Text(
                text = "الإعلانات والتنبيهات العامة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        items(getNoticeBoardItems()) { notice ->
            NoticeItemCard(notice = notice)
        }
    }
}

@Composable
fun TelemetryItemCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    description: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Right,
                maxLines = 1
            )
        }
    }
}

@Composable
fun EnergyWaveChart(lineColor: Color, areaColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val points = listOf(
            Offset(0f, height * 0.8f),
            Offset(width * 0.15f, height * 0.75f),
            Offset(width * 0.3f, height * 0.45f),
            Offset(width * 0.45f, height * 0.3f),
            Offset(width * 0.6f, height * 0.5f),
            Offset(width * 0.75f, height * 0.25f),
            Offset(width * 0.9f, height * 0.4f),
            Offset(width, height * 0.6f)
        )

        // Draw background area
        val areaPath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val current = points[i]
                val prev = points[i - 1]
                val control1 = Offset((prev.x + current.x) / 2f, prev.y)
                val control2 = Offset((prev.x + current.x) / 2f, current.y)
                cubicTo(control1.x, control1.y, control2.x, control2.y, current.x, current.y)
            }
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(path = areaPath, color = areaColor)

        // Draw line
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val current = points[i]
                val prev = points[i - 1]
                val control1 = Offset((prev.x + current.x) / 2f, prev.y)
                val control2 = Offset((prev.x + current.x) / 2f, current.y)
                cubicTo(control1.x, control1.y, control2.x, control2.y, current.x, current.y)
            }
        }
        drawPath(path = linePath, color = lineColor, style = Stroke(width = 3.dp.toPx()))

        // Draw dots
        points.forEachIndexed { index, point ->
            if (index == 3 || index == 5) {
                drawCircle(color = lineColor, radius = 5.dp.toPx(), center = point)
                drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = point)
            }
        }
    }
}

@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                maxLines = 2
            )
        }
    }
}

@Composable
fun NoticeItemCard(notice: NoticeItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notice.isAlert) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time
            Text(
                text = notice.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            // Content
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            ) {
                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (notice.isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notice.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Right
                )
            }

            // Dot or Icon
            Icon(
                imageVector = if (notice.isAlert) Icons.Default.Warning else Icons.Default.Campaign,
                contentDescription = null,
                tint = if (notice.isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

data class NoticeItem(
    val title: String,
    val body: String,
    val date: String,
    val isAlert: Boolean = false
)

fun getNoticeBoardItems() = listOf(
    NoticeItem(
        title = "تحذير: استهلاك عالي للطاقة!",
        body = "تنبيه من التوأم الرقمي: تم الكشف عن إضاءة وتكييف نشط بقاعة 102 وهى فارغة حالياً. يرجى إطفاؤها لتوفير الطاقة.",
        date = "اليوم، 11:32 ص",
        isAlert = true
    ),
    NoticeItem(
        title = "بدء التسجيل للامتحانات العملية",
        body = "يرجى من جميع طلاب الصفوف تسجيل أسمائهم لتوزيع لجان اختبارات المواد التطبيقية بمختبر العلوم والمختبر التقني.",
        date = "أمس، 09:15 ص"
    ),
    NoticeItem(
        title = "تعديل جدول الحصص الأسبوعي",
        body = "قسم إدارة الجداول أضاف مادة الحاسب الآلي يوم الإثنين للحصة الثالثة كحصة مهارات وتطبيقات عملية متطورة.",
        date = "22 مايو"
    )
)
