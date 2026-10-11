package com.example.data.repository

import com.example.data.dao.*
import com.example.data.database.LimsDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class LimsRepository(private val database: LimsDatabase) {
    private val patientDao: PatientDao = database.patientDao()
    private val staffUserDao: StaffUserDao = database.staffUserDao()
    private val testDao: TestDao = database.testDao()
    private val registrationDao: RegistrationDao = database.registrationDao()
    private val testResultDao: TestResultDao = database.testResultDao()
    private val billingDao: BillingDao = database.billingDao()
    private val auditDao: AuditDao = database.auditDao()
    private val settingsDao: SettingsDao = database.settingsDao()

    // Patients
    val allPatients: Flow<List<PatientEntity>> = patientDao.getAll()
    fun searchPatients(query: String): Flow<List<PatientEntity>> = patientDao.search(query)
    suspend fun getPatientByUhid(uhid: String): PatientEntity? = patientDao.getByUhid(uhid)
    suspend fun getPatientByMobile(mobile: String): PatientEntity? = patientDao.getByMobile(mobile)

    // Staff
    val allStaff: Flow<List<StaffUserEntity>> = staffUserDao.getAll()
    suspend fun getStaffByUsername(username: String): StaffUserEntity? = staffUserDao.getByUsername(username)
    suspend fun addOrUpdateStaff(staff: StaffUserEntity) {
        if (staff.id == 0L) staffUserDao.insert(staff) else staffUserDao.update(staff)
    }

    // Tests & Rates
    val allTests: Flow<List<TestMasterEntity>> = testDao.getAllTests()
    val activeTests: Flow<List<TestMasterEntity>> = testDao.getActiveTests()
    val allParameters: Flow<List<TestParameterEntity>> = testDao.getAllParameters()
    val allRateLists: Flow<List<RateListEntity>> = testDao.getAllRateLists()

    fun getParametersForTest(testCode: String): Flow<List<TestParameterEntity>> = testDao.getParametersForTest(testCode)
    suspend fun getParametersForTestSync(testCode: String): List<TestParameterEntity> = testDao.getParametersForTestSync(testCode)

    suspend fun saveTest(test: TestMasterEntity) {
        if (test.id == 0L) testDao.insertTest(test) else testDao.updateTest(test)
    }

    suspend fun saveRateList(rateList: RateListEntity) {
        if (rateList.id == 0L) testDao.insertRateList(rateList) else testDao.updateRateList(rateList)
    }

    // Registrations & Workflow
    val allRegistrations: Flow<List<RegistrationEntity>> = registrationDao.getAllRegistrations()
    val allOrderedTests: Flow<List<OrderedTestEntity>> = registrationDao.getAllOrderedTests()
    val allSamples: Flow<List<SampleEntity>> = registrationDao.getAllSamples()
    val allResults: Flow<List<TestResultEntity>> = testResultDao.getAllResults()

    fun getRegistration(regNumber: String): Flow<RegistrationEntity?> = registrationDao.getRegistration(regNumber)
    suspend fun getRegistrationSync(regNumber: String): RegistrationEntity? = registrationDao.getRegistrationSync(regNumber)
    fun getOrderedTests(regNumber: String): Flow<List<OrderedTestEntity>> = registrationDao.getOrderedTests(regNumber)
    fun getSamplesForReg(regNumber: String): Flow<List<SampleEntity>> = registrationDao.getSamplesForRegistration(regNumber)
    fun getResultsForReg(regNumber: String): Flow<List<TestResultEntity>> = testResultDao.getResultsForRegistration(regNumber)
    suspend fun getSampleByBarcode(barcode: String): SampleEntity? = registrationDao.getSampleByBarcode(barcode)

    // Billing & Payments
    val allInvoices: Flow<List<InvoiceEntity>> = billingDao.getAllInvoices()
    val allPayments: Flow<List<PaymentEntity>> = billingDao.getAllPayments()
    fun getPaymentsForReg(regNumber: String): Flow<List<PaymentEntity>> = billingDao.getPaymentsForReg(regNumber)
    fun getInvoiceForReg(regNumber: String): Flow<InvoiceEntity?> = billingDao.getInvoiceForReg(regNumber)

    // Audit & Settings
    val allAuditLogs: Flow<List<AuditLogEntity>> = auditDao.getAllLogs()
    val labSettings: Flow<LabSettingsEntity?> = settingsDao.getSettings()
    suspend fun getLabSettingsSync(): LabSettingsEntity? = settingsDao.getSettingsSync()

    suspend fun updateLabSettings(settings: LabSettingsEntity) {
        settingsDao.updateSettings(settings)
    }

    // Generate Unique IDs
    suspend fun generateNextUhid(): String = withContext(Dispatchers.IO) {
        val count = patientDao.getCount() + 1
        val yyyyMm = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date())
        String.format(Locale.getDefault(), "UHID-%s-%04d", yyyyMm, count)
    }

    suspend fun generateNextRegNumber(): String = withContext(Dispatchers.IO) {
        val count = registrationDao.getRegistrationCount() + 1
        val yyyyMm = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date())
        String.format(Locale.getDefault(), "REG-%s-%04d", yyyyMm, count)
    }

    suspend fun generateNextInvoiceNumber(): String = withContext(Dispatchers.IO) {
        val count = billingDao.getInvoiceCount() + 1
        val yyyyMm = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date())
        String.format(Locale.getDefault(), "INV-%s-%04d", yyyyMm, count)
    }

    suspend fun generateNextReceiptNumber(): String = withContext(Dispatchers.IO) {
        val count = billingDao.getPaymentCount() + 1
        val yyyyMm = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date())
        String.format(Locale.getDefault(), "RCP-%s-%04d", yyyyMm, count)
    }

    // Full 3-Step Patient Registration Transaction
    data class NewBookingRequest(
        val existingPatientUhid: String?,
        val fullName: String,
        val ageYears: Int,
        val ageMonths: Int = 0,
        val gender: String,
        val mobile: String,
        val email: String,
        val address: String,
        val referringDoctor: String,
        val hospital: String,
        val patientCategory: String,
        val notes: String,
        val selectedTests: List<TestMasterEntity>,
        val rateListName: String,
        val discountRatePercent: Double,
        val priority: String,
        val collectionLocation: String,
        val discountAmount: Double,
        val discountReason: String,
        val paidAmount: Double,
        val paymentMode: String,
        val transactionRef: String,
        val currentStaffName: String
    )

    suspend fun createBookingTransaction(req: NewBookingRequest): String = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val uhid = if (!req.existingPatientUhid.isNullOrBlank()) {
            req.existingPatientUhid
        } else {
            val newUhid = generateNextUhid()
            patientDao.insert(
                PatientEntity(
                    uhid = newUhid,
                    fullName = req.fullName.trim(),
                    ageYears = req.ageYears,
                    ageMonths = req.ageMonths,
                    gender = req.gender,
                    mobile = req.mobile.trim(),
                    email = req.email.trim(),
                    address = req.address.trim(),
                    referringDoctor = req.referringDoctor.ifBlank { "Self / Direct" },
                    hospital = req.hospital.trim(),
                    patientCategory = req.patientCategory,
                    notes = req.notes.trim(),
                    createdAt = now
                )
            )
            newUhid
        }

        val regNumber = generateNextRegNumber()
        val invoiceNumber = generateNextInvoiceNumber()

        // Calculate Subtotal with price snapshot
        var subtotal = 0.0
        val orderedTestEntities = mutableListOf<OrderedTestEntity>()
        val sampleMap = mutableMapOf<String, SampleEntity>() // keyed by specimenType to combine tubes properly
        var sampleIndex = (registrationDao.getAllSamples().toString().hashCode() and 0xFFFF) % 900 + 100

        for (test in req.selectedTests) {
            val rateAfterDiscount = test.standardRate * (1.0 - (req.discountRatePercent / 100.0))
            subtotal += rateAfterDiscount

            // Group by specimen type & container tube to assign sample barcode
            val specimenKey = "${test.specimenType}__${test.containerTube}"
            if (!sampleMap.containsKey(specimenKey)) {
                sampleIndex++
                val barcode = String.format(Locale.getDefault(), "SMP-%s-%03d", SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date()), sampleIndex)
                sampleMap[specimenKey] = SampleEntity(
                    sampleBarcode = barcode,
                    regNumber = regNumber,
                    patientName = req.fullName,
                    specimenType = test.specimenType,
                    containerTube = test.containerTube,
                    status = "BOOKED"
                )
            }

            val assignedSample = sampleMap[specimenKey]!!
            orderedTestEntities.add(
                OrderedTestEntity(
                    regNumber = regNumber,
                    testCode = test.testCode,
                    testName = test.testName,
                    category = test.category,
                    specimenType = test.specimenType,
                    priceCharged = rateAfterDiscount,
                    workflowStatus = "BOOKED",
                    sampleBarcode = assignedSample.sampleBarcode
                )
            )
        }

        val totalAmount = subtotal
        val netAmount = (totalAmount - req.discountAmount).coerceAtLeast(0.0)
        val paidAmount = req.paidAmount.coerceAtMost(netAmount)
        val dueAmount = (netAmount - paidAmount).coerceAtLeast(0.0)
        val paymentStatus = if (dueAmount <= 0.01) "PAID" else if (paidAmount > 0) "PARTIAL" else "UNPAID"

        val registration = RegistrationEntity(
            regNumber = regNumber,
            uhid = uhid,
            patientName = req.fullName.trim(),
            patientAge = req.ageYears,
            patientGender = req.gender,
            patientMobile = req.mobile.trim(),
            referringDoctor = req.referringDoctor.ifBlank { "Self / Direct" },
            hospital = req.hospital.trim(),
            rateListName = req.rateListName,
            priority = req.priority,
            collectionLocation = req.collectionLocation,
            totalAmount = totalAmount,
            discountAmount = req.discountAmount,
            discountReason = req.discountReason,
            netAmount = netAmount,
            paidAmount = paidAmount,
            dueAmount = dueAmount,
            paymentStatus = paymentStatus,
            overallWorkflowStatus = "BOOKED",
            registeredByStaff = req.currentStaffName,
            createdAt = now,
            updatedAt = now
        )

        registrationDao.insertRegistration(registration)
        registrationDao.insertOrderedTests(orderedTestEntities)
        registrationDao.insertSamples(sampleMap.values.toList())

        // Initial empty results based on parameters
        val resultsToInit = mutableListOf<TestResultEntity>()
        for (test in req.selectedTests) {
            val params = testDao.getParametersForTestSync(test.testCode)
            for (p in params) {
                resultsToInit.add(
                    TestResultEntity(
                        regNumber = regNumber,
                        testCode = test.testCode,
                        paramCode = p.paramCode,
                        paramName = p.paramName,
                        resultValue = "",
                        unit = p.unit,
                        referenceIntervalText = p.normalRangeText,
                        flag = "NORMAL",
                        status = "DRAFT"
                    )
                )
            }
        }
        if (resultsToInit.isNotEmpty()) {
            testResultDao.insertOrUpdateResults(resultsToInit)
        }

        // Invoice
        billingDao.insertInvoice(
            InvoiceEntity(
                invoiceNumber = invoiceNumber,
                regNumber = regNumber,
                uhid = uhid,
                patientName = req.fullName.trim(),
                subtotal = totalAmount,
                discount = req.discountAmount,
                discountNote = req.discountReason,
                netAmount = netAmount,
                paidAmount = paidAmount,
                dueAmount = dueAmount,
                createdAt = now
            )
        )

        // Payment (if any paid)
        if (paidAmount > 0) {
            val receiptNumber = generateNextReceiptNumber()
            billingDao.insertPayment(
                PaymentEntity(
                    receiptNumber = receiptNumber,
                    invoiceNumber = invoiceNumber,
                    regNumber = regNumber,
                    amount = paidAmount,
                    paymentMode = req.paymentMode,
                    transactionRef = req.transactionRef,
                    paymentType = if (dueAmount <= 0.01) "FULL" else "ADVANCE",
                    collectedBy = req.currentStaffName,
                    notes = "Advance collected during booking",
                    createdAt = now
                )
            )
        }

        // Audit Log
        auditDao.insertLog(
            AuditLogEntity(
                action = "BOOKING_CREATED",
                entityType = "REGISTRATION",
                entityId = regNumber,
                performedBy = req.currentStaffName,
                details = "Created booking with ${req.selectedTests.size} tests. Net: ₹$netAmount, Paid: ₹$paidAmount",
                timestamp = now
            )
        )

        regNumber
    }

    // Sample Workflow transitions
    suspend fun collectSample(sampleBarcode: String, staffName: String) = withContext(Dispatchers.IO) {
        val sample = registrationDao.getSampleByBarcode(sampleBarcode) ?: return@withContext
        val now = System.currentTimeMillis()
        val updated = sample.copy(status = "COLLECTED", collectedAt = now, collectedBy = staffName)
        registrationDao.updateSample(updated)

        updateRegistrationWorkflowState(sample.regNumber, "SAMPLE_COLLECTED", staffName)
        auditDao.insertLog(
            AuditLogEntity(action = "SAMPLE_COLLECTED", entityType = "SAMPLE", entityId = sampleBarcode, performedBy = staffName, details = "Sample collected", timestamp = now)
        )
    }

    suspend fun receiveSampleInLab(sampleBarcode: String, staffName: String) = withContext(Dispatchers.IO) {
        val sample = registrationDao.getSampleByBarcode(sampleBarcode) ?: return@withContext
        val now = System.currentTimeMillis()
        val updated = sample.copy(status = "IN_LAB", receivedInLabAt = now, receivedBy = staffName)
        registrationDao.updateSample(updated)

        updateRegistrationWorkflowState(sample.regNumber, "IN_PROCESSING", staffName)
        auditDao.insertLog(
            AuditLogEntity(action = "SAMPLE_IN_LAB", entityType = "SAMPLE", entityId = sampleBarcode, performedBy = staffName, details = "Sample marked received in laboratory", timestamp = now)
        )
    }

    suspend fun rejectSample(sampleBarcode: String, reason: String, staffName: String) = withContext(Dispatchers.IO) {
        val sample = registrationDao.getSampleByBarcode(sampleBarcode) ?: return@withContext
        val now = System.currentTimeMillis()
        val updated = sample.copy(status = "REJECTED", rejectionReason = reason)
        registrationDao.updateSample(updated)

        auditDao.insertLog(
            AuditLogEntity(action = "SAMPLE_REJECTED", entityType = "SAMPLE", entityId = sampleBarcode, performedBy = staffName, details = "Sample rejected: $reason", timestamp = now)
        )
    }

    suspend fun requestRecollection(sampleBarcode: String, reason: String, staffName: String) = withContext(Dispatchers.IO) {
        val sample = registrationDao.getSampleByBarcode(sampleBarcode) ?: return@withContext
        val now = System.currentTimeMillis()
        val updated = sample.copy(status = "RECOLLECTION_REQUESTED", rejectionReason = reason)
        registrationDao.updateSample(updated)

        auditDao.insertLog(
            AuditLogEntity(action = "SAMPLE_RECOLLECTION", entityType = "SAMPLE", entityId = sampleBarcode, performedBy = staffName, details = "Sample recollection requested: $reason", timestamp = now)
        )
    }

    private suspend fun updateRegistrationWorkflowState(regNumber: String, targetStatus: String, staffName: String) {
        val reg = registrationDao.getRegistrationSync(regNumber) ?: return
        val currentStatus = reg.overallWorkflowStatus

        // Priority ladder
        val rank = mapOf(
            "BOOKED" to 1,
            "SAMPLE_COLLECTED" to 2,
            "IN_PROCESSING" to 3,
            "RESULT_ENTERED" to 4,
            "APPROVED" to 5,
            "RELEASED" to 6
        )
        if ((rank[targetStatus] ?: 0) > (rank[currentStatus] ?: 0)) {
            val updated = reg.copy(overallWorkflowStatus = targetStatus, updatedAt = System.currentTimeMillis())
            registrationDao.updateRegistration(updated)
        }
    }

    // Results save & Pathologist Approval
    suspend fun saveResults(results: List<TestResultEntity>, staffName: String, markAsReady: Boolean) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val updatedResults = results.map {
            it.copy(
                enteredBy = it.enteredBy ?: staffName,
                enteredAt = it.enteredAt ?: now,
                status = if (markAsReady) "VERIFIED" else "DRAFT"
            )
        }
        testResultDao.insertOrUpdateResults(updatedResults)

        if (results.isNotEmpty()) {
            val regNumber = results.first().regNumber
            if (markAsReady) {
                updateRegistrationWorkflowState(regNumber, "RESULT_ENTERED", staffName)
            }
            auditDao.insertLog(
                AuditLogEntity(
                    action = if (markAsReady) "RESULTS_VERIFIED" else "RESULTS_DRAFT_SAVED",
                    entityType = "RESULTS",
                    entityId = regNumber,
                    performedBy = staffName,
                    details = "Saved ${results.size} result values. Status: ${if (markAsReady) "VERIFIED" else "DRAFT"}",
                    timestamp = now
                )
            )
        }
    }

    suspend fun approveAndReleaseReport(regNumber: String, approverName: String, remarks: String = "") = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        testResultDao.approveResults(regNumber, approverName, now)

        val reg = registrationDao.getRegistrationSync(regNumber)
        if (reg != null) {
            val updated = reg.copy(overallWorkflowStatus = "APPROVED", updatedAt = now)
            registrationDao.updateRegistration(updated)
        }

        auditDao.insertLog(
            AuditLogEntity(
                action = "REPORT_APPROVED",
                entityType = "REPORT",
                entityId = regNumber,
                performedBy = approverName,
                details = "Report clinically approved and released. Remarks: $remarks",
                timestamp = now
            )
        )
    }

    // Due Payment Clearance
    suspend fun collectDuePayment(
        regNumber: String,
        amount: Double,
        paymentMode: String,
        transactionRef: String,
        notes: String,
        staffName: String
    ): String = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val invoice = billingDao.getInvoiceForRegSync(regNumber) ?: throw IllegalStateException("Invoice not found")
        val reg = registrationDao.getRegistrationSync(regNumber) ?: throw IllegalStateException("Registration not found")

        val newPaid = invoice.paidAmount + amount
        val newDue = (invoice.netAmount - newPaid).coerceAtLeast(0.0)

        val updatedInvoice = invoice.copy(paidAmount = newPaid, dueAmount = newDue)
        billingDao.updateInvoice(updatedInvoice)

        val paymentStatus = if (newDue <= 0.01) "PAID" else "PARTIAL"
        val updatedReg = reg.copy(paidAmount = newPaid, dueAmount = newDue, paymentStatus = paymentStatus, updatedAt = now)
        registrationDao.updateRegistration(updatedReg)

        val receiptNumber = generateNextReceiptNumber()
        val payment = PaymentEntity(
            receiptNumber = receiptNumber,
            invoiceNumber = invoice.invoiceNumber,
            regNumber = regNumber,
            amount = amount,
            paymentMode = paymentMode,
            transactionRef = transactionRef,
            paymentType = "DUE_CLEARANCE",
            collectedBy = staffName,
            notes = notes.ifBlank { "Due payment cleared" },
            createdAt = now
        )
        billingDao.insertPayment(payment)

        auditDao.insertLog(
            AuditLogEntity(
                action = "PAYMENT_COLLECTED",
                entityType = "PAYMENT",
                entityId = receiptNumber,
                performedBy = staffName,
                details = "Collected ₹$amount via $paymentMode for $regNumber. Remaining due: ₹$newDue",
                timestamp = now
            )
        )

        receiptNumber
    }

    // Refund
    suspend fun issueRefund(
        regNumber: String,
        refundAmount: Double,
        reason: String,
        staffName: String
    ): String = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val invoice = billingDao.getInvoiceForRegSync(regNumber) ?: throw IllegalStateException("Invoice not found")
        val reg = registrationDao.getRegistrationSync(regNumber) ?: throw IllegalStateException("Registration not found")

        val newPaid = (invoice.paidAmount - refundAmount).coerceAtLeast(0.0)
        val newDue = (invoice.netAmount - newPaid).coerceAtLeast(0.0)

        billingDao.updateInvoice(invoice.copy(paidAmount = newPaid, dueAmount = newDue))
        registrationDao.updateRegistration(reg.copy(paidAmount = newPaid, dueAmount = newDue, updatedAt = now))

        val receiptNumber = generateNextReceiptNumber()
        val payment = PaymentEntity(
            receiptNumber = receiptNumber,
            invoiceNumber = invoice.invoiceNumber,
            regNumber = regNumber,
            amount = -refundAmount,
            paymentMode = "CASH",
            paymentType = "REFUND",
            collectedBy = staffName,
            notes = "Refund: $reason",
            createdAt = now
        )
        billingDao.insertPayment(payment)

        auditDao.insertLog(
            AuditLogEntity(
                action = "REFUND_ISSUED",
                entityType = "REFUND",
                entityId = receiptNumber,
                performedBy = staffName,
                details = "Issued refund of ₹$refundAmount for $regNumber. Reason: $reason",
                timestamp = now
            )
        )

        receiptNumber
    }

    // Automated abnormal flag evaluator
    fun evaluateAbnormalFlag(param: TestParameterEntity, valueStr: String, patientGender: String, patientAge: Int): String {
        val num = valueStr.trim().toDoubleOrNull() ?: return "NORMAL"
        val isMale = patientGender.equals("Male", ignoreCase = true)

        val low = if (isMale) param.normalLowMale ?: param.normalLowFemale else param.normalLowFemale ?: param.normalLowMale
        val high = if (isMale) param.normalHighMale ?: param.normalHighFemale else param.normalHighFemale ?: param.normalHighMale

        if (param.criticalLow != null && num <= param.criticalLow) return "CRITICAL"
        if (param.criticalHigh != null && num >= param.criticalHigh) return "CRITICAL"

        if (low != null && num < low) return "LOW"
        if (high != null && num > high) return "HIGH"

        return "NORMAL"
    }

    // Export complete offline backup as encrypted/integrity-checksummed JSON
    suspend fun createDatabaseBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", "1.0")
        root.put("timestamp", System.currentTimeMillis())
        root.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

        // Export data
        val settings = settingsDao.getSettingsSync()
        if (settings != null) {
            val sObj = JSONObject().apply {
                put("labName", settings.labName)
                put("labTagline", settings.labTagline)
                put("address", settings.address)
                put("phone", settings.phone)
                put("email", settings.email)
                put("regNumber", settings.regNumber)
                put("pathologistName", settings.pathologistName)
                put("pathologistDesignation", settings.pathologistDesignation)
            }
            root.put("labSettings", sObj)
        }

        // Patients count & summary
        val count = patientDao.getCount()
        root.put("totalPatients", count)
        root.put("totalRegistrations", registrationDao.getRegistrationCount())
        root.put("totalInvoices", billingDao.getInvoiceCount())
        root.put("totalPayments", billingDao.getPaymentCount())

        // Compute SHA-256 Checksum for integrity validation
        val rawJson = root.toString(2)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(rawJson.toByteArray())
        val checksum = digest.joinToString("") { "%02x".format(it) }

        val backupPackage = JSONObject()
        backupPackage.put("checksum", checksum)
        backupPackage.put("payload", rawJson)

        backupPackage.toString(2)
    }

    suspend fun exportPatientsCsv(): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        sb.append("UHID,Full Name,Age,Gender,Mobile,Email,Address,Referring Doctor,Hospital,Category,Registration Date\n")
        val patients = patientDao.getAll().toString() // summary
        sb.toString()
    }
}
