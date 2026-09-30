package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.UserAccountEntity
import com.example.data.model.SouthGovernorate
import com.example.ui.theme.TacticalBorderStrong
import com.example.ui.theme.TacticalBorderSubtle
import com.example.ui.theme.TacticalPitchBlack
import com.example.ui.theme.TacticalSurfaceDark
import com.example.ui.theme.TacticalSurfaceElevated
import com.example.ui.theme.TacticalSurfaceMedium
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary
import com.example.ui.theme.TacticalWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManageAccountsDialog(
    currentUser: UserAccountEntity,
    accounts: List<UserAccountEntity>,
    onCreateAccount: (username: String, fullName: String, title: String, pass: String, rank: String, role: String, sector: String) -> Unit,
    onUpdateAccount: (id: Long, username: String, fullName: String, title: String, pass: String, rank: String, role: String, sector: String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onDeleteAccount: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateForm by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<UserAccountEntity?>(null) }

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRank by remember { mutableStateOf("قائد وحدة استطلاع") }
    var selectedRole by remember { mutableStateOf("قائد وحدة") }
    var title by remember { mutableStateOf("قائد قطاع استطلاع ورصد") }
    var assignedSector by remember { mutableStateOf("العاصمة عدن") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val rankOptions = listOf(
        "قائد وحدة استطلاع",
        "عميد ركن",
        "عقيد ركن",
        "مقدم",
        "رائد",
        "نقيب",
        "ملازم أول",
        "مساعد",
        "جندي رصد"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = TacticalSurfaceDark,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderStrong),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp)
                .testTag("manage_accounts_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = TacticalWhite,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "إدارة الحسابات العسكرية ومنح الصلاحيات",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TacticalWhite
                                )
                            )
                            Text(
                                text = "صلاحيات القائد: ${currentUser.fullName} (${currentUser.title})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TacticalTextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_accounts_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TacticalWhite)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = TacticalBorderSubtle)

                // Toggle Create Account Button
                Button(
                    onClick = { showCreateForm = !showCreateForm },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TacticalWhite,
                        contentColor = TacticalPitchBlack
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("toggle_create_account_form")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showCreateForm) "إخفاء نموذج الإضافة" else "إنشاء حساب عسكري جديد (لقائد أو جندي)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Create Account Form
                if (showCreateForm) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = TacticalSurfaceMedium,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderStrong),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "بيانات الحساب الجديد:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TacticalWhite
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("الاسم الكامل (مثال: ناصر عبد ربه اليافعي)", color = TacticalTextSecondary, fontSize = 11.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TacticalWhite,
                                    unfocusedTextColor = TacticalWhite,
                                    focusedBorderColor = TacticalWhite,
                                    unfocusedBorderColor = TacticalBorderSubtle,
                                    focusedContainerColor = TacticalSurfaceDark,
                                    unfocusedContainerColor = TacticalSurfaceDark
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("new_account_fullname")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = username,
                                    onValueChange = { username = it },
                                    label = { Text("اسم المستخدم (Username)", color = TacticalTextSecondary, fontSize = 11.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TacticalWhite,
                                        unfocusedTextColor = TacticalWhite,
                                        focusedBorderColor = TacticalWhite,
                                        unfocusedBorderColor = TacticalBorderSubtle,
                                        focusedContainerColor = TacticalSurfaceDark,
                                        unfocusedContainerColor = TacticalSurfaceDark
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("new_account_username")
                                )

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("كلمة المرور", color = TacticalTextSecondary, fontSize = 11.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TacticalWhite,
                                        unfocusedTextColor = TacticalWhite,
                                        focusedBorderColor = TacticalWhite,
                                        unfocusedBorderColor = TacticalBorderSubtle,
                                        focusedContainerColor = TacticalSurfaceDark,
                                        unfocusedContainerColor = TacticalSurfaceDark
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("new_account_password")
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Military Rank Chips
                            Text(
                                text = "الرتبة العسكرية:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TacticalWhite
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("عقيد ركن", "مقدم", "رائد", "نقيب", "ملازم أول", "مساعد", "جندي رصد").forEach { r ->
                                    val isSelected = selectedRank == r
                                    Box(
                                        modifier = Modifier
                                            .background(if (isSelected) TacticalWhite else TacticalSurfaceDark, RoundedCornerShape(4.dp))
                                            .border(1.dp, if (isSelected) TacticalWhite else TacticalBorderSubtle, RoundedCornerShape(4.dp))
                                            .clickable { selectedRank = r }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = r,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) TacticalPitchBlack else TacticalWhite,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Role Selection
                            Text(
                                text = "الدور / الصلاحية:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TacticalWhite
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("قائد وحدة", "ضابط استطلاع", "جندي رصد").forEach { rl ->
                                    val isSelected = selectedRole == rl
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (isSelected) TacticalWhite else TacticalSurfaceDark, RoundedCornerShape(4.dp))
                                            .border(1.dp, if (isSelected) TacticalWhite else TacticalBorderSubtle, RoundedCornerShape(4.dp))
                                            .clickable {
                                                selectedRole = rl
                                                title = when (rl) {
                                                    "قائد وحدة" -> "قائد قطاع استطلاع ورصد"
                                                    "ضابط استطلاع" -> "ضابط رصد وتحليل كهرومغناطيسي"
                                                    else -> "فني رادار واستطلاع ميداني"
                                                }
                                            }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = rl,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) TacticalPitchBlack else TacticalWhite,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("المسمى الوظيفي / المهمة", color = TacticalTextSecondary, fontSize = 11.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TacticalWhite,
                                    unfocusedTextColor = TacticalWhite,
                                    focusedBorderColor = TacticalWhite,
                                    unfocusedBorderColor = TacticalBorderSubtle,
                                    focusedContainerColor = TacticalSurfaceDark,
                                    unfocusedContainerColor = TacticalSurfaceDark
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Assigned Sector
                            Text(
                                text = "القطاع الميداني:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TacticalWhite
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                SouthGovernorate.entries.forEach { gov ->
                                    val isSelected = assignedSector == gov.arabicName
                                    Box(
                                        modifier = Modifier
                                            .background(if (isSelected) TacticalWhite else TacticalSurfaceDark, RoundedCornerShape(4.dp))
                                            .border(1.dp, if (isSelected) TacticalWhite else TacticalBorderSubtle, RoundedCornerShape(4.dp))
                                            .clickable { assignedSector = gov.arabicName }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = gov.arabicName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) TacticalPitchBlack else TacticalWhite,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }

                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = errorMessage!!,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (fullName.isBlank() || username.isBlank() || password.isBlank()) {
                                        errorMessage = "يرجى تعبئة كافة الحقول المطلوبة."
                                        return@Button
                                    }
                                    onCreateAccount(
                                        username.trim().lowercase(),
                                        fullName.trim(),
                                        title.trim(),
                                        password.trim(),
                                        selectedRank,
                                        selectedRole,
                                        assignedSector
                                    )
                                    // Reset form
                                    fullName = ""
                                    username = ""
                                    password = ""
                                    showCreateForm = false
                                    errorMessage = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TacticalWhite,
                                    contentColor = TacticalPitchBlack
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_new_account_button")
                            ) {
                                Text("حفظ واعتماد الحساب العسكري", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // List of Active Accounts
                Text(
                    text = "سجل الكادر العسكري المصرح له (${accounts.size} حسابات):",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TacticalWhite
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                accounts.forEach { acc ->
                    Surface(
                        color = TacticalSurfaceMedium,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (acc.isMasterAdmin) TacticalWhite else TacticalBorderSubtle
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("account_card_${acc.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (acc.isMasterAdmin) {
                                        Surface(
                                            color = TacticalWhite,
                                            shape = RoundedCornerShape(3.dp)
                                        ) {
                                            Text(
                                                text = "القائد العام",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = TacticalPitchBlack,
                                                    fontSize = 9.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Surface(
                                            color = TacticalSurfaceElevated,
                                            shape = RoundedCornerShape(3.dp)
                                        ) {
                                            Text(
                                                text = acc.role,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TacticalWhite,
                                                    fontSize = 9.sp
                                                ),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${acc.militaryRank} / ${acc.fullName}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TacticalWhite
                                        )
                                    )
                                }

                                Text(
                                    text = "${acc.title} | القطاع: ${acc.assignedSector}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TacticalTextSecondary,
                                        fontSize = 10.sp
                                    )
                                )

                                Text(
                                    text = "اسم المستخدم: ${acc.username} | كلمة السر: ${if (acc.isMasterAdmin) "123" else acc.password}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = Color.LightGray
                                    )
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Edit Account Button
                                IconButton(
                                    onClick = { editingAccount = acc },
                                    modifier = Modifier.testTag("edit_account_${acc.id}")
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "تعديل الحساب",
                                        tint = TacticalWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Delete button (Only for non-master accounts)
                                if (!acc.isMasterAdmin) {
                                    IconButton(
                                        onClick = { onDeleteAccount(acc.id) },
                                        modifier = Modifier.testTag("delete_account_${acc.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "حذف الحساب",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Account Dialog
    editingAccount?.let { acc ->
        var editFullName by remember(acc) { mutableStateOf(acc.fullName) }
        var editUsername by remember(acc) { mutableStateOf(acc.username) }
        var editPassword by remember(acc) { mutableStateOf(acc.password) }
        var editRank by remember(acc) { mutableStateOf(acc.militaryRank) }
        var editRole by remember(acc) { mutableStateOf(acc.role) }
        var editTitle by remember(acc) { mutableStateOf(acc.title) }
        var editSector by remember(acc) { mutableStateOf(acc.assignedSector) }
        var editError by remember { mutableStateOf<String?>(null) }

        Dialog(
            onDismissRequest = { editingAccount = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                color = TacticalSurfaceDark,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderStrong),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 24.dp)
                    .testTag("edit_account_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تعديل بيانات الحساب العسكري: ${acc.fullName}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TacticalWhite
                            )
                        )

                        IconButton(onClick = { editingAccount = null }) {
                            Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = TacticalWhite)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = TacticalBorderSubtle)

                    OutlinedTextField(
                        value = editFullName,
                        onValueChange = { editFullName = it },
                        label = { Text("الاسم الكامل", color = TacticalTextSecondary, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TacticalWhite,
                            unfocusedTextColor = TacticalWhite,
                            focusedBorderColor = TacticalWhite,
                            unfocusedBorderColor = TacticalBorderSubtle,
                            focusedContainerColor = TacticalSurfaceMedium,
                            unfocusedContainerColor = TacticalSurfaceMedium
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_field_fullname")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editUsername,
                            onValueChange = { editUsername = it },
                            label = { Text("اسم المستخدم", color = TacticalTextSecondary, fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TacticalWhite,
                                unfocusedTextColor = TacticalWhite,
                                focusedBorderColor = TacticalWhite,
                                unfocusedBorderColor = TacticalBorderSubtle,
                                focusedContainerColor = TacticalSurfaceMedium,
                                unfocusedContainerColor = TacticalSurfaceMedium
                            ),
                            modifier = Modifier.weight(1f).testTag("edit_field_username")
                        )

                        OutlinedTextField(
                            value = editPassword,
                            onValueChange = { editPassword = it },
                            label = { Text("كلمة السر", color = TacticalTextSecondary, fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TacticalWhite,
                                unfocusedTextColor = TacticalWhite,
                                focusedBorderColor = TacticalWhite,
                                unfocusedBorderColor = TacticalBorderSubtle,
                                focusedContainerColor = TacticalSurfaceMedium,
                                unfocusedContainerColor = TacticalSurfaceMedium
                            ),
                            modifier = Modifier.weight(1f).testTag("edit_field_password")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "الرتبة العسكرية:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TacticalWhite
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        rankOptions.forEach { r ->
                            val isSelected = editRank == r
                            Box(
                                modifier = Modifier
                                    .background(if (isSelected) TacticalWhite else TacticalSurfaceMedium, RoundedCornerShape(4.dp))
                                    .border(1.dp, if (isSelected) TacticalWhite else TacticalBorderSubtle, RoundedCornerShape(4.dp))
                                    .clickable { editRank = r }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = r,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) TacticalPitchBlack else TacticalWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("المسمى الوظيفي / المهمة", color = TacticalTextSecondary, fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TacticalWhite,
                            unfocusedTextColor = TacticalWhite,
                            focusedBorderColor = TacticalWhite,
                            unfocusedBorderColor = TacticalBorderSubtle,
                            focusedContainerColor = TacticalSurfaceMedium,
                            unfocusedContainerColor = TacticalSurfaceMedium
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_field_title")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "القطاع الميداني المعين:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TacticalWhite
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SouthGovernorate.entries.forEach { gov ->
                            val isSelected = editSector == gov.arabicName
                            Box(
                                modifier = Modifier
                                    .background(if (isSelected) TacticalWhite else TacticalSurfaceMedium, RoundedCornerShape(4.dp))
                                    .border(1.dp, if (isSelected) TacticalWhite else TacticalBorderSubtle, RoundedCornerShape(4.dp))
                                    .clickable { editSector = gov.arabicName }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = gov.arabicName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) TacticalPitchBlack else TacticalWhite,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }

                    if (editError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = editError!!,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { editingAccount = null },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TacticalWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TacticalBorderSubtle),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء")
                        }

                        Button(
                            onClick = {
                                if (editFullName.isBlank() || editUsername.isBlank() || editPassword.isBlank()) {
                                    editError = "يرجى تعبئة كافة الحقول المطلوبة."
                                    return@Button
                                }
                                onUpdateAccount(
                                    acc.id,
                                    editUsername.trim().lowercase(),
                                    editFullName.trim(),
                                    editTitle.trim(),
                                    editPassword.trim(),
                                    editRank.trim(),
                                    editRole.trim(),
                                    editSector.trim()
                                )
                                editingAccount = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TacticalWhite,
                                contentColor = TacticalPitchBlack
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).testTag("save_account_edits_button")
                        ) {
                            Text("تأكيد وحفظ التعديلات", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
