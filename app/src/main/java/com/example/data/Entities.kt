package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val grade: String,
    val attendanceStatus: String,
    val parentContact: String = "",
    val notes: String = ""
)

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val subject: String,
    val assignedGrade: String,
    val email: String = ""
)

@Entity(tableName = "timetable")
data class TimetableItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayOfWeek: String, // "الأحد", "الإثنين", etc.
    val subject: String,
    val teacherName: String,
    val timeSlot: String, // e.g. "08:00 ص - 09:00 ص"
    val roomNumber: String
)

@Entity(tableName = "classroom_devices")
data class ClassroomDevice(
    @PrimaryKey val id: String,
    val classroomId: String, // "class_1", "class_2", "lab", "library"
    val deviceName: String,
    val deviceType: String, // "light", "ac", "projector", "screen"
    val isOn: Boolean,
    val value: Float = 0.0f // Temperature, brightness, etc.
)
