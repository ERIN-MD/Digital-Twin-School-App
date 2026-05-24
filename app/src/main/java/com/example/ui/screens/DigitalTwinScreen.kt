package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClassroomDevice
import com.example.ui.SchoolViewModel

@Composable
fun DigitalTwinScreen(viewModel: SchoolViewModel) {
    val devicesList by viewModel.devices.collectAsState()
    val selectedClass by viewModel.selectedClassroom.collectAsState()
    val totalPower by viewModel.totalPowerKW.collectAsState()
    val activeDevicesCount by viewModel.activeDevicesCount.collectAsState()

    // Key stats
    val classrooms = listOf(
        ClassroomInfo("class_1", "الصف الأول", "قاعة 101"),
        ClassroomInfo("class_2", "الصف الثاني", "قاعة 102"),
        ClassroomInfo("lab", "مختبر العلوم", "مركزي"),
        ClassroomInfo("library", "المكتبة", "طابق 1")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("digital_twin_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper status dashboard
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "$totalPower kW",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "إجمالي الطاقة المسحوبة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "لوحة محاكاة التوأم الرقمي للمدرسة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = "تحكم تفاعلي مباشر بأنظمة الطاقة وتكييف الصفوف",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Right
                        )
                    }
                }
            }
        }

        // Section header for floor plan
        item {
            Text(
                text = "المخطط الجغرافي والبيئي للمدرسة (ثنائي الأبعاد)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Classroom Floor Grid (Sensory Visualization Panel)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // School Structure Grid layout
                Text(
                    text = "مبنى قاعات المحاكاة الإلكترونية",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Class 1 Box
                    ClassroomBentoBox(
                        modifier = Modifier.weight(1f),
                        info = classrooms[0],
                        isActiveClass = selectedClass == classrooms[0].id,
                        devices = devicesList.filter { it.classroomId == classrooms[0].id },
                        onSelect = { viewModel.selectClassroom(classrooms[0].id) }
                    )
                    // Class 2 Box
                    ClassroomBentoBox(
                        modifier = Modifier.weight(1f),
                        info = classrooms[1],
                        isActiveClass = selectedClass == classrooms[1].id,
                        devices = devicesList.filter { it.classroomId == classrooms[1].id },
                        onSelect = { viewModel.selectClassroom(classrooms[1].id) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Science Lab Box
                    ClassroomBentoBox(
                        modifier = Modifier.weight(1f),
                        info = classrooms[2],
                        isActiveClass = selectedClass == classrooms[2].id,
                        devices = devicesList.filter { it.classroomId == classrooms[2].id },
                        onSelect = { viewModel.selectClassroom(classrooms[2].id) }
                    )
                    // Library Box
                    ClassroomBentoBox(
                        modifier = Modifier.weight(1f),
                        info = classrooms[3],
                        isActiveClass = selectedClass == classrooms[3].id,
                        devices = devicesList.filter { it.classroomId == classrooms[3].id },
                        onSelect = { viewModel.selectClassroom(classrooms[3].id) }
                    )
                }
            }
        }

        // Section header for selected classroom controls
        item {
            val selectedRoomName = classrooms.find { it.id == selectedClass }?.name ?: ""
            Text(
                text = "التحكم ببيئة صف: $selectedRoomName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Active items in selected classroom
        val activeClassroomDevices = devicesList.filter { it.classroomId == selectedClass }
        if (activeClassroomDevices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = "لا توجد أجهزة متصلة بهذا الصف حالياً",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }
            }
        } else {
            items(activeClassroomDevices) { device ->
                DevicePanelRow(
                    device = device,
                    onToggle = { viewModel.toggleDevice(device) },
                    onValueChange = { newValue -> viewModel.setDeviceValue(device, newValue) }
                )
            }
        }
    }
}

@Composable
fun ClassroomBentoBox(
    modifier: Modifier = Modifier,
    info: ClassroomInfo,
    isActiveClass: Boolean,
    devices: List<ClassroomDevice>,
    onSelect: () -> Unit
) {
    val isOn = devices.any { it.isOn }
    val activeCount = devices.count { it.isOn }

    val backgroundBrush = if (isActiveClass) {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
            )
        )
    }

    val borderStrokeColor = when {
        isActiveClass -> MaterialTheme.colorScheme.primary
        isOn -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val borderWidth = if (isActiveClass) 2.dp else 1.dp

    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundBrush)
            .border(borderWidth, borderStrokeColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect)
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glow status light indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isOn) MaterialTheme.colorScheme.secondary else Color.Gray.copy(
                                alpha = 0.5f
                            )
                        )
                )

                Text(
                    text = info.roomNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = info.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Right
                )
                Text(
                    text = if (isOn) "نشط: $activeCount أجهزة" else "خامل / موقر للطاقة",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOn) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Right
                )
            }
        }
    }
}

@Composable
fun DevicePanelRow(
    device: ClassroomDevice,
    onToggle: () -> Unit,
    onValueChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (device.isOn) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title & Switch row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = device.isOn,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = device.deviceName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right
                        )
                        Text(
                            text = when (device.deviceType) {
                                "light" -> "إضاءة ليد ذكية"
                                "ac" -> "مكيف حراري موفر"
                                "screen" -> "شاشة اندرويد مدرسية"
                                "projector" -> "بروجيكتور عرض"
                                else -> "تجهيزات بيئية"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            textAlign = TextAlign.Right
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (device.isOn) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (device.deviceType) {
                                "light" -> Icons.Default.Lightbulb
                                "ac" -> Icons.Default.AcUnit
                                "screen" -> Icons.Default.Tv
                                "projector" -> Icons.Default.CoPresent
                                else -> Icons.Default.DeviceHub
                            },
                            contentDescription = null,
                            tint = if (device.isOn) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }

            // Interactive Sliders Block (visible only if device is ON)
            if (device.isOn && (device.deviceType == "light" || device.deviceType == "ac")) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    val label = if (device.deviceType == "light") "السطوع: ${device.value.toInt()}%"
                                else "درجة التبريد المستهدفة: ${device.value.toInt()}°C"
                    
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Slider(
                        value = device.value,
                        onValueChange = { onValueChange(it) },
                        valueRange = if (device.deviceType == "light") 10f..100f else 16f..28f,
                        steps = if (device.deviceType == "light") 9 else 12,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

data class ClassroomInfo(
    val id: String,
    val name: String,
    val roomNumber: String
)
