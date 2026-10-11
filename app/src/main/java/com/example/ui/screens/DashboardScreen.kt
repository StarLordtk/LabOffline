package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RegistrationEntity
import com.example.ui.components.PaymentStatusChip
import com.example.ui.components.WorkflowStatusChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.DashboardDateFilter
import com.example.ui.viewmodel.LimsScreen
import com.example.ui.viewmodel.LimsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: LimsViewModel,
    onNavigate: (LimsScreen) -> Unit,
    onOpenRegistrationDetail: (RegistrationEntity) -> Unit,
    onOpenResultEntry: (RegistrationEntity) -> Unit
) {
    val registrations by viewModel.registrations.collectAsState()
    val dateFilter by viewModel.dashboardDateFilter.collectAsState()
    val currentStaff by viewModel.currentStaff.collectAsState()
    val labSettings by viewModel.labSettings.collectAsState()

    // Filter registrations based on dateFilter
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfToday = calendar.timeInMillis

    calendar.add(Calendar.DAY_OF_YEAR, -1)
    val startOfYesterday = calendar.timeInMillis
    val endOfYesterday = startOfToday

    calendar.timeInMillis = now
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    val startOfMonth = calendar.timeInMillis

    val filteredRegs = remember(registrations, dateFilter) {
        when (dateFilter) {
            DashboardDateFilter.TODAY -> registrations.filter { it.createdAt >= startOfToday }
            DashboardDateFilter.YESTERDAY -> registrations.filter { it.createdAt in startOfYesterday until endOfYesterday }
            DashboardDateFilter.THIS_MONTH -> registrations.filter { it.createdAt >= startOfMonth }
            DashboardDateFilter.ALL_TIME -> registrations
        }
    }

    // Calculations
    val totalCount = filteredRegs.size
    val pendingSamples = filteredRegs.count { it.overallWorkflowStatus == "BOOKED" }
    val inLabCount = filteredRegs.count { it.overallWorkflowStatus == "IN_PROCESSING" }
    val readyForApproval = filteredRegs.count { it.overallWorkflowStatus == "RESULT_ENTERED" }
    val approvedCount = filteredRegs.count { it.overallWorkflowStatus == "APPROVED" || it.overallWorkflowStatus == "RELEASED" }

    val totalBilled = filteredRegs.sumOf { it.netAmount }
    val totalCollected = filteredRegs.sumOf { it.paidAmount }
    val totalDue = filteredRegs.sumOf { it.dueAmount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Welcome Banner & Date Filter
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Blue800),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Welcome, ${currentStaff.fullName}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Role: ${currentStaff.role} • 100% Offline Mode Active",
                                color = Color(0xFFCCE0FA),
                                fontSize = 12.sp
                            )
                        }
                        Surface(
                            color = Color(0x33FFFFFF),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Date Filters Row
                    Text(
                        text = "Report Period Filter:",
                        color = Color(0xFFCCE0FA),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DashboardDateFilter.values().forEach { filter ->
                            val isSelected = dateFilter == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setDashboardDateFilter(filter) },
                                label = {
                                    Text(
                                        text = filter.label,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Blue800 else Color.White
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color.White,
                                    containerColor = Color(0x22FFFFFF)
                                )
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Actions Row
        item {
            Text(
                text = "QUICK ACTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.PersonAdd,
                    label = "New Booking",
                    containerColor = Blue800,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.resetRegistrationForm()
                        onNavigate(LimsScreen.REGISTRATION)
                    }
                )
                QuickActionButton(
                    icon = Icons.Default.QrCodeScanner,
                    label = "Track Sample",
                    containerColor = Teal700,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.setSamplesStatusFilter("ALL")
                        onNavigate(LimsScreen.SAMPLES_TESTS)
                    }
                )
                QuickActionButton(
                    icon = Icons.Default.EditNote,
                    label = "Result Entry",
                    containerColor = Color(0xFF6A1B9A),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.setSamplesStatusFilter("IN_PROCESSING")
                        onNavigate(LimsScreen.SAMPLES_TESTS)
                    }
                )
                QuickActionButton(
                    icon = Icons.Default.Payments,
                    label = "Collect Due",
                    containerColor = Color(0xFFC62828),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(LimsScreen.BILLING_FINANCE) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Key Metrics Summary Grid (Tappable to drill down!)
        item {
            Text(
                text = "OPERATIONAL METRICS (TAP TO VIEW)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "Registrations",
                    value = totalCount.toString(),
                    subtitle = "Patients registered",
                    icon = Icons.Default.Assignment,
                    color = Blue800,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.setSamplesStatusFilter("ALL")
                        onNavigate(LimsScreen.SAMPLES_TESTS)
                    }
                )
                StatCard(
                    title = "Pending Collection",
                    value = pendingSamples.toString(),
                    subtitle = "Awaiting phlebotomy",
                    icon = Icons.Default.PendingActions,
                    color = StatusCollected,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.setSamplesStatusFilter("BOOKED")
                        onNavigate(LimsScreen.SAMPLES_TESTS)
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(
                    title = "Ready for Review",
                    value = readyForApproval.toString(),
                    subtitle = "Pending sign-off",
                    icon = Icons.Default.VerifiedUser,
                    color = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.setSamplesStatusFilter("RESULT_ENTERED")
                        onNavigate(LimsScreen.SAMPLES_TESTS)
                    }
                )
                StatCard(
                    title = "Approved Reports",
                    value = approvedCount.toString(),
                    subtitle = "Released to patient",
                    icon = Icons.Default.CheckCircle,
                    color = StatusApproved,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.setSamplesStatusFilter("APPROVED")
                        onNavigate(LimsScreen.SAMPLES_TESTS)
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Financial KPI Cards
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(LimsScreen.BILLING_FINANCE) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FINANCIAL SUMMARY (${dateFilter.label.uppercase()})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open finance",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Billing", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "₹${"%.0f".format(totalBilled)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Column {
                            Text(text = "Collections", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "₹${"%.0f".format(totalCollected)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusApproved
                            )
                        }
                        Column {
                            Text(text = "Outstanding Due", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "₹${"%.0f".format(totalDue)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalDue > 0) StatusRejected else TextPrimary
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Clinical Alerts Section
        item {
            if (readyForApproval > 0 || totalDue > 0 || pendingSamples > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = Color(0xFFF57F17),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ACTIONABLE CLINICAL ALERTS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFFE65100)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (readyForApproval > 0) {
                            AlertItem(
                                title = "$readyForApproval Report(s) Awaiting Pathologist Approval",
                                action = "Review & Approve",
                                onClick = {
                                    viewModel.setSamplesStatusFilter("RESULT_ENTERED")
                                    onNavigate(LimsScreen.SAMPLES_TESTS)
                                }
                            )
                        }
                        if (pendingSamples > 0) {
                            AlertItem(
                                title = "$pendingSamples Sample(s) Pending Phlebotomy Collection",
                                action = "Collect Now",
                                onClick = {
                                    viewModel.setSamplesStatusFilter("BOOKED")
                                    onNavigate(LimsScreen.SAMPLES_TESTS)
                                }
                            )
                        }
                        if (totalDue > 0) {
                            AlertItem(
                                title = "Outstanding Patient Balance: ₹${"%.0f".format(totalDue)}",
                                action = "Collect Dues",
                                onClick = { onNavigate(LimsScreen.BILLING_FINANCE) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Recent Registrations Header & List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT REGISTRATIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                TextButton(onClick = {
                    viewModel.setSamplesStatusFilter("ALL")
                    onNavigate(LimsScreen.SAMPLES_TESTS)
                }) {
                    Text("View All", fontSize = 12.sp, color = Blue800, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (filteredRegs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No registrations found for the selected period", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filteredRegs.take(5)) { reg ->
                RegistrationCard(
                    reg = reg,
                    onClick = { onOpenRegistrationDetail(reg) },
                    onEnterResults = { onOpenResultEntry(reg) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(72.dp)
            .clickable { onClick() },
        color = containerColor,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = subtitle, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
fun AlertItem(title: String, action: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "• $title",
            fontSize = 12.sp,
            color = Color(0xFF424242),
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onClick) {
            Text(action, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Blue800)
        }
    }
}

@Composable
fun RegistrationCard(
    reg: RegistrationEntity,
    onClick: () -> Unit,
    onEnterResults: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = reg.patientName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "${reg.patientAge} Yrs / ${reg.patientGender} • ${reg.uhid}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                WorkflowStatusChip(status = reg.overallWorkflowStatus)
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
                    Text(
                        text = "Reg: ${reg.regNumber}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Ref: ${reg.referringDoctor}",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${"%.0f".format(reg.netAmount)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        PaymentStatusChip(status = reg.paymentStatus)
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Details",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
