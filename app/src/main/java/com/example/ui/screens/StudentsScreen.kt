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
import com.example.data.Student
import com.example.ui.SchoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(viewModel: SchoolViewModel) {
    val studentsList by viewModel.students.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedGradeFilter by remember { mutableStateOf("الكل") }
    
    // Add student dialog trigger
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredStudents = studentsList.filter { student ->
        val matchesSearch = student.name.contains(searchQuery, ignoreCase = true)
        val matchesGrade = selectedGradeFilter == "الكل" || student.grade == selectedGradeFilter
        matchesSearch && matchesGrade
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("إضافة طالب") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("students_screen")
        ) {
            // Search and filters sheet
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "البحث وفلترة الطلاب",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("بحث باسم الطالب...", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    // Grade filter chips
                    Text(
                        text = "حسب الفصل الدراسي:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        listOf("الكل", "الصف الثالث", "الصف الثاني", "الصف الأول").forEach { grade ->
                            FilterChip(
                                selected = selectedGradeFilter == grade,
                                onClick = { selectedGradeFilter = grade },
                                label = { Text(grade) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }

            // Students List
            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PeopleOutline,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لا يوجد طلاب يطابقون خيارات البحث",
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        StudentRowCard(
                            student = student,
                            onToggleAttendance = { currentStatus ->
                                val nextStatus = when (currentStatus) {
                                    "حاضر" -> "غائب"
                                    "غائب" -> "متأخر"
                                    else -> "حاضر"
                                }
                                viewModel.updateStudentAttendance(student, nextStatus)
                            },
                            onDelete = { viewModel.removeStudent(student) }
                        )
                    }
                }
            }
        }
    }

    // Add Student Dialog
    if (showAddDialog) {
        AddStudentDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, grade, attendance, phone, notes ->
                viewModel.addStudent(name, grade, attendance, phone, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun StudentRowCard(
    student: Student,
    onToggleAttendance: (String) -> Unit,
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
            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "مسح الطالب",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }

            // Attendance Toggle Button
            val (badgeBg, badgeText, statusLabel) = when (student.attendanceStatus) {
                "حاضر" -> Triple(Color(0xFFE6F4EA), Color(0xFF137333), "حاضر")
                "غائب" -> Triple(Color(0xFFFCE8E6), Color(0xFFC5221F), "غائب")
                else -> Triple(Color(0xFFFEF7E0), Color(0xFFB06000), "متأخر")
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBg)
                    .clickable { onToggleAttendance(student.attendanceStatus) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = badgeText
                )
            }

            // Info details
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            ) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right
                )
                
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ولي الأمر: ${student.parentContact.ifEmpty { "غير مسجل" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Right
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = student.grade,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right
                    )
                }

                if (student.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = student.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Right,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, grade: String, attendance: String, phone: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("الصف الأول") }
    var attendance by remember { mutableStateOf("حاضر") }
    var phone by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var expandedGrade by remember { mutableStateOf(false) }
    var expandedAttendance by remember { mutableStateOf(false) }

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
                    text = "تسجيل طالب جديد بالمنصة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الطالب بالكامل", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Grade Selector Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = grade,
                        onValueChange = {},
                        label = { Text("الصف الدراسي", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedGrade = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = expandedGrade,
                        onDismissRequest = { expandedGrade = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("الصف الأول", "الصف الثاني", "الصف الثالث").forEach { classroomGrade ->
                            DropdownMenuItem(
                                text = { Text(classroomGrade, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                                onClick = {
                                    grade = classroomGrade
                                    expandedGrade = false
                                }
                            )
                        }
                    }
                }

                // Phone field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم هاتف ولي الأمر", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Notes field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية عن الحالة", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
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
                            if (name.trim().isNotEmpty()) {
                                onConfirm(name, grade, attendance, phone, notes)
                            }
                        },
                        enabled = name.trim().isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حفظ البيانات")
                    }
                }
            }
        }
    }
}
