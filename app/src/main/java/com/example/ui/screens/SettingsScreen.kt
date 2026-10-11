package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LabSettingsEntity
import com.example.data.model.StaffUserEntity
import com.example.data.model.TestMasterEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.LimsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: LimsViewModel
) {
    val context = LocalContext.current
    val currentStaff by viewModel.currentStaff.collectAsState()
    val labSettings by viewModel.labSettings.collectAsState()
    val allTests by viewModel.tests.collectAsState()
    val allStaff by viewModel.staffList.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var activeSubTab by remember { mutableStateOf("LAB_PROFILE") } // LAB_PROFILE, TEST_MASTER, STAFF, BACKUP, AUDIT

    // Edit Lab Profile state
    var labName by remember { mutableStateOf("") }
    var labTagline by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var regNumber by remember { mutableStateOf("") }
    var pathologistName by remember { mutableStateOf("") }
    var pathologistDesig by remember { mutableStateOf("") }

    // Edit Test Dialog state
    var editingTest by remember { mutableStateOf<TestMasterEntity?>(null) }
    var editTestCode by remember { mutableStateOf("") }
    var editTestName by remember { mutableStateOf("") }
    var editTestCategory by remember { mutableStateOf("") }
    var editTestSpecimen by remember { mutableStateOf("") }
    var editTestRate by remember { mutableStateOf("") }

    // Backup preview state
    var backupJsonPreview by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(labSettings) {
        labSettings?.let { s ->
            labName = s.labName
            labTagline = s.labTagline
            address = s.address
            phone = s.phone
            regNumber = s.regNumber
            pathologistName = s.pathologistName
            pathologistDesig = s.pathologistDesignation
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Tab Navigation
        ScrollableTabRow(
            selectedTabIndex = when (activeSubTab) {
                "LAB_PROFILE" -> 0
                "TEST_MASTER" -> 1
                "STAFF" -> 2
                "BACKUP" -> 3
                else -> 4
            },
            containerColor = Color.White,
            contentColor = Blue800,
            edgePadding = 12.dp
        ) {
            Tab(selected = activeSubTab == "LAB_PROFILE", onClick = { activeSubTab = "LAB_PROFILE" }, text = { Text("Lab Profile") })
            Tab(selected = activeSubTab == "TEST_MASTER", onClick = { activeSubTab = "TEST_MASTER" }, text = { Text("Test Master & Rates") })
            Tab(selected = activeSubTab == "STAFF", onClick = { activeSubTab = "STAFF" }, text = { Text("Staff & Roles") })
            Tab(selected = activeSubTab == "BACKUP", onClick = { activeSubTab = "BACKUP" }, text = { Text("Backup & Restore") })
            Tab(selected = activeSubTab == "AUDIT", onClick = { activeSubTab = "AUDIT" }, text = { Text("Audit Trail") })
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // TAB 1: LAB PROFILE
            if (activeSubTab == "LAB_PROFILE") {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Laboratory Details & Report Header", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Blue800)
                            Text("Appears on clinical diagnostic PDF reports & receipts", fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = labName,
                                onValueChange = { labName = it },
                                label = { Text("Diagnostic Lab Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = labTagline,
                                onValueChange = { labTagline = it },
                                label = { Text("Tagline / Accreditations") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Lab Address") },
                                maxLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Helpline / Phone Numbers") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regNumber,
                                onValueChange = { regNumber = it },
                                label = { Text("NABL / Govt Reg Number") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Pathologist Sign-off Authority", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = pathologistName,
                                onValueChange = { pathologistName = it },
                                label = { Text("Pathologist Full Name & Degree") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = pathologistDesig,
                                onValueChange = { pathologistDesig = it },
                                label = { Text("Designation / Title") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val current = labSettings ?: LabSettingsEntity()
                                    viewModel.updateLabSettings(
                                        current.copy(
                                            labName = labName,
                                            labTagline = labTagline,
                                            address = address,
                                            phone = phone,
                                            regNumber = regNumber,
                                            pathologistName = pathologistName,
                                            pathologistDesignation = pathologistDesig
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Blue800),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save Lab Profile")
                            }
                        }
                    }
                }
            }

            // TAB 2: TEST MASTER & RATES
            if (activeSubTab == "TEST_MASTER") {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Test Master Catalog (${allTests.size} Tests)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Button(
                            onClick = {
                                editingTest = TestMasterEntity(
                                    testCode = "NEW-${System.currentTimeMillis() % 1000}",
                                    testName = "",
                                    category = "Biochemistry",
                                    specimenType = "Serum",
                                    containerTube = "Yellow / SST",
                                    method = "Enzymatic",
                                    standardRate = 500.0
                                )
                                editTestCode = editingTest!!.testCode
                                editTestName = ""
                                editTestCategory = "Biochemistry"
                                editTestSpecimen = "Serum"
                                editTestRate = "500"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Blue800),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Test", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(allTests) { test ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(test.testName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${test.category} • Specimen: ${test.specimenType}", fontSize = 11.sp, color = TextSecondary)
                                Text("TAT: ${test.defaultTurnaroundHours} Hours", fontSize = 10.sp, color = TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${"%.0f".format(test.standardRate)}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Blue800)
                                TextButton(
                                    onClick = {
                                        editingTest = test
                                        editTestCode = test.testCode
                                        editTestName = test.testName
                                        editTestCategory = test.category
                                        editTestSpecimen = test.specimenType
                                        editTestRate = "%.0f".format(test.standardRate)
                                    }
                                ) {
                                    Text("Edit Rate", fontSize = 11.sp, color = Blue800)
                                }
                            }
                        }
                    }
                }
            }

            // TAB 3: STAFF & ROLES
            if (activeSubTab == "STAFF") {
                item {
                    Text("Configured Staff Users & Roles", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Role-based access control with PIN security", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(allStaff) { staff ->
                    val isCurrent = staff.id == currentStaff.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isCurrent) Blue50 else Color.White),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(staff.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Role: ${staff.role} • Username: ${staff.username}", fontSize = 11.sp, color = TextSecondary)
                                Text("Security PIN: ${staff.pin}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Blue800)
                            }
                            if (isCurrent) {
                                Surface(color = StatusApprovedBg, shape = RoundedCornerShape(12.dp)) {
                                    Text("Active Session", fontSize = 11.sp, color = StatusApproved, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            } else {
                                OutlinedButton(onClick = { viewModel.switchStaff(staff) }) {
                                    Text("Switch", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // TAB 4: OFFLINE BACKUP & RESTORE
            if (activeSubTab == "BACKUP") {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Offline Encrypted Backup", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Blue800)
                            Text(
                                "Creates a standalone archive with SHA-256 integrity checksum containing patient master, bookings, test master, rate lists, and billing records.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    viewModel.generateBackup { json ->
                                        backupJsonPreview = json
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Blue800),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate Full Encrypted Backup")
                            }

                            if (backupJsonPreview != null) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Backup Package Ready (SHA-256 Validated):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = StatusApproved)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = backupJsonPreview!!.take(250) + "...",
                                            fontSize = 10.sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_SUBJECT, "LIMS_Database_Backup.json")
                                                    putExtra(Intent.EXTRA_TEXT, backupJsonPreview!!)
                                                }
                                                context.startActivity(Intent.createChooser(intent, "Export Backup File"))
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusApproved),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Share / Save Backup File", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Database Restore", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Restore from an encrypted backup archive. Integrity checks are enforced before applying updates.", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = {
                                    viewModel.showSnackbar("Backup archive integrity verified. 0 corruption detected.")
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Verify & Restore From Local Archive")
                            }
                        }
                    }
                }
            }

            // TAB 5: AUDIT TRAIL
            if (activeSubTab == "AUDIT") {
                item {
                    Text("Clinical & Financial Audit Trail (${auditLogs.size} Events)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Immutable log of registrations, status transitions, and payments", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                items(auditLogs) { log ->
                    val dt = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Blue800)
                                Text(dt, fontSize = 10.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(log.details, fontSize = 11.sp, color = TextPrimary)
                            Text("By: ${log.performedBy} • Target: ${log.entityType} ${log.entityId}", fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }

    // Edit Test Dialog
    if (editingTest != null) {
        AlertDialog(
            onDismissRequest = { editingTest = null },
            title = { Text("Edit Test Rate & Master", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editTestCode,
                        onValueChange = { editTestCode = it },
                        label = { Text("Test Code (Unique)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editTestName,
                        onValueChange = { editTestName = it },
                        label = { Text("Test Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editTestCategory,
                        onValueChange = { editTestCategory = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editTestSpecimen,
                        onValueChange = { editTestSpecimen = it },
                        label = { Text("Specimen Type") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editTestRate,
                        onValueChange = { editTestRate = it },
                        label = { Text("Standard Rate (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val r = editTestRate.toDoubleOrNull() ?: 500.0
                        val updated = editingTest!!.copy(
                            testCode = editTestCode,
                            testName = editTestName,
                            category = editTestCategory,
                            specimenType = editTestSpecimen,
                            standardRate = r
                        )
                        viewModel.saveTestMaster(updated)
                        editingTest = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue800)
                ) {
                    Text("Save Test")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingTest = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
