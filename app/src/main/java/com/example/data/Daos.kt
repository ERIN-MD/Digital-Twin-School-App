package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY id DESC")
    fun getAllStudents(): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>
}

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers ORDER BY name ASC")
    fun getAllTeachers(): Flow<List<Teacher>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher)

    @Delete
    suspend fun deleteTeacher(teacher: Teacher)

    @Query("SELECT COUNT(*) FROM teachers")
    fun getTeacherCount(): Flow<Int>
}

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable ORDER BY id ASC")
    fun getAllTimetableItems(): Flow<List<TimetableItem>>

    @Query("SELECT * FROM timetable WHERE dayOfWeek = :day ORDER BY timeSlot ASC")
    fun getTimetableItemsForDay(day: String): Flow<List<TimetableItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableItem(item: TimetableItem)

    @Delete
    suspend fun deleteTimetableItem(item: TimetableItem)
}

@Dao
interface ClassroomDeviceDao {
    @Query("SELECT * FROM classroom_devices")
    fun getAllDevices(): Flow<List<ClassroomDevice>>

    @Query("SELECT * FROM classroom_devices WHERE classroomId = :classroomId")
    fun getDevicesForClassroom(classroomId: String): Flow<List<ClassroomDevice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: ClassroomDevice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDevices(devices: List<ClassroomDevice>)

    @Update
    suspend fun updateDevice(device: ClassroomDevice)
}
