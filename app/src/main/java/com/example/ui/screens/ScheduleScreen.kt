package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.TimetableItem
import com.example.ui.SchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: SchoolViewModel) {
    val currentDay by viewModel.scheduleTabDay.collectAsState()
    val scheduleItems by viewModel.currentTimetable.collectAsState()

    // Days list for the school week in Arabic
    val schoolDays = listOf("الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس")
    
    // Add schedule item dialog trigger
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                text = { Text("إضافة حصة") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("schedule_screen")
        ) {
            // Days tabs bar
            ScrollableTabRow(
                selectedTabIndex = schoolDays.indexOf(currentDay).coerceAtLeast(0),
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { Divider(color = MaterialTheme.colorScheme.outlineVariant) }
            ) {
                schoolDays.forEach { day ->
                    Tab(
                        selected = currentDay == day,
                        onClick = { viewModel.setScheduleTabDay(day) },
                        text = {
                            Text(
                                text = day,
                                fontWeight = if (currentDay == day) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timetable list
            if (scheduleItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لا توجد حصص مجدولة ليوم $currentDay",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(scheduleItems, key = { it.id }) { item ->
                        ScheduleItemCard(
                            item = item,
                            onDelete = { viewModel.removeTimetableItem(item) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddScheduleItemDialog(
            defaultDay = currentDay,
            onDismiss = { showAddDialog = false },
            onConfirm = { day, subject, teacher, time, room ->
                viewModel.addTimetableItem(day, subject, teacher, time, room)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ScheduleItemCard(
    item: TimetableItem,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Delete Action
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "حذف الحصة",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }

            // Time capsule badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.timeSlot,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Info Details
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            ) {
                Text(
                    text = item.subject,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.teacherName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.roomNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Right
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Room,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleItemDialog(
    defaultDay: String,
    onDismiss: () -> Unit,
    onConfirm: (day: String, subject: String, teacher: String, time: String, room: String) -> Unit
) {
    var day by remember { mutableStateOf(defaultDay) }
    var subject by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var timeSlot by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("") }

    var expandedDay by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "جدولة حصة دراسية جديدة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Day Selector dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = day,
                        onValueChange = {},
                        label = { Text("اليوم", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedDay = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = expandedDay,
                        onDismissRequest = { expandedDay = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس").forEach { schoolDay ->
                            DropdownMenuItem(
                                text = { Text(schoolDay, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                                onClick = {
                                    day = schoolDay
                                    expandedDay = false
                                }
                            )
                        }
                    }
                }

                // Subject name
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("اسم المادة الدراسية (مثل: اللغة العربية)", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Teacher name
                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text("اسم المعلم المسؤول", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Time slot e.g. "08:00 ص - 09:00 ص"
                OutlinedTextField(
                    value = timeSlot,
                    onValueChange = { timeSlot = it },
                    label = { Text("الموعد المجدد (مثل: 08:00 ص - 09:00 ص)", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Room Number
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("الحجرة / القاعة (مثل: قاعة 101)", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = {
                            if (subject.trim().isNotEmpty() && teacher.trim().isNotEmpty() && timeSlot.trim().isNotEmpty()) {
                                onConfirm(day, subject, teacher, timeSlot, roomNumber.ifEmpty { "قاعة غير محددة" })
                            }
                        },
                        enabled = subject.trim().isNotEmpty() && teacher.trim().isNotEmpty() && timeSlot.trim().isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("جدولة الحصة")
                    }
                }
            }
        }
    }
}
