package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object FirebaseSyncManager {
    private const val TAG = "FirebaseSyncManager"
    
    // Default shared Firebase DB URL for educational simulation
    var firebaseDbUrl = "https://digital-twin-school-default-rtdb.firebaseio.com/"
        set(value) {
            field = if (value.endsWith("/")) value else "$value/"
        }

    // Network status tracker
    var isConnected = false
        private set

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    // Sync all students to Firebase Realtime Database
    suspend fun syncStudentsToFirebase(students: List<Student>): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "${firebaseDbUrl}students.json"
            val jsonArray = JSONArray()
            students.forEach { s ->
                val json = JSONObject().apply {
                    put("id", s.id)
                    put("name", s.name)
                    put("grade", s.grade)
                    put("attendanceStatus", s.attendanceStatus)
                    put("parentContact", s.parentContact)
                    put("notes", s.notes)
                }
                jsonArray.put(json)
            }

            val body = jsonArray.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(url)
                .put(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    isConnected = true
                    Log.d(TAG, "Successfully synced students to Firebase")
                    true
                } else {
                    Log.e(TAG, "Failed to sync students: Code ${response.code}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing students to firebase: ${e.message}")
            false
        }
    }

    // Sync all teachers to Firebase Realtime Database
    suspend fun syncTeachersToFirebase(teachers: List<Teacher>): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "${firebaseDbUrl}teachers.json"
            val jsonArray = JSONArray()
            teachers.forEach { t ->
                val json = JSONObject().apply {
                    put("id", t.id)
                    put("name", t.name)
                    put("subject", t.subject)
                    put("assignedGrade", t.assignedGrade)
                    put("email", t.email)
                }
                jsonArray.put(json)
            }

            val body = jsonArray.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(url)
                .put(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    isConnected = true
                    Log.d(TAG, "Successfully synced teachers to Firebase")
                    true
                } else {
                    Log.e(TAG, "Failed to sync teachers: Code ${response.code}")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing teachers: ${e.message}")
            false
        }
    }

    // Fetch students from Firebase Realtime Database
    suspend fun fetchStudentsFromFirebase(): List<Student> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Student>()
        try {
            val url = "${firebaseDbUrl}students.json"
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    isConnected = true
                    val responseStr = response.body?.string() ?: "[]"
                    if (responseStr != "null" && responseStr.trim().isNotEmpty()) {
                        val jsonArray = JSONArray(responseStr)
                        for (i in 0 until jsonArray.length()) {
                            if (jsonArray.isNull(i)) continue
                            val obj = jsonArray.getJSONObject(i)
                            list.add(
                                Student(
                                    id = obj.optInt("id", 0),
                                    name = obj.optString("name", ""),
                                    grade = obj.optString("grade", "الصف الأول"),
                                    attendanceStatus = obj.optString("attendanceStatus", "حاضر"),
                                    parentContact = obj.optString("parentContact", ""),
                                    notes = obj.optString("notes", "")
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching students: ${e.message}")
        }
        list
    }

    // Fetch teachers from Firebase Realtime Database
    suspend fun fetchTeachersFromFirebase(): List<Teacher> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Teacher>()
        try {
            val url = "${firebaseDbUrl}teachers.json"
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    isConnected = true
                    val responseStr = response.body?.string() ?: "[]"
                    if (responseStr != "null" && responseStr.trim().isNotEmpty()) {
                        val jsonArray = JSONArray(responseStr)
                        for (i in 0 until jsonArray.length()) {
                            if (jsonArray.isNull(i)) continue
                            val obj = jsonArray.getJSONObject(i)
                            list.add(
                                Teacher(
                                    id = obj.optInt("id", 0),
                                    name = obj.optString("name", ""),
                                    subject = obj.optString("subject", ""),
                                    assignedGrade = obj.optString("assignedGrade", ""),
                                    email = obj.optString("email", "")
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching teachers: ${e.message}")
        }
        list
    }

    // Send a live chat message to digital twin broadcast
    suspend fun sendMessageToFirebase(sender: String, messageText: String, role: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "${firebaseDbUrl}chat.json"
            val json = JSONObject().apply {
                put("sender", sender)
                put("text", messageText)
                put("role", role)
                put("timestamp", System.currentTimeMillis())
            }
            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    isConnected = true
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    // Fetch chat history
    suspend fun fetchChatFromFirebase(): List<FirebaseMessage> = withContext(Dispatchers.IO) {
        val list = mutableListOf<FirebaseMessage>()
        try {
            val url = "${firebaseDbUrl}chat.json"
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseStr = response.body?.string() ?: "{}"
                    if (responseStr != "null" && responseStr.trim().isNotEmpty()) {
                        val rootJson = JSONObject(responseStr)
                        val keys = rootJson.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val obj = rootJson.getJSONObject(key)
                            list.add(
                                FirebaseMessage(
                                    sender = obj.optString("sender", "مجهول"),
                                    text = obj.optString("text", ""),
                                    role = obj.optString("role", "طالب"),
                                    timestamp = obj.optLong("timestamp", 0L)
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching chat: ${e.message}")
        }
        list.sortedBy { it.timestamp }
    }
}

data class FirebaseMessage(
    val sender: String,
    val text: String,
    val role: String,
    val timestamp: Long
)
