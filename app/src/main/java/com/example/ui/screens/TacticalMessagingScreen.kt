package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.TacticalMessageEntity
import com.example.data.entity.UserAccountEntity
import com.example.ui.theme.TacticalBorderStrong
import com.example.ui.theme.TacticalBorderSubtle
import com.example.ui.theme.TacticalPitchBlack
import com.example.ui.theme.TacticalSurfaceDark
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TacticalSurfaceMedium
import com.example.ui.theme.TacticalWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TacticalMessagingScreen(
    currentUser: UserAccountEntity,
    messages: List<TacticalMessageEntity>,
    allAccounts: List<UserAccountEntity>,
    onSendMessage: (
        channel: String,
        content: String,
        receiverId: Long?,
        receiverName: String,
        classification: String,
        isUrgent: Boolean
    ) -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onClearAllMessages: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val channels = listOf(
        "OPS_ROOM" to "غرفة العمليات المشتركة",
        "UAV_RECON" to "الاستطلاع والمسيرات",
        "AIR_DEFENSE" to "الإنذار والدفاع الجوي",
        "ENCRYPTED_DIRECT" to "برقيات سرية خاصة"
    )

    var selectedChannelIndex by remember { mutableIntStateOf(0) }
    val currentChannel = channels[selectedChannelIndex].first

    val filteredMessages = remember(messages, currentChannel, currentUser.id, currentUser.isMasterAdmin) {
        messages.filter { message ->
            val sameChannel = message.channel == currentChannel
            val allowedRecipient = message.receiverId == null ||
                message.senderId == currentUser.id ||
                message.receiverId == currentUser.id ||
                currentUser.isMasterAdmin
            sameChannel && allowedRecipient
        }
    }

    var messageInput by remember { mutableStateOf("") }
    var selectedClassification by remember { mutableStateOf("سري للغاية") }
    var isUrgentMessage by remember { mutableStateOf(false) }

    var selectedRecipient by remember {
        mutableStateOf<Pair<Long?, String>>(null to "غرفة العمليات المشتركة (تعميم عام)")
    }
    var showRecipientDropdown by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val quickDispatches = listOf(
        "تأكيد رصد طيران مسير معادٍ في القطاع والتعامل معه استطلاعياً",
        "رصد نشاط تشويش إلكتروني معادٍ يؤثر على ملاحة ممر باب المندب",
        "رفع الجاهزية القتالية لمنظومات الرصد والاستطلاع للدرجة القصوى",
        "إشارة استخباراتية: خلو سماء القطاع من أي أهداف جوية معادية",
        "طلب تعزيز المراقبة الكهروبصرية والرادارية في المحور فوراً"
    )

    val listState = rememberLazyListState()

    LaunchedEffect(filteredMessages.size) {
        if (filteredMessages.isNotEmpty()) {
            listState.animateScrollToItem(filteredMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TacticalPitchBlack)
            .testTag("tactical_messaging_screen")
    ) {
        // 1. Header Bar
        Surface(
            color = TacticalSurfaceDark,
            border = BorderStroke(1.dp, TacticalBorderStrong),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = TacticalWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "منظومة التراسل والبرقيات العسكرية المشفرة",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TacticalWhite,
                                fontSize = 13.sp
                            )
                        )
                    }

                    Text(
                        text = "الإدارة العامة للاتصالات الاستخباراتية // شبكة العمليات المشتركة - الجنوب العربي",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    )
                }

                if (currentUser.isMasterAdmin || currentUser.role == "COMMANDER") {
                    IconButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_clear_all_comms")
                    ) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "مسح البرقيات",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. Channel Selector Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedChannelIndex,
            containerColor = TacticalPitchBlack,
            contentColor = TacticalWhite,
            edgePadding = 8.dp,
            modifier = Modifier.border(1.dp, TacticalBorderSubtle)
        ) {
            channels.forEachIndexed { index, pair ->
                Tab(
                    selected = selectedChannelIndex == index,
                    onClick = { selectedChannelIndex = index },
                    text = {
                        Text(
                            text = pair.second,
                            fontWeight = if (selectedChannelIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                            color = if (selectedChannelIndex == index) TacticalWhite else Color.Gray
                        )
                    },
                    modifier = Modifier.testTag("tab_channel_${pair.first}")
                )
            }
        }

        // 3. Quick Dispatches Bar (برقيات سريعة جاهزة)
        Surface(
            color = TacticalSurfaceElevated,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text(
                    text = "برقيات عملياتية سريعة:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold
                    )
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    quickDispatches.take(3).forEach { template ->
                        Surface(
                            color = TacticalSurfaceDark,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, TacticalBorderSubtle),
                            modifier = Modifier
                                .clickable {
                                    messageInput = template
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = template,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = TacticalWhite
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Messages Feed
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (filteredMessages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لا توجد برقيات مرسلة في هذه القناة حتى الآن",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.LightGray)
                    )
                    Text(
                        text = "القناة مشفرة بالكامل وجاهزة لإرسال واستقبال البرقيات العملياتية.",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 10.sp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("messages_list")
                ) {
                    items(filteredMessages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            isOwnMessage = msg.senderId == currentUser.id || msg.senderName.contains(currentUser.fullName),
                            canDelete = currentUser.isMasterAdmin || currentUser.role == "COMMANDER" || msg.senderId == currentUser.id,
                            onDelete = { onDeleteMessage(msg.id) }
                        )
                    }
                }
            }
        }

        // 5. Compose Box
        Surface(
            color = TacticalSurfaceDark,
            border = BorderStroke(1.dp, TacticalBorderStrong),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Controls Row: Recipient selector + Classification + Urgent toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Recipient Selector Dropdown
                    Box {
                        Surface(
                            color = TacticalSurfaceElevated,
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, TacticalBorderSubtle),
                            modifier = Modifier.clickable { showRecipientDropdown = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = TacticalWhite,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "إلى: ${selectedRecipient.second.take(18)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = TacticalWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showRecipientDropdown,
                            onDismissRequest = { showRecipientDropdown = false },
                            modifier = Modifier.background(TacticalSurfaceDark)
                        ) {
                            DropdownMenuItem(
                                text = { Text("غرفة العمليات المشتركة (تعميم عام)", color = TacticalWhite, fontSize = 11.sp) },
                                onClick = {
                                    selectedRecipient = null to "غرفة العمليات المشتركة (تعميم عام)"
                                    showRecipientDropdown = false
                                }
                            )

                            allAccounts.filter { it.id != currentUser.id }.forEach { account ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${account.militaryRank}: ${account.fullName} (${account.assignedSector})",
                                            color = TacticalWhite,
                                            fontSize = 11.sp
                                        )
                                    },
                                    onClick = {
                                        selectedRecipient = account.id to "${account.militaryRank} ${account.fullName}"
                                        showRecipientDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Classification selector
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("سري للغاية", "عملياتي عاجل", "إشارة استخباراتية").forEach { cls ->
                            Surface(
                                color = if (selectedClassification == cls) TacticalWhite else TacticalSurfaceElevated,
                                shape = RoundedCornerShape(3.dp),
                                border = BorderStroke(1.dp, TacticalBorderSubtle),
                                modifier = Modifier.clickable { selectedClassification = cls }
                            ) {
                                Text(
                                    text = cls,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedClassification == cls) TacticalPitchBlack else TacticalWhite
                                    ),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Input Field + Send Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = {
                            Text(
                                "اكتب نص البرقية العسكرية المشفرة...",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("message_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TacticalWhite,
                            unfocusedTextColor = TacticalWhite,
                            focusedBorderColor = TacticalWhite,
                            unfocusedBorderColor = TacticalBorderSubtle,
                            cursorColor = TacticalWhite,
                            focusedContainerColor = TacticalPitchBlack,
                            unfocusedContainerColor = TacticalPitchBlack
                        ),
                        singleLine = false,
                        maxLines = 3,
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                    )

                    IconButton(
                        onClick = {
                            if (messageInput.isNotBlank()) {
                                onSendMessage(
                                    currentChannel,
                                    messageInput.trim(),
                                    selectedRecipient.first,
                                    selectedRecipient.second,
                                    selectedClassification,
                                    isUrgentMessage
                                )
                                messageInput = ""
                            }
                        },
                        enabled = messageInput.isNotBlank(),
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = if (messageInput.isNotBlank()) TacticalWhite else TacticalSurfaceElevated,
                                shape = CircleShape
                            )
                            .testTag("send_message_button")
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "إرسال البرقية",
                            tint = if (messageInput.isNotBlank()) TacticalPitchBlack else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = TacticalSurfaceDark,
            titleContentColor = TacticalWhite,
            textContentColor = TacticalWhite,
            title = {
                Text(
                    "مسح أرشيف البرقيات",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                Text(
                    "هل أنت متأكد من مسح وإفراغ كافة البرقيات والمراسلات العسكرية؟ لا يمكن التراجع عن هذا الإجراء.",
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllMessages()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = TacticalWhite)
                ) {
                    Text("تأكيد المسح", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearConfirmDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.LightGray)
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun MessageBubble(
    message: TacticalMessageEntity,
    isOwnMessage: Boolean,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
    val timeStr = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("message_item_${message.id}"),
        horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isOwnMessage) TacticalSurfaceElevated else TacticalSurfaceDark,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(
                width = 1.dp,
                color = if (isOwnMessage) TacticalWhite else TacticalBorderStrong
            ),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Cipher & Classification Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = TacticalPitchBlack,
                            shape = RoundedCornerShape(3.dp),
                            border = BorderStroke(1.dp, TacticalBorderSubtle)
                        ) {
                            Text(
                                text = "[${message.cipherCode}]",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TacticalWhite
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        Surface(
                            color = if (message.isUrgent) TacticalWhite else TacticalPitchBlack,
                            shape = RoundedCornerShape(3.dp),
                            border = BorderStroke(1.dp, TacticalBorderStrong)
                        ) {
                            Text(
                                text = message.classification,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (message.isUrgent) TacticalPitchBlack else TacticalWhite
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$timeStr Z",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = Color.LightGray
                            )
                        )

                        if (canDelete) {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "حذف البرقية",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Sender & Recipient Information
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "من: ${message.senderRank} ${message.senderName}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TacticalWhite,
                            fontSize = 11.sp
                        )
                    )

                    Text(
                        text = "إلى: ${message.receiverName}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    )
                }

                // Message Content
                Surface(
                    color = TacticalPitchBlack,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, TacticalBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TacticalWhite,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        ),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
