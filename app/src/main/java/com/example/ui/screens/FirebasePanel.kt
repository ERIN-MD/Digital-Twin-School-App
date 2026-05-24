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
import com.example.ui.SchoolViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebasePanelDialog(
    viewModel: SchoolViewModel,
    onDismiss: () -> Unit
) {
    var curTab by remember { mutableStateOf(0) } // 0: Live Chat, 1: Cloud Sync Settings
    val syncStatus by viewModel.syncStatus.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val firebaseUrl by viewModel.firebaseUrl.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "بوابة الخدمات السحابية والتواصل",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section Tabs (Chat / Synclink)
                TabRow(
                    selectedTabIndex = curTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = curTab == 0,
                        onClick = { curTab = 0 },
                        text = { Text("محادثات الطلاب والمعلمين", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = curTab == 1,
                        onClick = { curTab = 1 },
                        text = { Text("مزامنة السحابة (Firebase)", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content Panel depending on active tab
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (curTab == 0) {
                        FirebaseChatScreen(viewModel = viewModel)
                    } else {
                        FirebaseSettingsScreen(
                            viewModel = viewModel,
                            syncStatus = syncStatus,
                            isSyncing = isSyncing,
                            firebaseUrl = firebaseUrl
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FirebaseChatScreen(viewModel: SchoolViewModel) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    var textMsg by remember { mutableStateOf("") }
    
    // Virtual mock profiles for typing role
    val roles = listOf("مدير المنصة", "أ/ منى عبد العزيز", "أ/ عبد الرحمن صلاح", "الطالب: أحمد مصطفى")
    var selectedRoleIndex by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat History List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp),
            reverseLayout = false,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد رسائل نشطة حالياً. ابدأ المحادثة بالأسفل وسيبث النظام رسائلك لـ Firebase!",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(chatMessages) { msg ->
                    ChatBubble(msg = msg)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Role select indicator label
        Text(
            text = "إرسال كـ: ${roles[selectedRoleIndex]} [انقر للتغيير]",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    selectedRoleIndex = (selectedRoleIndex + 1) % roles.size
                }
                .padding(vertical = 4.dp),
            textAlign = TextAlign.Right
        )

        // Text field & send button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (textMsg.trim().isNotEmpty()) {
                        val activeRole = roles[selectedRoleIndex]
                        viewModel.sendChatMessage(
                            sender = activeRole,
                            text = textMsg.trim(),
                            role = if (activeRole.contains("أ/")) "معلم" else if (activeRole.contains("الطالب")) "طالب" else "مشرف"
                        )
                        textMsg = ""
                    }
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Send, contentDescription = "إرسال", tint = Color.White)
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = textMsg,
                onValueChange = { textMsg = it },
                placeholder = { Text("اكتب رسالة لبثها بالبوابة...", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }
}

@Composable
fun ChatBubble(msg: com.example.data.FirebaseMessage) {
    val isTeacher = msg.role == "معلم"
    val isManager = msg.role == "مشرف"
    
    val bubbleBg = when {
        isManager -> MaterialTheme.colorScheme.primaryContainer
        isTeacher -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val bubbleTextColor = when {
        isManager -> MaterialTheme.colorScheme.onPrimaryContainer
        isTeacher -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Card(
            shape = RoundedCornerShape(18.dp, 18.dp, 0.dp, 18.dp),
            colors = CardDefaults.cardColors(containerColor = bubbleBg)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "(${msg.role})",
                        fontSize = 10.sp,
                        color = bubbleTextColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = msg.sender,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = bubbleTextColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = msg.text,
                    fontSize = 13.sp,
                    color = bubbleTextColor,
                    textAlign = TextAlign.Right
                )
            }
        }
        
        val dateStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(msg.timestamp))
        Text(
            text = dateStr,
            fontSize = 9.sp,
            color = Color.Gray,
            modifier = Modifier.padding(end = 4.dp, top = 2.dp)
        )
    }
}

@Composable
fun FirebaseSettingsScreen(
    viewModel: SchoolViewModel,
    syncStatus: String,
    isSyncing: Boolean,
    firebaseUrl: String
) {
    var urlInput by remember { mutableStateOf(firebaseUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "تهيئة رابط خادم Firebase",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        OutlinedTextField(
            value = urlInput,
            onValueChange = {
                urlInput = it
                viewModel.updateFirebaseUrl(it)
            },
            label = { Text("قاعدة بيانات Firebase URL", textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth()) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "استخدم رابط Firebase مخصص لتخزين بيانات الطلاب المضافة بالكامل وحفظ المنظومة.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Right
            )
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant)

        // Real-time Action buttons for Firebase DB Sync
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "عمليات مزامنة قاعدة البيانات",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.downloadDataFromFirebase() },
                        modifier = Modifier.weight(1f),
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSyncing && syncStatus.contains("سحب")) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("تنزيل الكلي")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.uploadLocalDataToFirebase() },
                        modifier = Modifier.weight(1f),
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSyncing && syncStatus.contains("رفع")) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("رفع الكلي")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Sync status indicator text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (syncStatus.contains("نجاح")) Color(0xFF10B981)
                                else if (syncStatus.contains("فشل")) Color(0xFFEF4444)
                                else MaterialTheme.colorScheme.primary
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = syncStatus, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Text(text = "حالة الاتصال بالخادم السحابي:", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}
