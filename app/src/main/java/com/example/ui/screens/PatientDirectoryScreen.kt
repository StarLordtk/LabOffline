package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.PatientEntity
import com.example.data.model.RegistrationEntity
import com.example.ui.components.WorkflowStatusChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.LimsScreen
import com.example.ui.viewmodel.LimsViewModel

@Composable
fun PatientDirectoryScreen(
    viewModel: LimsViewModel,
    onNavigate: (LimsScreen) -> Unit,
    onOpenRegistration: (RegistrationEntity) -> Unit
) {
    val patients by viewModel.patients.collectAsState()
    val allRegistrations by viewModel.registrations.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedPatient by remember { mutableStateOf<PatientEntity?>(null) }

    val filteredPatients = patients.filter { p ->
        searchQuery.isBlank() ||
                p.fullName.contains(searchQuery, ignoreCase = true) ||
                p.uhid.contains(searchQuery, ignoreCase = true) ||
                p.mobile.contains(searchQuery) ||
                p.referringDoctor.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Search & Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Patient Master Directory",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Blue800
                        )
                        Text(
                            text = "${patients.size} Registered Patients • Search by Name, UHID, Mobile",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.resetRegistrationForm()
                            onNavigate(LimsScreen.REGISTRATION)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Blue800),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Patient", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Patient Name, UHID, or Mobile...") },
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
            }
        }

        // Patient List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            if (filteredPatients.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No patients matching '$searchQuery'", color = TextSecondary)
                    }
                }
            } else {
                items(filteredPatients) { patient ->
                    val patientVisits = allRegistrations.filter { it.uhid == patient.uhid }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable { selectedPatient = patient },
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = Blue50,
                                        shape = CircleShape,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = patient.fullName.take(1).uppercase(),
                                                color = Blue800,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(patient.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${patient.ageYears} Yrs / ${patient.gender} • ${patient.patientCategory}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }

                                Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(6.dp)) {
                                    Text(
                                        text = "${patientVisits.size} Visit${if (patientVisits.size > 1) "s" else ""}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Blue800,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("UHID: ${patient.uhid}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Blue800)
                                    Text("Mobile: ${patient.mobile} • Doctor: ${patient.referringDoctor}", fontSize = 11.sp, color = TextSecondary)
                                }

                                TextButton(
                                    onClick = {
                                        viewModel.resetRegistrationForm()
                                        viewModel.autoFillExistingPatient(patient)
                                        viewModel.regStep.value = 2 // jump right into adding tests
                                        onNavigate(LimsScreen.REGISTRATION)
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Book Test", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Blue800)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Patient History Detail Dialog
    if (selectedPatient != null) {
        val p = selectedPatient!!
        val visits = allRegistrations.filter { it.uhid == p.uhid }

        AlertDialog(
            onDismissRequest = { selectedPatient = null },
            title = {
                Column {
                    Text(p.fullName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("UHID: ${p.uhid} • ${p.ageYears}y / ${p.gender}", fontSize = 12.sp, color = TextSecondary)
                }
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp)) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Contact & Referral Details:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Mobile: ${p.mobile}", fontSize = 11.sp)
                                if (p.email.isNotBlank()) Text("Email: ${p.email}", fontSize = 11.sp)
                                if (p.address.isNotBlank()) Text("Address: ${p.address}", fontSize = 11.sp)
                                Text("Referring Doctor: ${p.referringDoctor}", fontSize = 11.sp)
                                if (p.hospital.isNotBlank()) Text("Hospital: ${p.hospital}", fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Registration History (${visits.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (visits.isEmpty()) {
                        item {
                            Text("No visits logged yet", fontSize = 12.sp, color = TextSecondary)
                        }
                    } else {
                        items(visits) { reg ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        selectedPatient = null
                                        onOpenRegistration(reg)
                                    },
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(reg.regNumber, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Blue800)
                                        Text("Bill: ₹${"%.0f".format(reg.netAmount)} • Paid: ₹${"%.0f".format(reg.paidAmount)}", fontSize = 10.sp, color = TextSecondary)
                                    }
                                    WorkflowStatusChip(status = reg.overallWorkflowStatus)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetRegistrationForm()
                        viewModel.autoFillExistingPatient(p)
                        viewModel.regStep.value = 2
                        selectedPatient = null
                        onNavigate(LimsScreen.REGISTRATION)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue800)
                ) {
                    Text("New Booking")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPatient = null }) {
                    Text("Close")
                }
            }
        )
    }
}
