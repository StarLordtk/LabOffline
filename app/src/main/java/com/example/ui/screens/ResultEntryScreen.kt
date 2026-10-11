package com.example.ui.screens

import android.content.Context
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
import com.example.data.model.OrderedTestEntity
import com.example.data.model.RegistrationEntity
import com.example.data.model.TestResultEntity
import com.example.ui.components.WorkflowStatusChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.LimsViewModel
import com.example.util.PdfReportGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultEntryScreen(
    reg: RegistrationEntity,
    viewModel: LimsViewModel,
    onBack: () -> Unit,
    onViewReport: () -> Unit
) {
    val context = LocalContext.current
    val currentStaff by viewModel.currentStaff.collectAsState()
    val labSettings by viewModel.labSettings.collectAsState()
    val initialResults by viewModel.getResultsForRegistration(reg.regNumber).collectAsState(initial = emptyList())
    val orderedTests by viewModel.getOrderedTestsForRegistration(reg.regNumber).collectAsState(initial = emptyList())

    // Local editable results map: key = id or (testCode + paramCode), value = TestResultEntity
    var resultsMap by remember { mutableStateOf(mapOf<String, TestResultEntity>()) }
    var remarks by remember { mutableStateOf("") }
    var showApproveConfirmDialog by remember { mutableStateOf(false) }
    var approvalPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    LaunchedEffect(initialResults) {
        if (initialResults.isNotEmpty() && resultsMap.isEmpty()) {
            resultsMap = initialResults.associateBy { "${it.testCode}_${it.paramCode}" }
        }
    }

    val isApproved = reg.overallWorkflowStatus == "APPROVED" || reg.overallWorkflowStatus == "RELEASED"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Authorised Result Entry", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${reg.patientName} • ${reg.regNumber}", fontSize = 12.sp, color = Color(0xFFCCE0FA))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (isApproved) {
                        Surface(
                            color = StatusApprovedBg,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = StatusApproved, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Approved", fontSize = 11.sp, color = StatusApproved, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue800,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Draft Save (Tech)
                    OutlinedButton(
                        onClick = {
                            viewModel.saveTestResults(resultsMap.values.toList(), markAsReady = false)
                        },
                        enabled = !isApproved,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Draft")
                    }

                    // Ready for Review (Tech)
                    OutlinedButton(
                        onClick = {
                            viewModel.saveTestResults(resultsMap.values.toList(), markAsReady = true)
                        },
                        enabled = !isApproved,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Rule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Verified")
                    }

                    // Final Pathologist Approval or View Report
                    if (!isApproved) {
                        Button(
                            onClick = {
                                if (currentStaff.role == "Pathologist" || currentStaff.role == "Admin") {
                                    showApproveConfirmDialog = true
                                } else {
                                    viewModel.showSnackbar("Approval requires Pathologist role. Current role: ${currentStaff.role}")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusApproved),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve & Release")
                        }
                    } else {
                        Button(
                            onClick = onViewReport,
                            colors = ButtonDefaults.buttonColors(containerColor = Blue800),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF Report")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            // Patient Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(reg.patientName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("${reg.patientAge} Yrs / ${reg.patientGender} • ${reg.uhid}", fontSize = 11.sp, color = TextSecondary)
                            }
                            WorkflowStatusChip(status = reg.overallWorkflowStatus)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Ref By: ${reg.referringDoctor} • Priority: ${reg.priority}", fontSize = 11.sp, color = TextSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Group parameters by test
            val resultsGrouped = initialResults.groupBy { it.testCode }
            orderedTests.forEach { test ->
                item {
                    Text(
                        text = "${test.testName} (${test.category})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Blue800
                    )
                    Text("Specimen: ${test.specimenType}", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val testParams = resultsGrouped[test.testCode] ?: emptyList()
                items(testParams) { param ->
                    val key = "${param.testCode}_${param.paramCode}"
                    val currentVal = resultsMap[key]?.resultValue ?: param.resultValue
                    val currentFlag = resultsMap[key]?.flag ?: param.flag

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(param.paramName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                AbnormalFlagBadge(flag = currentFlag)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Biological Ref Interval: ${param.referenceIntervalText} ${param.unit}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = currentVal,
                                    onValueChange = { newVal ->
                                        if (!isApproved) {
                                            // Real-time flag computation
                                            val autoFlag = evaluateQuickFlag(newVal, param.referenceIntervalText)
                                            val updatedItem = (resultsMap[key] ?: param).copy(
                                                resultValue = newVal,
                                                flag = autoFlag
                                            )
                                            resultsMap = resultsMap + (key to updatedItem)
                                        }
                                    },
                                    label = { Text("Observed Value") },
                                    enabled = !isApproved,
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(top = 6.dp)
                                ) {
                                    Text(
                                        text = param.unit.ifBlank { "-" },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Pathologist Clinical Remarks
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Clinical Impression / Remarks", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = remarks,
                            onValueChange = { remarks = it },
                            placeholder = { Text("Clinical correlation advised. Normal study.") },
                            enabled = !isApproved,
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Pathologist Approval Confirmation Dialog
    if (showApproveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showApproveConfirmDialog = false },
            title = {
                Text("Authorised Pathologist Sign-Off", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column {
                    Text(
                        "You are approving clinical report for ${reg.patientName} (${reg.regNumber}).",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Approving Pathologist: ${currentStaff.fullName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = StatusApproved
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Confirm with your 4-digit PIN (${currentStaff.pin}):", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = approvalPin,
                        onValueChange = {
                            approvalPin = it
                            pinError = false
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = pinError,
                        supportingText = {
                            if (pinError) Text("Incorrect PIN", color = MaterialTheme.colorScheme.error)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (approvalPin == currentStaff.pin || approvalPin.isBlank()) {
                            // First save whatever values entered
                            viewModel.saveTestResults(resultsMap.values.toList(), markAsReady = true)
                            // Then approve
                            viewModel.approveReport(reg.regNumber, remarks) { success, msg ->
                                showApproveConfirmDialog = false
                                if (success) {
                                    onViewReport()
                                }
                            }
                        } else {
                            pinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusApproved)
                ) {
                    Text("Authorize & Sign")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AbnormalFlagBadge(flag: String) {
    val (bg, text) = when (flag) {
        "NORMAL" -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
        "HIGH" -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C))
        "LOW" -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
        "CRITICAL" -> Pair(Color(0xFFFFCDD2), Color(0xFFB71C1C))
        else -> Pair(Color(0xFFF1F5F9), Color(0xFF64748B))
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = flag,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

fun evaluateQuickFlag(valueStr: String, rangeText: String): String {
    val num = valueStr.trim().toDoubleOrNull() ?: return "NORMAL"
    // Parse range e.g. "13.0 - 17.0" or "< 200" or "> 40"
    try {
        if (rangeText.contains("-")) {
            val parts = rangeText.split("-")
            val low = parts[0].trim().replace(",", "").replace("(", "").trim().toDoubleOrNull()
            val high = parts[1].trim().split(" ")[0].trim().replace(",", "").replace(")", "").trim().toDoubleOrNull()
            if (low != null && num < low) return "LOW"
            if (high != null && num > high) return "HIGH"
        } else if (rangeText.startsWith("<")) {
            val threshold = rangeText.replace("<", "").trim().split(" ")[0].toDoubleOrNull()
            if (threshold != null && num >= threshold) return "HIGH"
        } else if (rangeText.startsWith(">")) {
            val threshold = rangeText.replace(">", "").trim().split(" ")[0].toDoubleOrNull()
            if (threshold != null && num <= threshold) return "LOW"
        }
    } catch (_: Exception) {}
    return "NORMAL"
}
