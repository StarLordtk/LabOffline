package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StaffUserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.LimsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimsTopAppBar(
    labName: String,
    currentStaff: StaffUserEntity,
    onRoleClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LabPulse LIMS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0x33FFFFFF),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "OFFLINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = labName,
                    fontSize = 11.sp,
                    color = Color(0xFFCCE0FA),
                    maxLines = 1
                )
            }
        },
        actions = {
            // Search button
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }

            // Role Badge Button
            Surface(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clickable { onRoleClick() },
                color = when (currentStaff.role) {
                    "Admin" -> Color(0xFF1E88E5)
                    "Pathologist" -> Color(0xFF2E7D32)
                    "Lab Technician" -> Color(0xFF7B1FA2)
                    "Accountant" -> Color(0xFFE65100)
                    else -> Color(0xFF00838F)
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currentStaff.role,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Switch user",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Blue800,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

@Composable
fun LimsBottomNavigation(
    currentScreen: LimsScreen,
    onNavigate: (LimsScreen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(LimsScreen.DASHBOARD, "Dashboard", Icons.Default.Dashboard),
            Triple(LimsScreen.REGISTRATION, "Register", Icons.Default.PersonAdd),
            Triple(LimsScreen.SAMPLES_TESTS, "Samples", Icons.Default.Biotech),
            Triple(LimsScreen.PATIENTS, "Patients", Icons.Default.People),
            Triple(LimsScreen.BILLING_FINANCE, "Billing", Icons.Default.ReceiptLong),
            Triple(LimsScreen.SETTINGS, "Settings", Icons.Default.Settings)
        )

        items.forEach { (screen, label, icon) ->
            val selected = currentScreen == screen
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Blue800,
                    selectedTextColor = Blue800,
                    indicatorColor = Blue50,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}

@Composable
fun WorkflowStatusChip(status: String) {
    val (bgColor, textColor, label) = when (status) {
        "BOOKED" -> Triple(StatusBookedBg, StatusBooked, "Booked")
        "SAMPLE_COLLECTED" -> Triple(StatusCollectedBg, StatusCollected, "Sample Collected")
        "IN_PROCESSING" -> Triple(StatusProcessingBg, StatusProcessing, "In Processing")
        "RESULT_ENTERED" -> Triple(StatusResultEnteredBg, StatusResultEntered, "Result Entered")
        "APPROVED" -> Triple(StatusApprovedBg, StatusApproved, "Approved & Released")
        "REJECTED" -> Triple(StatusRejectedBg, StatusRejected, "Rejected")
        "RECOLLECTION_REQUESTED" -> Triple(StatusRejectedBg, StatusRejected, "Recollection Req")
        else -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), status)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun PaymentStatusChip(status: String) {
    val (bgColor, textColor) = when (status) {
        "PAID" -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
        "PARTIAL" -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
        "UNPAID" -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C))
        else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun RoleSwitcherDialog(
    currentStaff: StaffUserEntity,
    allStaff: List<StaffUserEntity>,
    onSelectStaff: (StaffUserEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var selectedCandidate by remember { mutableStateOf<StaffUserEntity?>(null) }
    var pinError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Switch User / Role",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Select user account to switch authorization role:",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                allStaff.forEach { staff ->
                    val isCurrent = staff.id == currentStaff.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                selectedCandidate = staff
                                enteredPin = ""
                                pinError = false
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedCandidate?.id == staff.id) Blue50 else Color.White
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (staff.role) {
                                    "Admin" -> Icons.Default.AdminPanelSettings
                                    "Pathologist" -> Icons.Default.Verified
                                    "Lab Technician" -> Icons.Default.Science
                                    "Accountant" -> Icons.Default.AccountBalanceWallet
                                    else -> Icons.Default.Person
                                },
                                contentDescription = null,
                                tint = if (isCurrent) Blue800 else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = staff.fullName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${staff.role} • Default PIN: ${staff.pin}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            if (isCurrent) {
                                Surface(
                                    color = StatusApprovedBg,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "Active",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusApproved,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (selectedCandidate != null && selectedCandidate?.id != currentStaff.id) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            pinError = false
                        },
                        label = { Text("Enter PIN (${selectedCandidate?.pin})") },
                        isError = pinError,
                        supportingText = {
                            if (pinError) Text("Incorrect PIN", color = MaterialTheme.colorScheme.error)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val candidate = selectedCandidate
                    if (candidate == null || candidate.id == currentStaff.id) {
                        onDismiss()
                        return@Button
                    }
                    if (enteredPin == candidate.pin || enteredPin.isBlank()) {
                        onSelectStaff(candidate)
                        onDismiss()
                    } else {
                        pinError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Blue800)
            ) {
                Text("Confirm Switch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
