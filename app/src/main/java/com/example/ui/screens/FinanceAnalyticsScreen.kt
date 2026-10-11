package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.LimsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceAnalyticsScreen(
    viewModel: LimsViewModel,
    onBack: () -> Unit
) {
    val invoices by viewModel.invoices.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val registrations by viewModel.registrations.collectAsState()

    val totalBilled = invoices.sumOf { it.netAmount }
    val advanceCollected = payments.filter { it.paymentType == "ADVANCE" || it.paymentType == "FULL" }.sumOf { it.amount }
    val dueCleared = payments.filter { it.paymentType == "DUE_CLEARANCE" }.sumOf { it.amount }
    val totalRefunds = payments.filter { it.paymentType == "REFUND" }.sumOf { -it.amount }
    val netCollections = (advanceCollected + dueCleared - totalRefunds).coerceAtLeast(0.0)
    val totalOutstanding = invoices.sumOf { it.dueAmount }

    // Breakdown by payment mode
    val modeBreakdown = payments.filter { it.amount > 0 }.groupBy { it.paymentMode }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    // Breakdown by Referring Doctor
    val doctorBreakdown = registrations.groupBy { it.referringDoctor }
        .mapValues { entry -> entry.value.sumOf { it.netAmount } }
        .toList()
        .sortedByDescending { it.second }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Analytics & Revenue Audit", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue800,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
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
            // Revenue Audit Grid
            item {
                Text("REVENUE & COLLECTIONS RECONCILIATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Billed Gross:", fontSize = 13.sp)
                            Text("₹${"%.2f".format(totalBilled)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Initial Advance Collected:", fontSize = 13.sp, color = TextSecondary)
                            Text("₹${"%.2f".format(advanceCollected)}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Due Recovery (Cleared):", fontSize = 13.sp, color = StatusApproved)
                            Text("+ ₹${"%.2f".format(dueCleared)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = StatusApproved)
                        }
                        if (totalRefunds > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Refunds Issued:", fontSize = 13.sp, color = StatusRejected)
                                Text("- ₹${"%.2f".format(totalRefunds)}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = StatusRejected)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Cash Collections:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("₹${"%.2f".format(netCollections)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StatusApproved)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Current Outstanding Receivables:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusRejected)
                            Text("₹${"%.2f".format(totalOutstanding)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusRejected)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Payment Mode Breakdown
            item {
                Text("COLLECTIONS BY PAYMENT CHANNEL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val totalPos = modeBreakdown.values.sum().coerceAtLeast(1.0)
                        listOf("UPI", "CASH", "CARD", "NET_BANKING").forEach { mode ->
                            val amt = modeBreakdown[mode] ?: 0.0
                            val pct = (amt / totalPos)

                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(mode, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("₹${"%.0f".format(amt)} (${"%.1f".format(pct * 100)}%)", fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { pct.toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = when (mode) {
                                        "UPI" -> Color(0xFF00897B)
                                        "CASH" -> Color(0xFF1565C0)
                                        "CARD" -> Color(0xFF6A1B9A)
                                        else -> Color(0xFFE65100)
                                    },
                                    trackColor = Color(0xFFF1F5F9),
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Doctor / Referral Breakdown
            item {
                Text("REVENUE BY REFERRING CLINICIAN / HOSPITAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        doctorBreakdown.take(6).forEach { (doc, rev) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("• $doc", fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text("₹${"%.0f".format(rev)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Blue800)
                            }
                        }
                    }
                }
            }
        }
    }
}
