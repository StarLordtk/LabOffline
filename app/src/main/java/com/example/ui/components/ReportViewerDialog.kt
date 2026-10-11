package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LabSettingsEntity
import com.example.data.model.RegistrationEntity
import com.example.ui.screens.AbnormalFlagBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.LimsViewModel
import com.example.util.PdfReportGenerator
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportViewerDialog(
    reg: RegistrationEntity,
    viewModel: LimsViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val labSettings by viewModel.labSettings.collectAsState()
    val results by viewModel.getResultsForRegistration(reg.regNumber).collectAsState(initial = emptyList())
    val orderedTests by viewModel.getOrderedTestsForRegistration(reg.regNumber).collectAsState(initial = emptyList())

    val settings = labSettings ?: LabSettingsEntity()
    val resultsByTest = results.groupBy { it.testCode }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Clinical Diagnostic Report",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Blue800
                    )
                    Text(
                        text = "${reg.regNumber} • Electronically Approved",
                        fontSize = 11.sp,
                        color = StatusApproved,
                        fontWeight = FontWeight.Bold
                    )
                }
                WorkflowStatusChip(status = reg.overallWorkflowStatus)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                // Lab Header Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(settings.labName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Blue800)
                            Text(settings.labTagline, fontSize = 10.sp, color = Teal700)
                            Text(settings.address, fontSize = 10.sp, color = TextSecondary)
                            Text("Accreditation: ${settings.regNumber}", fontSize = 10.sp, color = Blue800, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Patient: ${reg.patientName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Age/Sex: ${reg.patientAge} Y / ${reg.patientGender}", fontSize = 11.sp)
                                    Text("UHID: ${reg.uhid}", fontSize = 11.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Ref: ${reg.referringDoctor}", fontSize = 11.sp)
                                    val dt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(reg.createdAt))
                                    Text("Date: $dt", fontSize = 11.sp)
                                    Text("Priority: ${reg.priority}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Blue800)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Table Header
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Blue50,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("INVESTIGATION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Blue800, modifier = Modifier.weight(1.4f))
                            Text("VALUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Blue800, modifier = Modifier.weight(0.9f))
                            Text("UNIT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Blue800, modifier = Modifier.weight(0.8f))
                            Text("REF INTERVAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Blue800, modifier = Modifier.weight(1.3f))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Test Parameters Table
                orderedTests.forEach { test ->
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${test.testName} (${test.specimenType})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    val testResults = resultsByTest[test.testCode] ?: emptyList()
                    items(testResults) { res ->
                        val isAbnormal = res.flag == "HIGH" || res.flag == "LOW" || res.flag == "CRITICAL"
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            color = if (isAbnormal) Color(0xFFFFF1F2) else Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = res.paramName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isAbnormal) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1.4f)
                                )
                                Text(
                                    text = res.resultValue.ifBlank { "Pending" },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAbnormal) StatusRejected else TextPrimary,
                                    modifier = Modifier.weight(0.9f)
                                )
                                Text(
                                    text = res.unit,
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.weight(0.8f)
                                )
                                Text(
                                    text = res.referenceIntervalText,
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.weight(1.3f)
                                )
                            }
                        }
                    }
                }

                // Doctor Signature Block
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = StatusApprovedBg,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "CLINICALLY VERIFIED\n& APPROVED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusApproved,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(settings.pathologistName, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text(settings.pathologistDesignation, fontSize = 10.sp, color = TextSecondary)
                                Text("LabPulse LIMS Certified Sign-off", fontSize = 9.sp, color = Blue800)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val pdfFile = PdfReportGenerator.generateClinicalReportPdf(
                            context = context,
                            settings = settings,
                            reg = reg,
                            orderedTests = orderedTests,
                            results = results
                        )
                        PdfReportGenerator.shareOrOpenPdf(context, pdfFile)
                        viewModel.showSnackbar("PDF Report generated: ${pdfFile.name}")
                    } catch (e: Exception) {
                        viewModel.showSnackbar("PDF Generation failed: ${e.message}")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Blue800)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Print / Share PDF")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
