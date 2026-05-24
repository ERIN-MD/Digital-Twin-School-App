package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SchoolViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SchoolRepository

    // Global lists
    val students: StateFlow<List<Student>>
    val teachers: StateFlow<List<Teacher>>
    val timetable: StateFlow<List<TimetableItem>>
    val devices: StateFlow<List<ClassroomDevice>>

    // Selected Classroom for Digital Twin details
    private val _selectedClassroom = MutableStateFlow("class_1")
    val selectedClassroom: StateFlow<String> = _selectedClassroom.asStateFlow()

    // Filter day for schedule tab
    private val _scheduleTabDay = MutableStateFlow("الأحد")
    val scheduleTabDay: StateFlow<String> = _scheduleTabDay.asStateFlow()

    // Firebase States
    private val _chatMessages = MutableStateFlow<List<FirebaseMessage>>(emptyList())
    val chatMessages: StateFlow<List<FirebaseMessage>> = _chatMessages.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncStatus = MutableStateFlow("مستقر")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _firebaseUrl = MutableStateFlow(FirebaseSyncManager.firebaseDbUrl)
    val firebaseUrl: StateFlow<String> = _firebaseUrl.asStateFlow()

    init {
        val database = SchoolDatabase.getDatabase(application)
        repository = SchoolRepository(database)

        // Flows from Room
        students = repository.allStudents
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        teachers = repository.allTeachers
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        timetable = repository.allTimetableItems
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        devices = repository.allDevices
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Seed data on launch
        viewModelScope.launch {
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    repository.seedDatabaseIfEmpty()
                }
            } catch (e: Exception) {
                android.util.Log.e("SchoolViewModel", "Failed to seed default school database: ${e.message}", e)
            }
            // Pull initial chat history
            loadChatMessages()
        }
    }

    // Dynamic Digital Twin School calculations!
    val totalPowerKW: StateFlow<Double> = devices.map { deviceList ->
        var power = 0.5 // base standby power of school (server rooms and corridor lights)
        deviceList.forEach { dev ->
            if (dev.isOn) {
                power += when (dev.deviceType) {
                    "light" -> 0.15 * (dev.value / 100f) // Brightness factored 150W max per light
                    "ac" -> {
                        // AC power is higher if target temperature is colder
                        val diff = (28f - dev.value).coerceAtLeast(0f)
                        1.2 + (diff * 0.08) // 1.2kW - 1.8kW depending on difference
                    }
                    "screen" -> 0.3
                    "projector" -> 0.4
                    else -> 0.1
                }
            }
        }
        Math.round(power * 10.0) / 10.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5)

    // Dynamic classroom telemetry summaries for digital twin dashboard status cards
    val averageTemperature: StateFlow<Double> = devices.map { deviceList ->
        val acs = deviceList.filter { it.deviceType == "ac" }
        if (acs.isEmpty()) 22.0
        else {
            val sumTemp = acs.map { if (it.isOn) it.value else 24.0f }.sum()
            Math.round((sumTemp / acs.size) * 10.0) / 10.0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 22.0)

    val activeDevicesCount: StateFlow<Int> = devices.map { deviceList ->
        deviceList.count { it.isOn }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentTimetable: StateFlow<List<TimetableItem>> = combine(timetable, scheduleTabDay) { list, day ->
        list.filter { it.dayOfWeek == day }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Classroom operations
    fun selectClassroom(classroomId: String) {
        _selectedClassroom.value = classroomId
    }

    fun setScheduleTabDay(day: String) {
        _scheduleTabDay.value = day
    }

    // Toggle classroom device
    fun toggleDevice(device: ClassroomDevice) {
        viewModelScope.launch {
            repository.updateDevice(device.copy(isOn = !device.isOn))
        }
    }

    // Edit classroom device value (e.g. brightness or temperature)
    fun setDeviceValue(device: ClassroomDevice, newValue: Float) {
        viewModelScope.launch {
            repository.updateDevice(device.copy(value = newValue))
        }
    }

    // Firebase Sync operations
    fun updateFirebaseUrl(url: String) {
        FirebaseSyncManager.firebaseDbUrl = url
        _firebaseUrl.value = FirebaseSyncManager.firebaseDbUrl
    }

    fun uploadLocalDataToFirebase() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatus.value = "جاري الحفظ والرفع لـ Firebase..."
            val successStudents = FirebaseSyncManager.syncStudentsToFirebase(students.value)
            val successTeachers = FirebaseSyncManager.syncTeachersToFirebase(teachers.value)
            
            _isSyncing.value = false
            _syncStatus.value = if (successStudents && successTeachers) {
                "تم حفظ البيانات سحابياً بنجاح!"
            } else {
                "فشل المزامنة. يرجى مراجعة الإعدادات والإنترنت"
            }
        }
    }

    fun downloadDataFromFirebase() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatus.value = "جاري ترحيل البيانات من السحابة..."
            val fbStudents = FirebaseSyncManager.fetchStudentsFromFirebase()
            val fbTeachers = FirebaseSyncManager.fetchTeachersFromFirebase()

            if (fbStudents.isNotEmpty() || fbTeachers.isNotEmpty()) {
                fbStudents.forEach { repository.insertStudent(it) }
                fbTeachers.forEach { repository.insertTeacher(it) }
                _syncStatus.value = "تمت المزامنة وتنزيل البيانات بنجاح!"
            } else {
                _syncStatus.value = "لم يتم العثور على قاعدة بيانات صالحة بـ Firebase"
            }
            _isSyncing.value = false
        }
    }

    // Chat management
    fun loadChatMessages() {
        viewModelScope.launch {
            val messages = FirebaseSyncManager.fetchChatFromFirebase()
            _chatMessages.value = messages
        }
    }

    fun sendChatMessage(sender: String, text: String, role: String) {
        viewModelScope.launch {
            val success = FirebaseSyncManager.sendMessageToFirebase(sender, text, role)
            if (success) {
                loadChatMessages()
            }
        }
    }

    // Students actions with automatic Firebase Sync
    fun addStudent(name: String, grade: String, attendance: String, parentContact: String, notes: String) {
        viewModelScope.launch {
            val newStudent = Student(
                name = name,
                grade = grade,
                attendanceStatus = attendance,
                parentContact = parentContact,
                notes = notes
            )
            repository.insertStudent(newStudent)
            // Auto sync to Firebase
            FirebaseSyncManager.syncStudentsToFirebase(students.value + newStudent)
        }
    }

    fun updateStudentAttendance(student: Student, newStatus: String) {
        viewModelScope.launch {
            val updatedStudent = student.copy(attendanceStatus = newStatus)
            repository.updateStudent(updatedStudent)
            // Auto sync to Firebase
            val updatedList = students.value.map { if (it.id == student.id) updatedStudent else it }
            FirebaseSyncManager.syncStudentsToFirebase(updatedList)
        }
    }

    fun removeStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            // Auto sync to Firebase
            val updatedList = students.value.filter { it.id != student.id }
            FirebaseSyncManager.syncStudentsToFirebase(updatedList)
        }
    }

    // Teachers actions with automatic Firebase Sync
    fun addTeacher(name: String, subject: String, grade: String, email: String) {
        viewModelScope.launch {
            val newTeacher = Teacher(
                name = name,
                subject = subject,
                assignedGrade = grade,
                email = email
            )
            repository.insertTeacher(newTeacher)
            // Auto sync to Firebase
            FirebaseSyncManager.syncTeachersToFirebase(teachers.value + newTeacher)
        }
    }

    fun removeTeacher(teacher: Teacher) {
        viewModelScope.launch {
            repository.deleteTeacher(teacher)
            // Auto sync to Firebase
            val updatedList = teachers.value.filter { it.id != teacher.id }
            FirebaseSyncManager.syncTeachersToFirebase(updatedList)
        }
    }

    // Timetable actions
    fun addTimetableItem(day: String, subject: String, teacher: String, timeSlot: String, room: String) {
        viewModelScope.launch {
            repository.insertTimetableItem(
                TimetableItem(
                    dayOfWeek = day,
                    subject = subject,
                    teacherName = teacher,
                    timeSlot = timeSlot,
                    roomNumber = room
                )
            )
        }
    }

    fun removeTimetableItem(item: TimetableItem) {
        viewModelScope.launch {
            repository.deleteTimetableItem(item)
        }
    }
}
