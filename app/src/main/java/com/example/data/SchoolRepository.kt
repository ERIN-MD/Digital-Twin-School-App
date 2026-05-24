package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SchoolRepository(private val database: SchoolDatabase) {

    val allStudents: Flow<List<Student>> = database.studentDao().getAllStudents()
    val allTeachers: Flow<List<Teacher>> = database.teacherDao().getAllTeachers()
    val allTimetableItems: Flow<List<TimetableItem>> = database.timetableDao().getAllTimetableItems()
    val allDevices: Flow<List<ClassroomDevice>> = database.classroomDeviceDao().getAllDevices()

    val studentCount: Flow<Int> = database.studentDao().getStudentCount()
    val teacherCount: Flow<Int> = database.teacherDao().getTeacherCount()

    // Student Operations
    suspend fun insertStudent(student: Student) {
        database.studentDao().insertStudent(student)
    }

    suspend fun updateStudent(student: Student) {
        database.studentDao().updateStudent(student)
    }

    suspend fun deleteStudent(student: Student) {
        database.studentDao().deleteStudent(student)
    }

    // Teacher Operations
    suspend fun insertTeacher(teacher: Teacher) {
        database.teacherDao().insertTeacher(teacher)
    }

    suspend fun deleteTeacher(teacher: Teacher) {
        database.teacherDao().deleteTeacher(teacher)
    }

    // Timetable Operations
    suspend fun insertTimetableItem(item: TimetableItem) {
        database.timetableDao().insertTimetableItem(item)
    }

    suspend fun deleteTimetableItem(item: TimetableItem) {
        database.timetableDao().deleteTimetableItem(item)
    }

    fun getTimetableForDay(day: String): Flow<List<TimetableItem>> {
        return database.timetableDao().getTimetableItemsForDay(day)
    }

    // Device Operations
    suspend fun updateDevice(device: ClassroomDevice) {
        database.classroomDeviceDao().updateDevice(device)
    }

    suspend fun seedDatabaseIfEmpty() {
        // Query current students, teachers, etc.
        val students = database.studentDao().getAllStudents().first()
        if (students.isEmpty()) {
            // Seed Students
            listOf(
                Student(name = "محمد أحمد علي", grade = "الصف الأول", attendanceStatus = "حاضر", parentContact = "01123456789", notes = "طالب متميز ومشارك فعال"),
                Student(name = "سارة محمود حسن", grade = "الصف الأول", attendanceStatus = "حاضر", parentContact = "01098765432", notes = "متفوقة في مادة الرياضيات"),
                Student(name = "يوسف عمر فاروق", grade = "الصف الأول", attendanceStatus = "غائب", parentContact = "01234567890", notes = "حالة غياب مبررة بعذر طبي"),
                Student(name = "رنا خالد عبد الله", grade = "الصف الثاني", attendanceStatus = "حاضر", parentContact = "01512345678", notes = "موهوبة في الرسم والفنون وبطل المدرسة"),
                Student(name = "مازن مصطفى سعيد", grade = "الصف الثاني", attendanceStatus = "متأخر", parentContact = "01145678901", notes = "وصل بعد الحصة الأولى بقليل"),
                Student(name = "فاطمة محمد إبراهيم", grade = "الصف الثالث", attendanceStatus = "حاضر", parentContact = "01011223344", notes = "عضو فعال في الإذاعة المدرسية"),
                Student(name = "خالد وليد سيد", grade = "الصف الثالث", attendanceStatus = "حاضر", parentContact = "01255667788", notes = "رئيس اتحاد طلاب الفصل")
            ).forEach { database.studentDao().insertStudent(it) }
        }

        val teachers = database.teacherDao().getAllTeachers().first()
        if (teachers.isEmpty()) {
            // Seed Teachers
            listOf(
                Teacher(name = "أ/ عبد الرحمن صلاح", subject = "العلوم والفيزياء", assignedGrade = "مختبر العلوم", email = "abdo.salah@school.edu.eg"),
                Teacher(name = "أ/ منى عبد العزيز", subject = "الرياضيات", assignedGrade = "الصف الأول والثاني", email = "mona.aziz@school.edu.eg"),
                Teacher(name = "أ/ أحمد عبد الله", subject = "اللغة العربية", assignedGrade = "الصف الثالث", email = "ahmed.abdallah@school.edu.eg"),
                Teacher(name = "أ/ مروة يحيى", subject = "اللغة الإنجليزية", assignedGrade = "جميع المراحل", email = "marwa.yahya@school.edu.eg")
            ).forEach { database.teacherDao().insertTeacher(it) }
        }

        val timetable = database.timetableDao().getAllTimetableItems().first()
        if (timetable.isEmpty()) {
            // Seed Timetable items
            listOf(
                TimetableItem(dayOfWeek = "الأحد", subject = "الرياضيات", teacherName = "أ/ منى عبد العزيز", timeSlot = "08:00 ص - 09:00 ص", roomNumber = "قاعة 101"),
                TimetableItem(dayOfWeek = "الأحد", subject = "اللغة العربية", teacherName = "أ/ أحمد عبد الله", timeSlot = "09:15 ص - 10:15 ص", roomNumber = "قاعة 101"),
                TimetableItem(dayOfWeek = "الأحد", subject = "العلوم", teacherName = "أ/ عبد الرحمن صلاح", timeSlot = "10:30 ص - 11:30 ص", roomNumber = "مختبر العلوم"),
                
                TimetableItem(dayOfWeek = "الإثنين", subject = "اللغة الإنجليزية", teacherName = "أ/ مروة يحيى", timeSlot = "08:00 ص - 09:00 ص", roomNumber = "قاعة 101"),
                TimetableItem(dayOfWeek = "الإثنين", subject = "الرياضيات", teacherName = "أ/ منى عبد العزيز", timeSlot = "09:15 ص - 10:15 ص", roomNumber = "قاعة 102"),
                TimetableItem(dayOfWeek = "الإثنين", subject = "الحاسب الآلي", teacherName = "أ/ مروة يحيى", timeSlot = "10:30 ص - 11:30 ص", roomNumber = "معمل الحاسبات"),
                
                TimetableItem(dayOfWeek = "الثلاثاء", subject = "اللغة العربية", teacherName = "أ/ أحمد عبد الله", timeSlot = "08:00 ص - 09:00 ص", roomNumber = "قاعة 102"),
                TimetableItem(dayOfWeek = "الثلاثاء", subject = "العلوم والأحياء", teacherName = "أ/ عبد الرحمن صلاح", timeSlot = "09:15 ص - 10:15 ص", roomNumber = "مختبر العلوم"),
                
                TimetableItem(dayOfWeek = "الأربعاء", subject = "الرياضيات الهندسية", teacherName = "أ/ منى عبد العزيز", timeSlot = "08:00 ص - 09:00 ص", roomNumber = "قاعة 101"),
                TimetableItem(dayOfWeek = "الأربعاء", subject = "اللغة الإنجليزية", teacherName = "أ/ مروة يحيى", timeSlot = "09:15 ص - 10:15 ص", roomNumber = "قاعة 101"),
                
                TimetableItem(dayOfWeek = "الخميس", subject = "التربية الرياضية", teacherName = "مدرس رياضي", timeSlot = "08:00 ص - 09:00 ص", roomNumber = "فناء المدرسة"),
                TimetableItem(dayOfWeek = "الخميس", subject = "العلوم العامة", teacherName = "أ/ عبد الرحمن صلاح", timeSlot = "10:30 ص - 11:30 ص", roomNumber = "قاعة 102")
            ).forEach { database.timetableDao().insertTimetableItem(it) }
        }

        val devices = database.classroomDeviceDao().getAllDevices().first()
        if (devices.isEmpty()) {
            // Seed Classroom Devices (Lights, AC, Screens, Projectors)
            listOf(
                // Class 1 (قاعة 101 - الصف الأول)
                ClassroomDevice("c1_light", "class_1", "إضاءة الصف الأول", "light", true, 80.0f),
                ClassroomDevice("c1_ac", "class_1", "تكييف الصف الأول", "ac", true, 22.0f),
                ClassroomDevice("c1_screen", "class_1", "الشاشة التفاعلية الذكية", "screen", true, 1.0f),

                // Class 2 (قاعة 102 - الصف الثاني)
                ClassroomDevice("c2_light", "class_2", "إضاءة الصف الثاني", "light", true, 50.0f),
                ClassroomDevice("c2_ac", "class_2", "تكييف الصف الثاني", "ac", false, 24.0f),
                ClassroomDevice("c2_screen", "class_2", "شاشة عرض الفيديوهات", "screen", false, 0.0f),

                // Lab (مختبر العلوم)
                ClassroomDevice("lab_light", "lab", "إضاءة المختبر الرئيسي", "light", true, 90.0f),
                ClassroomDevice("lab_ac", "lab", "تكييف المختبر المركزي", "ac", true, 20.0f),
                ClassroomDevice("lab_projector", "lab", "جهاز البروجيكتور", "projector", true, 1.0f),

                // Library (المكتبة)
                ClassroomDevice("lib_light", "library", "إضاءة ركن القراءة", "light", true, 40.0f),
                ClassroomDevice("lib_ac", "library", "تكييف القراءة الهادئة", "ac", true, 21.0f)
            ).forEach { database.classroomDeviceDao().insertDevice(it) }
        }
    }
}
