package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "patients",
    indices = [Index(value = ["uhid"], unique = true), Index(value = ["mobile"]), Index(value = ["fullName"])]
)
data class PatientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uhid: String,
    val fullName: String,
    val ageYears: Int,
    val ageMonths: Int = 0,
    val gender: String, // Male, Female, Other
    val mobile: String,
    val email: String = "",
    val address: String = "",
    val referringDoctor: String = "Self / Direct",
    val hospital: String = "",
    val patientCategory: String = "General", // General, Senior Citizen, Corporate, VIP, BPL
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "staff_users",
    indices = [Index(value = ["username"], unique = true)]
)
data class StaffUserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val fullName: String,
    val role: String, // Admin, Receptionist, Lab Technician, Pathologist, Accountant, Collection Staff
    val pin: String = "1234",
    val isActive: Boolean = true
)

@Entity(
    tableName = "test_master",
    indices = [Index(value = ["testCode"], unique = true)]
)
data class TestMasterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val testCode: String,
    val testName: String,
    val category: String, // Hematology, Biochemistry, Serology, Clinical Pathology, Microbiology, Immunology
    val specimenType: String, // EDTA Whole Blood, Serum, Fluoride Plasma, Random Urine, etc.
    val containerTube: String, // Lavender / EDTA, Red / Plain, Yellow / SST, Grey / Fluoride, Sterile Cup
    val method: String, // Automated Flow Cytometry, Spectrophotometry, ECLIA, Enzymatic, Microscopy
    val defaultTurnaroundHours: Int = 4,
    val standardRate: Double,
    val isPackage: Boolean = false,
    val packageItemCodesJson: String = "",
    val isActive: Boolean = true
)

@Entity(
    tableName = "test_parameters",
    indices = [Index(value = ["testCode", "paramCode"], unique = true)]
)
data class TestParameterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val testCode: String,
    val paramCode: String,
    val paramName: String,
    val unit: String = "",
    val resultType: String = "NUMERIC", // NUMERIC, QUALITATIVE, TEXT
    val normalLowMale: Double? = null,
    val normalHighMale: Double? = null,
    val normalLowFemale: Double? = null,
    val normalHighFemale: Double? = null,
    val normalRangeText: String = "",
    val criticalLow: Double? = null,
    val criticalHigh: Double? = null,
    val sortOrder: Int = 0
)

@Entity(tableName = "rate_lists")
data class RateListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listName: String,
    val discountPercentage: Double = 0.0,
    val isDefault: Boolean = false
)

@Entity(
    tableName = "registrations",
    indices = [
        Index(value = ["regNumber"], unique = true),
        Index(value = ["uhid"]),
        Index(value = ["createdAt"]),
        Index(value = ["overallWorkflowStatus"])
    ]
)
data class RegistrationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val regNumber: String,
    val uhid: String,
    val patientName: String,
    val patientAge: Int,
    val patientGender: String,
    val patientMobile: String,
    val referringDoctor: String,
    val hospital: String,
    val rateListName: String,
    val priority: String = "Routine", // Routine, Urgent, STAT
    val collectionLocation: String = "Lab Centre", // Lab Centre, Home Visit, Collection Centre
    val totalAmount: Double,
    val discountAmount: Double,
    val discountReason: String = "",
    val netAmount: Double,
    val paidAmount: Double,
    val dueAmount: Double,
    val paymentStatus: String = "PAID", // PAID, PARTIAL, UNPAID
    val overallWorkflowStatus: String = "BOOKED", // BOOKED, SAMPLE_COLLECTED, IN_PROCESSING, RESULT_ENTERED, APPROVED, RELEASED, CANCELLED
    val registeredByStaff: String = "Receptionist",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ordered_tests")
data class OrderedTestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val regNumber: String,
    val testCode: String,
    val testName: String,
    val category: String,
    val specimenType: String,
    val priceCharged: Double,
    val workflowStatus: String = "BOOKED", // BOOKED, SAMPLE_COLLECTED, IN_PROCESSING, RESULT_ENTERED, APPROVED, RELEASED
    val sampleBarcode: String
)

@Entity(
    tableName = "samples",
    indices = [Index(value = ["sampleBarcode"], unique = true), Index(value = ["regNumber"])]
)
data class SampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sampleBarcode: String,
    val regNumber: String,
    val patientName: String,
    val specimenType: String,
    val containerTube: String,
    val status: String = "BOOKED", // BOOKED, COLLECTED, IN_LAB, REJECTED, RECOLLECTION_REQUESTED
    val rejectionReason: String = "",
    val collectedAt: Long? = null,
    val collectedBy: String? = null,
    val receivedInLabAt: Long? = null,
    val receivedBy: String? = null,
    val notes: String = ""
)

@Entity(
    tableName = "test_results",
    indices = [Index(value = ["regNumber", "testCode", "paramCode"], unique = true)]
)
data class TestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val regNumber: String,
    val testCode: String,
    val paramCode: String,
    val paramName: String,
    val resultValue: String = "",
    val unit: String = "",
    val referenceIntervalText: String = "",
    val flag: String = "NORMAL", // NORMAL, HIGH, LOW, CRITICAL, ABNORMAL
    val status: String = "DRAFT", // DRAFT, VERIFIED, APPROVED
    val enteredBy: String? = null,
    val enteredAt: Long? = null,
    val approvedBy: String? = null,
    val approvedAt: Long? = null,
    val clinicalRemarks: String = "",
    val version: Int = 1
)

@Entity(
    tableName = "invoices",
    indices = [Index(value = ["invoiceNumber"], unique = true), Index(value = ["regNumber"])]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val regNumber: String,
    val uhid: String,
    val patientName: String,
    val subtotal: Double,
    val discount: Double,
    val discountNote: String = "",
    val netAmount: Double,
    val paidAmount: Double,
    val dueAmount: Double,
    val isCancelled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "payments",
    indices = [Index(value = ["receiptNumber"], unique = true), Index(value = ["regNumber"])]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptNumber: String,
    val invoiceNumber: String,
    val regNumber: String,
    val amount: Double,
    val paymentMode: String = "CASH", // CASH, UPI, CARD, NET_BANKING
    val transactionRef: String = "",
    val paymentType: String = "ADVANCE", // ADVANCE, FULL, DUE_CLEARANCE, REFUND
    val collectedBy: String = "Receptionist",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val entityType: String,
    val entityId: String,
    val performedBy: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "lab_settings")
data class LabSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val labName: String = "Apex Clinical Diagnostics & Pathology",
    val labTagline: String = "Precision In Every Report • NABL Accredited Care",
    val address: String = "14/2 Park Street, Kolkata, West Bengal - 700016",
    val phone: String = "+91 98301 23456 / 033 2289 4500",
    val email: String = "reports@apexdiagnosticlab.in",
    val regNumber: String = "NABL-MC-5421 / WB-HEALTH-2024-9128",
    val pathologistName: String = "Dr. Arindam Sen, MBBS, MD (Pathology)",
    val pathologistDesignation: String = "Chief Consultant Pathologist & Lab Director",
    val currencySymbol: String = "₹",
    val requireApproverPin: Boolean = true
)
