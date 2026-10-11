package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvoiceEntity
import com.example.data.model.PaymentEntity
import com.example.data.model.RegistrationEntity
import com.example.ui.components.PaymentStatusChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.LimsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    viewModel: LimsViewModel,
    onOpenAnalytics: () -> Unit
) {
    val invoices by viewModel.invoices.collectAsState()
    val allPayments by viewModel.payments.collectAsState()
    val registrations by viewModel.registrations.collectAsState()
    val currentStaff by viewModel.currentStaff.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "DUE", "PAID"
    var selectedInvoiceForDue by remember { mutableStateOf<InvoiceEntity?>(null) }
    var selectedInvoiceForReceipt by remember { mutableStateOf<InvoiceEntity?>(null) }
    var selectedInvoiceForRefund by remember { mutableStateOf<InvoiceEntity?>(null) }

    // Due clearance form state
    var dueAmountInput by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("UPI") }
    var transactionRef by remember { mutableStateOf("") }
    var paymentNotes by remember { mutableStateOf("") }

    // Refund form state
    var refundAmountInput by remember { mutableStateOf("") }
    var refundReason by remember { mutableStateOf("") }

    val filteredInvoices = invoices.filter { inv ->
        val matchesSearch = searchQuery.isBlank() ||
                inv.patientName.contains(searchQuery, ignoreCase = true) ||
                inv.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                inv.regNumber.contains(searchQuery, ignoreCase = true) ||
                inv.uhid.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "DUE" -> inv.dueAmount > 0.01
            "PAID" -> inv.dueAmount <= 0.01
            else -> true
        }
        matchesSearch && matchesFilter
    }

    val totalBilled = invoices.sumOf { it.netAmount }
    val totalCollected = invoices.sumOf { it.paidAmount }
    val totalOutstanding = invoices.sumOf { it.dueAmount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Top Header & Financial Summary
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
                            text = "Billing & Financial Collections",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Blue800
                        )
                        Text(
                            text = "Invoices • Receipts • Due Balances • Refunds",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenAnalytics,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Analytics", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mini KPI Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Total Billed", fontSize = 10.sp, color = TextSecondary)
                            Text("₹${"%.0f".format(totalBilled)}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Collected", fontSize = 10.sp, color = Color(0xFF15803D))
                            Text("₹${"%.0f".format(totalCollected)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Pending Due", fontSize = 10.sp, color = Color(0xFFB91C1C))
                            Text("₹${"%.0f".format(totalOutstanding)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by invoice, reg no, or patient name...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter tabs
                Row {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All Invoices (${invoices.size})", fontSize = 11.sp) }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = selectedFilter == "DUE",
                        onClick = { selectedFilter = "DUE" },
                        label = { Text("Outstanding Dues (${invoices.count { it.dueAmount > 0.01 }})", fontSize = 11.sp) }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = selectedFilter == "PAID",
                        onClick = { selectedFilter = "PAID" },
                        label = { Text("Cleared (${invoices.count { it.dueAmount <= 0.01 }})", fontSize = 11.sp) }
                    )
                }
            }
        }

        // Invoices List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            if (filteredInvoices.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No invoices found", color = TextSecondary)
                    }
                }
            } else {
                items(filteredInvoices) { inv ->
                    val hasDue = inv.dueAmount > 0.01
                    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(inv.createdAt))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
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
                                    Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Blue800)
                                    Text("${inv.patientName} • ${inv.uhid}", fontSize = 12.sp, color = TextPrimary)
                                }
                                PaymentStatusChip(status = if (hasDue) "PARTIAL / DUE" else "PAID")
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Reg: ${inv.regNumber} • Date: $dateStr",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Net Bill: ₹${"%.2f".format(inv.netAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Paid: ₹${"%.2f".format(inv.paidAmount)}", fontSize = 11.sp, color = StatusApproved)
                                    if (hasDue) {
                                        Text("Balance Due: ₹${"%.2f".format(inv.dueAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusRejected)
                                    }
                                }

                                Row {
                                    // Collect Due button
                                    if (hasDue) {
                                        Button(
                                            onClick = {
                                                selectedInvoiceForDue = inv
                                                dueAmountInput = "%.0f".format(inv.dueAmount)
                                                paymentMode = "UPI"
                                                transactionRef = ""
                                                paymentNotes = ""
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusApproved),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Clear Due", fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    // View Receipts
                                    OutlinedButton(
                                        onClick = { selectedInvoiceForReceipt = inv },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Receipt", fontSize = 11.sp)
                                    }

                                    // Refund button (Admin / Accountant)
                                    if (inv.paidAmount > 0 && (currentStaff.role == "Admin" || currentStaff.role == "Accountant")) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = {
                                                selectedInvoiceForRefund = inv
                                                refundAmountInput = "%.0f".format(inv.paidAmount)
                                                refundReason = ""
                                            },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.CurrencyExchange, contentDescription = "Refund", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Collect Due Balance Dialog
    if (selectedInvoiceForDue != null) {
        val inv = selectedInvoiceForDue!!
        AlertDialog(
            onDismissRequest = { selectedInvoiceForDue = null },
            title = {
                Text("Collect Outstanding Balance Due", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column {
                    Text("Patient: ${inv.patientName} (${inv.regNumber})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Total Net: ₹${"%.2f".format(inv.netAmount)} • Paid so far: ₹${"%.2f".format(inv.paidAmount)}", fontSize = 12.sp, color = TextSecondary)
                    Text("Outstanding Balance: ₹${"%.2f".format(inv.dueAmount)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = StatusRejected)

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = dueAmountInput,
                        onValueChange = { dueAmountInput = it },
                        label = { Text("Amount Collecting (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Payment Mode:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("CASH", "UPI", "CARD", "NET_BANKING").forEach { mode ->
                            FilterChip(
                                selected = paymentMode == mode,
                                onClick = { paymentMode = mode },
                                label = { Text(mode, fontSize = 10.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = transactionRef,
                        onValueChange = { transactionRef = it },
                        label = { Text("Txn Ref / UTR (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = paymentNotes,
                        onValueChange = { paymentNotes = it },
                        label = { Text("Notes / Comments") },
                        placeholder = { Text("Balance cleared upon report handover") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = dueAmountInput.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.clearDuePayment(
                                regNumber = inv.regNumber,
                                amount = amt,
                                mode = paymentMode,
                                ref = transactionRef,
                                notes = paymentNotes,
                                onSuccess = { receiptNo ->
                                    selectedInvoiceForDue = null
                                },
                                onError = { err ->
                                    viewModel.showSnackbar(err)
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusApproved)
                ) {
                    Text("Confirm Collection")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedInvoiceForDue = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Thermal / A4 Receipt Preview Dialog
    if (selectedInvoiceForReceipt != null) {
        val inv = selectedInvoiceForReceipt!!
        val payments = allPayments.filter { it.regNumber == inv.regNumber }
        var isCompactThermal by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { selectedInvoiceForReceipt = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isCompactThermal) "Compact Thermal Receipt" else "Standard Invoice Receipt",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    FilterChip(
                        selected = isCompactThermal,
                        onClick = { isCompactThermal = !isCompactThermal },
                        label = { Text(if (isCompactThermal) "A4 View" else "Thermal", fontSize = 10.sp) }
                    )
                }
            },
            text = {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (isCompactThermal) Color(0xFFFAFAFA) else Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = if (isCompactThermal) Alignment.CenterHorizontally else Alignment.Start
                    ) {
                        Text("Apex Clinical Diagnostics", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Blue800)
                        Text("Park Street, Kolkata • Phone: +91 98301 23456", fontSize = 10.sp, color = TextSecondary)
                        Text("GSTIN: 19AAACC1234F1Z5 • NABL-MC-5421", fontSize = 9.sp, color = TextSecondary)

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Invoice: ${inv.invoiceNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("UHID: ${inv.uhid}", fontSize = 11.sp)
                            }
                            Text("Patient: ${inv.patientName}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Reg Number: ${inv.regNumber}", fontSize = 10.sp, color = TextSecondary)
                            val dt = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault()).format(Date(inv.createdAt))
                            Text("Date: $dt", fontSize = 10.sp, color = TextSecondary)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal Amount:", fontSize = 11.sp)
                            Text("₹${"%.2f".format(inv.subtotal)}", fontSize = 11.sp)
                        }
                        if (inv.discount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Discount: (${inv.discountNote})", fontSize = 11.sp, color = StatusApproved)
                                Text("- ₹${"%.2f".format(inv.discount)}", fontSize = 11.sp, color = StatusApproved)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Billed:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("₹${"%.2f".format(inv.netAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusApproved)
                            Text("₹${"%.2f".format(inv.paidAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusApproved)
                        }
                        if (inv.dueAmount > 0.01) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Outstanding Balance:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusRejected)
                                Text("₹${"%.2f".format(inv.dueAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusRejected)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Payment Transactions (${payments.size}):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        payments.forEach { p ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${p.receiptNumber} (${p.paymentMode})", fontSize = 10.sp, color = TextSecondary)
                                Text("₹${"%.2f".format(p.amount)}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Thank you for choosing Apex Diagnostics!", fontSize = 10.sp, color = TextSecondary)
                        Text("Computer Generated Receipt • Valid Without Seal", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.showSnackbar("Receipt printed via Android printing service")
                        selectedInvoiceForReceipt = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue800)
                ) {
                    Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print Receipt")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedInvoiceForReceipt = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Refund Dialog (Admin / Accountant)
    if (selectedInvoiceForRefund != null) {
        val inv = selectedInvoiceForRefund!!
        AlertDialog(
            onDismissRequest = { selectedInvoiceForRefund = null },
            title = {
                Text("Issue Financial Refund", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = StatusRejected)
            },
            text = {
                Column {
                    Text("Patient: ${inv.patientName} (${inv.regNumber})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Total Paid Amount: ₹${"%.2f".format(inv.paidAmount)}", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = refundAmountInput,
                        onValueChange = { refundAmountInput = it },
                        label = { Text("Refund Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = refundReason,
                        onValueChange = { refundReason = it },
                        label = { Text("Refund Reason (Required)") },
                        placeholder = { Text("e.g. Test cancelled by clinician / sample rejected") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val refAmt = refundAmountInput.toDoubleOrNull() ?: 0.0
                        if (refAmt > 0 && refundReason.isNotBlank()) {
                            viewModel.issueRefund(
                                regNumber = inv.regNumber,
                                amount = refAmt,
                                reason = refundReason,
                                onSuccess = { receipt ->
                                    selectedInvoiceForRefund = null
                                },
                                onError = { err ->
                                    viewModel.showSnackbar(err)
                                }
                            )
                        } else {
                            viewModel.showSnackbar("Please enter refund amount and valid reason")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRejected)
                ) {
                    Text("Issue Refund")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedInvoiceForRefund = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
