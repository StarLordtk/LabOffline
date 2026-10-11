package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.LimsApplication
import com.example.data.model.*
import com.example.data.repository.LimsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

enum class LimsScreen {
    DASHBOARD,
    REGISTRATION,
    SAMPLES_TESTS,
    PATIENTS,
    BILLING_FINANCE,
    SETTINGS
}

enum class DashboardDateFilter(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time")
}

class LimsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LimsRepository = (application as LimsApplication).repository

    // Current Screen
    private val _currentScreen = MutableStateFlow(LimsScreen.DASHBOARD)
    val currentScreen: StateFlow<LimsScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: LimsScreen) {
        _currentScreen.value = screen
    }

    // Role-based User Session
    private val _currentStaff = MutableStateFlow(
        StaffUserEntity(username = "admin", fullName = "Dr. Rajesh Sharma", role = "Admin", pin = "1234")
    )
    val currentStaff: StateFlow<StaffUserEntity> = _currentStaff.asStateFlow()

    fun switchStaff(staff: StaffUserEntity) {
        _currentStaff.value = staff
    }

    // Dashboard Filter
    private val _dashboardDateFilter = MutableStateFlow(DashboardDateFilter.ALL_TIME)
    val dashboardDateFilter: StateFlow<DashboardDateFilter> = _dashboardDateFilter.asStateFlow()

    fun setDashboardDateFilter(filter: DashboardDateFilter) {
        _dashboardDateFilter.value = filter
    }

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Status filter for Samples & Tests
    private val _samplesStatusFilter = MutableStateFlow("ALL")
    val samplesStatusFilter: StateFlow<String> = _samplesStatusFilter.asStateFlow()

    fun setSamplesStatusFilter(status: String) {
        _samplesStatusFilter.value = status
    }

    // UI Feedback Message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    // Reactive Data Flows
    val registrations: StateFlow<List<RegistrationEntity>> = repository.allRegistrations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val patients: StateFlow<List<PatientEntity>> = repository.allPatients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tests: StateFlow<List<TestMasterEntity>> = repository.allTests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTests: StateFlow<List<TestMasterEntity>> = repository.activeTests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rateLists: StateFlow<List<RateListEntity>> = repository.allRateLists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val samples: StateFlow<List<SampleEntity>> = repository.allSamples
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orderedTests: StateFlow<List<OrderedTestEntity>> = repository.allOrderedTests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<InvoiceEntity>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val labSettings: StateFlow<LabSettingsEntity?> = repository.labSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val staffList: StateFlow<List<StaffUserEntity>> = repository.allStaff
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Selection for Result Entry / Detailed Report
    private val _selectedRegistrationForResults = MutableStateFlow<RegistrationEntity?>(null)
    val selectedRegistrationForResults: StateFlow<RegistrationEntity?> = _selectedRegistrationForResults.asStateFlow()

    fun selectRegistrationForResults(reg: RegistrationEntity?) {
        _selectedRegistrationForResults.value = reg
    }

    private val _selectedRegistrationForReport = MutableStateFlow<RegistrationEntity?>(null)
    val selectedRegistrationForReport: StateFlow<RegistrationEntity?> = _selectedRegistrationForReport.asStateFlow()

    fun selectRegistrationForReport(reg: RegistrationEntity?) {
        _selectedRegistrationForReport.value = reg
    }

    // Results Flow for currently selected registration
    fun getResultsForRegistration(regNumber: String): Flow<List<TestResultEntity>> {
        return repository.getResultsForReg(regNumber)
    }

    fun getOrderedTestsForRegistration(regNumber: String): Flow<List<OrderedTestEntity>> {
        return repository.getOrderedTests(regNumber)
    }

    fun getSamplesForRegistration(regNumber: String): Flow<List<SampleEntity>> {
        return repository.getSamplesForReg(regNumber)
    }

    fun getPaymentsForRegistration(regNumber: String): Flow<List<PaymentEntity>> {
        return repository.getPaymentsForReg(regNumber)
    }

    // 3-Step Registration Form State
    var regStep = MutableStateFlow(1)
    var regPatientName = MutableStateFlow("")
    var regAge = MutableStateFlow("")
    var regGender = MutableStateFlow("Male")
    var regMobile = MutableStateFlow("")
    var regEmail = MutableStateFlow("")
    var regAddress = MutableStateFlow("")
    var regDoctor = MutableStateFlow("Dr. S. K. Banerjee (MD)")
    var regHospital = MutableStateFlow("Apollo Clinic")
    var regCategory = MutableStateFlow("General")
    var regNotes = MutableStateFlow("")
    var regExistingUhid = MutableStateFlow<String?>(null)

    var regSelectedTests = MutableStateFlow<List<TestMasterEntity>>(emptyList())
    var regSelectedRateList = MutableStateFlow("Main / Standard")
    var regDiscountRatePercent = MutableStateFlow(0.0)
    var regPriority = MutableStateFlow("Routine")
    var regCollectionLocation = MutableStateFlow("Lab Centre")

    var regCustomDiscount = MutableStateFlow("0")
    var regDiscountReason = MutableStateFlow("")
    var regPaidAmount = MutableStateFlow("")
    var regPaymentMode = MutableStateFlow("CASH")
    var regTransactionRef = MutableStateFlow("")

    fun resetRegistrationForm() {
        regStep.value = 1
        regPatientName.value = ""
        regAge.value = ""
        regGender.value = "Male"
        regMobile.value = ""
        regEmail.value = ""
        regAddress.value = ""
        regDoctor.value = "Dr. S. K. Banerjee (MD)"
        regHospital.value = ""
        regCategory.value = "General"
        regNotes.value = ""
        regExistingUhid.value = null
        regSelectedTests.value = emptyList()
        regSelectedRateList.value = "Main / Standard"
        regDiscountRatePercent.value = 0.0
        regPriority.value = "Routine"
        regCollectionLocation.value = "Lab Centre"
        regCustomDiscount.value = "0"
        regDiscountReason.value = ""
        regPaidAmount.value = ""
        regPaymentMode.value = "CASH"
        regTransactionRef.value = ""
    }

    fun autoFillExistingPatient(patient: PatientEntity) {
        regExistingUhid.value = patient.uhid
        regPatientName.value = patient.fullName
        regAge.value = patient.ageYears.toString()
        regGender.value = patient.gender
        regMobile.value = patient.mobile
        regEmail.value = patient.email
        regAddress.value = patient.address
        regDoctor.value = patient.referringDoctor
        regHospital.value = patient.hospital
        regCategory.value = patient.patientCategory
    }

    fun submitBooking(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val ageInt = regAge.value.toIntOrNull() ?: 30
                val discountDbl = regCustomDiscount.value.toDoubleOrNull() ?: 0.0
                val paidDbl = regPaidAmount.value.toDoubleOrNull() ?: 0.0

                if (regPatientName.value.isBlank()) {
                    onError("Patient name is required")
                    return@launch
                }
                if (regMobile.value.isBlank()) {
                    onError("Mobile number is required")
                    return@launch
                }
                if (regSelectedTests.value.isEmpty()) {
                    onError("Please select at least one test")
                    return@launch
                }

                val req = LimsRepository.NewBookingRequest(
                    existingPatientUhid = regExistingUhid.value,
                    fullName = regPatientName.value,
                    ageYears = ageInt,
                    gender = regGender.value,
                    mobile = regMobile.value,
                    email = regEmail.value,
                    address = regAddress.value,
                    referringDoctor = regDoctor.value,
                    hospital = regHospital.value,
                    patientCategory = regCategory.value,
                    notes = regNotes.value,
                    selectedTests = regSelectedTests.value,
                    rateListName = regSelectedRateList.value,
                    discountRatePercent = regDiscountRatePercent.value,
                    priority = regPriority.value,
                    collectionLocation = regCollectionLocation.value,
                    discountAmount = discountDbl,
                    discountReason = regDiscountReason.value,
                    paidAmount = paidDbl,
                    paymentMode = regPaymentMode.value,
                    transactionRef = regTransactionRef.value,
                    currentStaffName = _currentStaff.value.fullName
                )

                val regNumber = repository.createBookingTransaction(req)
                resetRegistrationForm()
                showSnackbar("Booking saved successfully: $regNumber")
                onSuccess(regNumber)
            } catch (e: Exception) {
                onError("Failed to save booking: ${e.message}")
            }
        }
    }

    // Sample Actions
    fun markSampleCollected(barcode: String) {
        viewModelScope.launch {
            repository.collectSample(barcode, _currentStaff.value.fullName)
            showSnackbar("Sample $barcode marked collected")
        }
    }

    fun markSampleInLab(barcode: String) {
        viewModelScope.launch {
            repository.receiveSampleInLab(barcode, _currentStaff.value.fullName)
            showSnackbar("Sample $barcode marked received in laboratory")
        }
    }

    fun rejectSample(barcode: String, reason: String) {
        viewModelScope.launch {
            repository.rejectSample(barcode, reason, _currentStaff.value.fullName)
            showSnackbar("Sample $barcode marked rejected: $reason")
        }
    }

    fun requestSampleRecollection(barcode: String, reason: String) {
        viewModelScope.launch {
            repository.requestRecollection(barcode, reason, _currentStaff.value.fullName)
            showSnackbar("Recollection requested for $barcode: $reason")
        }
    }

    // Save Results
    fun saveTestResults(results: List<TestResultEntity>, markAsReady: Boolean) {
        viewModelScope.launch {
            repository.saveResults(results, _currentStaff.value.fullName, markAsReady)
            showSnackbar(if (markAsReady) "Results verified and ready for approval" else "Results saved as draft")
        }
    }

    // Pathologist Approval
    fun approveReport(regNumber: String, remarks: String = "", onResult: (Boolean, String) -> Unit) {
        val staff = _currentStaff.value
        if (staff.role != "Pathologist" && staff.role != "Admin") {
            onResult(false, "Approval requires Pathologist or Admin role. Currently logged in as: ${staff.role}")
            return
        }
        viewModelScope.launch {
            try {
                repository.approveAndReleaseReport(regNumber, staff.fullName, remarks)
                showSnackbar("Report approved and released by ${staff.fullName}")
                onResult(true, "Report approved successfully")
            } catch (e: Exception) {
                onResult(false, "Approval failed: ${e.message}")
            }
        }
    }

    // Due Payment Clearance
    fun clearDuePayment(
        regNumber: String,
        amount: Double,
        mode: String,
        ref: String,
        notes: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val receipt = repository.collectDuePayment(regNumber, amount, mode, ref, notes, _currentStaff.value.fullName)
                showSnackbar("Due payment cleared! Receipt: $receipt")
                onSuccess(receipt)
            } catch (e: Exception) {
                onError("Failed to collect payment: ${e.message}")
            }
        }
    }

    // Issue Refund
    fun issueRefund(
        regNumber: String,
        amount: Double,
        reason: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val staff = _currentStaff.value
        if (staff.role != "Admin" && staff.role != "Accountant") {
            onError("Refund requires Admin or Accountant role.")
            return
        }
        viewModelScope.launch {
            try {
                val receipt = repository.issueRefund(regNumber, amount, reason, staff.fullName)
                showSnackbar("Refund issued! Voucher: $receipt")
                onSuccess(receipt)
            } catch (e: Exception) {
                onError("Failed to issue refund: ${e.message}")
            }
        }
    }

    // Test Master management
    fun saveTestMaster(test: TestMasterEntity) {
        val staff = _currentStaff.value
        if (staff.role != "Admin") {
            showSnackbar("Only Admin can edit Test Master rates")
            return
        }
        viewModelScope.launch {
            repository.saveTest(test)
            showSnackbar("Test '${test.testName}' saved")
        }
    }

    // Settings update
    fun updateLabSettings(settings: LabSettingsEntity) {
        viewModelScope.launch {
            repository.updateLabSettings(settings)
            showSnackbar("Lab profile updated successfully")
        }
    }

    // Backup & Restore
    fun generateBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.createDatabaseBackupJson()
            onResult(json)
        }
    }
}
