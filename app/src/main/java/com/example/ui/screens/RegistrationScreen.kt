package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PatientEntity
import com.example.data.model.TestMasterEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.LimsScreen
import com.example.ui.viewmodel.LimsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    viewModel: LimsViewModel,
    onBookingSuccess: (String) -> Unit
) {
    val currentStep by viewModel.regStep.collectAsState()
    val allPatients by viewModel.patients.collectAsState()
    val allTests by viewModel.activeTests.collectAsState()
    val rateLists by viewModel.rateLists.collectAsState()

    // Form states
    val patientName by viewModel.regPatientName.collectAsState()
    val age by viewModel.regAge.collectAsState()
    val gender by viewModel.regGender.collectAsState()
    val mobile by viewModel.regMobile.collectAsState()
    val email by viewModel.regEmail.collectAsState()
    val address by viewModel.regAddress.collectAsState()
    val doctor by viewModel.regDoctor.collectAsState()
    val hospital by viewModel.regHospital.collectAsState()
    val category by viewModel.regCategory.collectAsState()
    val notes by viewModel.regNotes.collectAsState()
    val existingUhid by viewModel.regExistingUhid.collectAsState()

    val selectedTests by viewModel.regSelectedTests.collectAsState()
    val selectedRateList by viewModel.regSelectedRateList.collectAsState()
    val discountPercent by viewModel.regDiscountRatePercent.collectAsState()
    val priority by viewModel.regPriority.collectAsState()
    val collectionLocation by viewModel.regCollectionLocation.collectAsState()

    val customDiscount by viewModel.regCustomDiscount.collectAsState()
    val discountReason by viewModel.regDiscountReason.collectAsState()
    val paidAmount by viewModel.regPaidAmount.collectAsState()
    val paymentMode by viewModel.regPaymentMode.collectAsState()
    val transactionRef by viewModel.regTransactionRef.collectAsState()

    var testSearchQuery by remember { mutableStateOf("") }
    var patientSearchQuery by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Subtotal calculation
    val subtotal = remember(selectedTests, discountPercent) {
        selectedTests.sumOf { it.standardRate * (1.0 - (discountPercent / 100.0)) }
    }
    val discountVal = customDiscount.toDoubleOrNull() ?: 0.0
    val netAmount = (subtotal - discountVal).coerceAtLeast(0.0)
    val paidVal = paidAmount.toDoubleOrNull() ?: 0.0
    val dueAmount = (netAmount - paidVal).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "3-Step Patient Registration & Booking",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Blue800
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Stepper Visual Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepBubble(step = 1, currentStep = currentStep, title = "Patient Info")
                    StepDivider(isActive = currentStep >= 2, modifier = Modifier.weight(1f))
                    StepBubble(step = 2, currentStep = currentStep, title = "Add Tests")
                    StepDivider(isActive = currentStep >= 3, modifier = Modifier.weight(1f))
                    StepBubble(step = 3, currentStep = currentStep, title = "Billing & Pay")
                }
            }
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
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.regStep.value = currentStep - 1 },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.resetRegistrationForm() },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reset")
                        }
                    }

                    if (currentStep < 3) {
                        Button(
                            onClick = {
                                if (currentStep == 1) {
                                    if (patientName.isBlank() || mobile.isBlank() || age.isBlank()) {
                                        errorMessage = "Please enter patient name, mobile, and age"
                                        return@Button
                                    }
                                } else if (currentStep == 2) {
                                    if (selectedTests.isEmpty()) {
                                        errorMessage = "Please select at least one test"
                                        return@Button
                                    }
                                    if (paidAmount.isBlank()) {
                                        viewModel.regPaidAmount.value = "%.0f".format(netAmount)
                                    }
                                }
                                errorMessage = null
                                viewModel.regStep.value = currentStep + 1
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Blue800),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Continue to Step ${currentStep + 1}")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.submitBooking(
                                    onSuccess = { regNo -> onBookingSuccess(regNo) },
                                    onError = { err -> errorMessage = err }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusApproved),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Complete & Generate Receipt")
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
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = StatusRejectedBg)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = StatusRejected)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = errorMessage!!, color = StatusRejected, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // STEP 1: PATIENT DETAILS
            if (currentStep == 1) {
                // Search Existing Patient to avoid duplicates
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Search Existing Patient Records",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = patientSearchQuery,
                                onValueChange = { patientSearchQuery = it },
                                placeholder = { Text("Search by name, UHID, or mobile...") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (patientSearchQuery.isNotBlank()) {
                                val matches = allPatients.filter {
                                    it.fullName.contains(patientSearchQuery, ignoreCase = true) ||
                                            it.mobile.contains(patientSearchQuery) ||
                                            it.uhid.contains(patientSearchQuery, ignoreCase = true)
                                }
                                if (matches.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Existing matches (tap to autofill):", fontSize = 11.sp, color = TextSecondary)
                                    matches.take(3).forEach { p ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clickable {
                                                    viewModel.autoFillExistingPatient(p)
                                                    patientSearchQuery = ""
                                                },
                                            color = Blue50,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("${p.fullName} (${p.ageYears}y, ${p.gender})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text("${p.uhid} • ${p.mobile}", fontSize = 11.sp, color = TextSecondary)
                                                }
                                                Text("Use", color = Blue800, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Mandatory & Optional Fields
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Demographics & Referral",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                if (existingUhid != null) {
                                    Surface(color = StatusApprovedBg, shape = RoundedCornerShape(4.dp)) {
                                        Text("Linked: $existingUhid", fontSize = 10.sp, color = StatusApproved, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                                    }
                                } else {
                                    Surface(color = Blue50, shape = RoundedCornerShape(4.dp)) {
                                        Text("Auto New UHID", fontSize = 10.sp, color = Blue800, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = patientName,
                                onValueChange = { viewModel.regPatientName.value = it },
                                label = { Text("Patient Full Name *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = age,
                                    onValueChange = { viewModel.regAge.value = it },
                                    label = { Text("Age (Years) *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text("Gender *", fontSize = 11.sp, color = TextSecondary)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf("Male", "Female", "Other").forEach { g ->
                                            val isSel = gender == g
                                            FilterChip(
                                                selected = isSel,
                                                onClick = { viewModel.regGender.value = g },
                                                label = { Text(g, fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = mobile,
                                onValueChange = { viewModel.regMobile.value = it },
                                label = { Text("Mobile Number (10 Digits) *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = email,
                                onValueChange = { viewModel.regEmail.value = it },
                                label = { Text("Email Address (Optional)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = address,
                                onValueChange = { viewModel.regAddress.value = it },
                                label = { Text("Residential Address (Optional)") },
                                maxLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = doctor,
                                onValueChange = { viewModel.regDoctor.value = it },
                                label = { Text("Referring Doctor") },
                                placeholder = { Text("e.g. Dr. S. K. Banerjee (MD)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = hospital,
                                onValueChange = { viewModel.regHospital.value = it },
                                label = { Text("Hospital / Clinic Name (Optional)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Patient Category
                            Text("Patient Category:", fontSize = 11.sp, color = TextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("General", "Senior Citizen", "Corporate", "BPL").forEach { cat ->
                                    val isSel = category == cat
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { viewModel.regCategory.value = cat },
                                        label = { Text(cat, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // STEP 2: ADD TESTS & RATES
            if (currentStep == 2) {
                // Rate List Selector
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Select Rate List & Priority", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                rateLists.forEach { r ->
                                    val isSel = selectedRateList == r.listName
                                    FilterChip(
                                        selected = isSel,
                                        onClick = {
                                            viewModel.regSelectedRateList.value = r.listName
                                            viewModel.regDiscountRatePercent.value = r.discountPercentage
                                        },
                                        label = { Text("${r.listName} (${r.discountPercentage.toInt()}%)", fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Priority:", fontSize = 11.sp, color = TextSecondary)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf("Routine", "Urgent", "STAT").forEach { p ->
                                            FilterChip(
                                                selected = priority == p,
                                                onClick = { viewModel.regPriority.value = p },
                                                label = { Text(p, fontSize = 10.sp) }
                                            )
                                        }
                                    }
                                }
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text("Collection Location:", fontSize = 11.sp, color = TextSecondary)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf("Lab Centre", "Home Visit").forEach { loc ->
                                            FilterChip(
                                                selected = collectionLocation == loc,
                                                onClick = { viewModel.regCollectionLocation.value = loc },
                                                label = { Text(loc, fontSize = 10.sp) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Search Tests
                item {
                    OutlinedTextField(
                        value = testSearchQuery,
                        onValueChange = { testSearchQuery = it },
                        placeholder = { Text("Search tests by name or code (e.g. CBC, LFT, Lipid)...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Available Tests List
                val filteredTestList = allTests.filter {
                    testSearchQuery.isBlank() ||
                            it.testName.contains(testSearchQuery, ignoreCase = true) ||
                            it.testCode.contains(testSearchQuery, ignoreCase = true) ||
                            it.category.contains(testSearchQuery, ignoreCase = true)
                }

                item {
                    Text(
                        text = "TEST MASTER CATALOG (${filteredTestList.size} available)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(filteredTestList) { test ->
                    val isSelected = selectedTests.any { it.testCode == test.testCode }
                    val finalPrice = test.standardRate * (1.0 - (discountPercent / 100.0))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                if (isSelected) {
                                    viewModel.regSelectedTests.value = selectedTests.filter { it.testCode != test.testCode }
                                } else {
                                    viewModel.regSelectedTests.value = selectedTests + test
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) Blue50 else Color.White),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        viewModel.regSelectedTests.value = selectedTests + test
                                    } else {
                                        viewModel.regSelectedTests.value = selectedTests.filter { it.testCode != test.testCode }
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = test.testName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = "${test.category} • Specimen: ${test.specimenType} • ${test.containerTube}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${"%.0f".format(finalPrice)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Blue800
                                )
                                if (discountPercent > 0) {
                                    Text(
                                        text = "₹${"%.0f".format(test.standardRate)}",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Selected Tests: ${selectedTests.size}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Estimated Subtotal: ₹${"%.0f".format(subtotal)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Blue800
                            )
                        }
                    }
                }
            }

            // STEP 3: BILLING & PAYMENT
            if (currentStep == 3) {
                // Booking Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Booking & Patient Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Patient: $patientName ($age Y / $gender) • Mobile: $mobile", fontSize = 12.sp)
                            Text("Referring Doctor: $doctor", fontSize = 12.sp)
                            Text("Rate List: $selectedRateList • Priority: $priority", fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Selected Tests (${selectedTests.size}):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            selectedTests.forEach { t ->
                                val p = t.standardRate * (1.0 - (discountPercent / 100.0))
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("• ${t.testName}", fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    Text("₹${"%.0f".format(p)}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Billing Calculation Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Financial Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal Amount:", fontSize = 13.sp)
                                Text("₹${"%.2f".format(subtotal)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = customDiscount,
                                    onValueChange = { viewModel.regCustomDiscount.value = it },
                                    label = { Text("Special Discount (₹)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = discountReason,
                                    onValueChange = { viewModel.regDiscountReason.value = it },
                                    label = { Text("Discount Reason") },
                                    placeholder = { Text("e.g. Director waiver") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.3f)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Net Billable Amount:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("₹${"%.2f".format(netAmount)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Blue800)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            // Payment mode
                            Text("Payment Mode:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("CASH", "UPI", "CARD", "NET_BANKING").forEach { mode ->
                                    FilterChip(
                                        selected = paymentMode == mode,
                                        onClick = { viewModel.regPaymentMode.value = mode },
                                        label = { Text(mode, fontSize = 11.sp) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = paidAmount,
                                    onValueChange = { viewModel.regPaidAmount.value = it },
                                    label = { Text("Amount Paid Now (₹)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = transactionRef,
                                    onValueChange = { viewModel.regTransactionRef.value = it },
                                    label = { Text("Txn Ref / UTR (Optional)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.2f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                color = if (dueAmount <= 0.01) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (dueAmount <= 0.01) "Status: Fully Paid" else "Outstanding Due Balance:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dueAmount <= 0.01) Color(0xFF15803D) else Color(0xFFD97706)
                                    )
                                    Text(
                                        text = "₹${"%.2f".format(dueAmount)}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dueAmount <= 0.01) Color(0xFF15803D) else Color(0xFFD97706)
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

@Composable
fun StepBubble(step: Int, currentStep: Int, title: String) {
    val isDone = currentStep > step
    val isCurrent = currentStep == step
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = if (isCurrent) Blue800 else if (isDone) StatusApproved else Color(0xFFE2E8F0),
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isDone) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Text(
                        text = step.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) Color.White else TextSecondary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrent) Blue800 else TextSecondary
        )
    }
}

@Composable
fun StepDivider(isActive: Boolean, modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = 4.dp),
        thickness = 2.dp,
        color = if (isActive) Blue800 else Color(0xFFE2E8F0)
    )
}
