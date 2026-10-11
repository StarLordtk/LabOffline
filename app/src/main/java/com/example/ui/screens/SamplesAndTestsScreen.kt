package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RegistrationEntity
import com.example.data.model.SampleEntity
import com.example.ui.components.WorkflowStatusChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.LimsViewModel
import com.example.util.BarcodeView

@Composable
fun SamplesAndTestsScreen(
    viewModel: LimsViewModel,
    onOpenResultEntry: (RegistrationEntity) -> Unit,
    onOpenReport: (RegistrationEntity) -> Unit
) {
    val registrations by viewModel.registrations.collectAsState()
    val samples by viewModel.samples.collectAsState()
    val currentStaff by viewModel.currentStaff.collectAsState()
    val currentStatusFilter by viewModel.samplesStatusFilter.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedViewMode by remember { mutableStateOf("PATIENT_WISE") } // "PATIENT_WISE" or "SAMPLE_WISE"
    var rejectionDialogSample by remember { mutableStateOf<SampleEntity?>(null) }
    var rejectionReason by remember { mutableStateOf("") }
    var isRecollectionMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Top Filter Bar & Search
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // View Mode Toggle (Patient-Wise vs Sample-Wise)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        FilterChip(
                            selected = selectedViewMode == "PATIENT_WISE",
                            onClick = { selectedViewMode = "PATIENT_WISE" },
                            label = { Text("Patient-Wise Orders", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        FilterChip(
                            selected = selectedViewMode == "SAMPLE_WISE",
                            onClick = { selectedViewMode = "SAMPLE_WISE" },
                            label = { Text("Specimen Barcodes", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    Surface(
                        color = Blue50,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable {
                            searchQuery = "SMP-"
                        }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = Blue800, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan", fontSize = 11.sp, color = Blue800, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search name, UHID, Reg No, or Barcode...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Status Filter Chips
                val filterOptions = listOf(
                    "ALL" to "All Status",
                    "BOOKED" to "Booked",
                    "SAMPLE_COLLECTED" to "Collected",
                    "IN_PROCESSING" to "In Lab",
                    "RESULT_ENTERED" to "Ready for Review",
                    "APPROVED" to "Approved & Released"
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(filterOptions) { (key, label) ->
                        val isSel = currentStatusFilter == key
                        FilterChip(
                            selected = isSel,
                            onClick = { viewModel.setSamplesStatusFilter(key) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Blue800,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // List Content
        if (selectedViewMode == "PATIENT_WISE") {
            val filteredRegs = registrations.filter { reg ->
                val matchesSearch = searchQuery.isBlank() ||
                        reg.patientName.contains(searchQuery, ignoreCase = true) ||
                        reg.uhid.contains(searchQuery, ignoreCase = true) ||
                        reg.regNumber.contains(searchQuery, ignoreCase = true) ||
                        reg.patientMobile.contains(searchQuery)

                val matchesStatus = currentStatusFilter == "ALL" || reg.overallWorkflowStatus == currentStatusFilter
                matchesSearch && matchesStatus
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
            ) {
                if (filteredRegs.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No patient orders found matching filters", color = TextSecondary)
                        }
                    }
                } else {
                    items(filteredRegs) { reg ->
                        PatientWorkflowCard(
                            reg = reg,
                            viewModel = viewModel,
                            onEnterResults = { onOpenResultEntry(reg) },
                            onOpenReport = { onOpenReport(reg) },
                            onRejectSample = { sample, isRecollect ->
                                rejectionDialogSample = sample
                                isRecollectionMode = isRecollect
                                rejectionReason = ""
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        } else {
            // Specimen Barcodes List
            val filteredSamples = samples.filter { sample ->
                val matchesSearch = searchQuery.isBlank() ||
                        sample.sampleBarcode.contains(searchQuery, ignoreCase = true) ||
                        sample.patientName.contains(searchQuery, ignoreCase = true) ||
                        sample.regNumber.contains(searchQuery, ignoreCase = true)

                val matchesStatus = currentStatusFilter == "ALL" || sample.status == currentStatusFilter
                matchesSearch && matchesStatus
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
            ) {
                if (filteredSamples.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No specimen barcodes found", color = TextSecondary)
                        }
                    }
                } else {
                    items(filteredSamples) { sample ->
                        SampleBarcodeDetailCard(
                            sample = sample,
                            onCollect = { viewModel.markSampleCollected(sample.sampleBarcode) },
                            onReceiveInLab = { viewModel.markSampleInLab(sample.sampleBarcode) },
                            onReject = {
                                rejectionDialogSample = sample
                                isRecollectionMode = false
                                rejectionReason = ""
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    // Rejection / Recollection Reason Dialog
    if (rejectionDialogSample != null) {
        val s = rejectionDialogSample!!
        AlertDialog(
            onDismissRequest = { rejectionDialogSample = null },
            title = {
                Text(
                    text = if (isRecollectionMode) "Request Sample Recollection" else "Reject Clinical Sample",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = StatusRejected
                )
            },
            text = {
                Column {
                    Text("Sample: ${s.sampleBarcode} (${s.specimenType})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Patient: ${s.patientName}", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Reason for ${if (isRecollectionMode) "recollection" else "rejection"} (required):", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        placeholder = { Text("e.g. Hemolyzed, Clotted, Insufficient volume, Wrong tube...") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Hemolyzed", "Clotted", "Insufficient Vol", "Lipemic").forEach { quickReason ->
                            Surface(
                                modifier = Modifier.clickable { rejectionReason = quickReason },
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(quickReason, fontSize = 10.sp, modifier = Modifier.padding(4.dp), color = TextPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = rejectionReason.ifBlank { "Sample quality compromised" }
                        if (isRecollectionMode) {
                            viewModel.requestSampleRecollection(s.sampleBarcode, reason)
                        } else {
                            viewModel.rejectSample(s.sampleBarcode, reason)
                        }
                        rejectionDialogSample = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRejected)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectionDialogSample = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PatientWorkflowCard(
    reg: RegistrationEntity,
    viewModel: LimsViewModel,
    onEnterResults: () -> Unit,
    onOpenReport: () -> Unit,
    onRejectSample: (SampleEntity, Boolean) -> Unit
) {
    val samplesForReg by viewModel.getSamplesForRegistration(reg.regNumber).collectAsState(initial = emptyList())
    val orderedTests by viewModel.getOrderedTestsForRegistration(reg.regNumber).collectAsState(initial = emptyList())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Patient & Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = reg.patientName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                    Text(text = "${reg.patientAge} Yrs / ${reg.patientGender} • ${reg.uhid}", fontSize = 11.sp, color = TextSecondary)
                }
                WorkflowStatusChip(status = reg.overallWorkflowStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Reg No: ${reg.regNumber} • Ref: ${reg.referringDoctor}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Tests Booked
            Text("Tests Booked (${orderedTests.size}):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            orderedTests.forEach { t ->
                Row(modifier = Modifier.padding(vertical = 1.dp)) {
                    Text("• ${t.testName}", fontSize = 12.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                    Text(t.specimenType, fontSize = 11.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Samples & Barcodes for this Patient
            if (samplesForReg.isNotEmpty()) {
                Text("Specimen Tubes & Barcodes:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                samplesForReg.forEach { s ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(s.sampleBarcode, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Blue800)
                                    Text("${s.specimenType} • ${s.containerTube}", fontSize = 10.sp, color = TextSecondary)
                                }
                                WorkflowStatusChip(status = s.status)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            BarcodeView(code = s.sampleBarcode, height = 28, showText = false)
                            Spacer(modifier = Modifier.height(6.dp))

                            // Sample Workflow Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (s.status == "BOOKED") {
                                    Button(
                                        onClick = { viewModel.markSampleCollected(s.sampleBarcode) },
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusCollected),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Science, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Collect", fontSize = 11.sp)
                                    }
                                } else if (s.status == "COLLECTED") {
                                    Button(
                                        onClick = { viewModel.markSampleInLab(s.sampleBarcode) },
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusProcessing),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Biotech, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Mark In Lab", fontSize = 11.sp)
                                    }
                                }

                                if (s.status != "REJECTED") {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    TextButton(
                                        onClick = { onRejectSample(s, false) },
                                        contentPadding = PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Text("Reject", fontSize = 11.sp, color = StatusRejected)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Workflow Actions for this Patient
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Enter / Review Results button
                OutlinedButton(
                    onClick = onEnterResults,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (reg.overallWorkflowStatus == "APPROVED") "View Results" else "Enter Results",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (reg.overallWorkflowStatus == "APPROVED" || reg.overallWorkflowStatus == "RELEASED") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOpenReport,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusApproved),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SampleBarcodeDetailCard(
    sample: SampleEntity,
    onCollect: () -> Unit,
    onReceiveInLab: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(sample.sampleBarcode, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Blue800)
                    Text("Patient: ${sample.patientName} • Reg: ${sample.regNumber}", fontSize = 11.sp, color = TextSecondary)
                }
                WorkflowStatusChip(status = sample.status)
            }

            Spacer(modifier = Modifier.height(10.dp))
            BarcodeView(code = sample.sampleBarcode, height = 36)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Specimen: ${sample.specimenType} (${sample.containerTube})", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            if (sample.rejectionReason.isNotBlank()) {
                Text("Rejection Note: ${sample.rejectionReason}", fontSize = 11.sp, color = StatusRejected, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (sample.status == "BOOKED") {
                    Button(onClick = onCollect, colors = ButtonDefaults.buttonColors(containerColor = StatusCollected)) {
                        Text("Mark Collected", fontSize = 11.sp)
                    }
                } else if (sample.status == "COLLECTED") {
                    Button(onClick = onReceiveInLab, colors = ButtonDefaults.buttonColors(containerColor = StatusProcessing)) {
                        Text("Receive In Lab", fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedButton(onClick = onReject) {
                    Text("Reject", fontSize = 11.sp, color = StatusRejected)
                }
            }
        }
    }
}
