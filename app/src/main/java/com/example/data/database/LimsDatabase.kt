package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PatientEntity::class,
        StaffUserEntity::class,
        TestMasterEntity::class,
        TestParameterEntity::class,
        RateListEntity::class,
        RegistrationEntity::class,
        OrderedTestEntity::class,
        SampleEntity::class,
        TestResultEntity::class,
        InvoiceEntity::class,
        PaymentEntity::class,
        AuditLogEntity::class,
        LabSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LimsDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun staffUserDao(): StaffUserDao
    abstract fun testDao(): TestDao
    abstract fun registrationDao(): RegistrationDao
    abstract fun testResultDao(): TestResultDao
    abstract fun billingDao(): BillingDao
    abstract fun auditDao(): AuditDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: LimsDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): LimsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LimsDatabase::class.java,
                    "lims_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(LimsDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class LimsDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: LimsDatabase) {
            val settingsDao = database.settingsDao()
            val staffDao = database.staffUserDao()
            val testDao = database.testDao()
            val patientDao = database.patientDao()
            val regDao = database.registrationDao()
            val billingDao = database.billingDao()
            val resultDao = database.testResultDao()
            val auditDao = database.auditDao()

            // 1. Lab Settings
            settingsDao.insertSettings(
                LabSettingsEntity(
                    id = 1,
                    labName = "Apex Clinical Diagnostics & Pathology",
                    labTagline = "Accredited Diagnostics • ISO 15189 Compliant • Accurate & Fast",
                    address = "14/2 Park Street, 2nd Floor, Kolkata, WB - 700016",
                    phone = "+91 98301 23456 / 033 2289 4500",
                    email = "reports@apexdiagnosticlab.in",
                    regNumber = "NABL-MC-5421 / WB-HEALTH-2024-9128",
                    pathologistName = "Dr. Arindam Sen, MBBS, MD (Pathology)",
                    pathologistDesignation = "Chief Consultant Pathologist & Lab Director",
                    currencySymbol = "₹",
                    requireApproverPin = true
                )
            )

            // 2. Staff Users
            val staffList = listOf(
                StaffUserEntity(username = "admin", fullName = "Dr. Rajesh Sharma (Lab Director)", role = "Admin", pin = "1234"),
                StaffUserEntity(username = "pathologist", fullName = "Dr. Arindam Sen, MD (Pathologist)", role = "Pathologist", pin = "3333"),
                StaffUserEntity(username = "reception", fullName = "Pooja Verma (Reception)", role = "Receptionist", pin = "1111"),
                StaffUserEntity(username = "tech", fullName = "Bikram Das (Senior Lab Tech)", role = "Lab Technician", pin = "2222"),
                StaffUserEntity(username = "accounts", fullName = "Suresh Iyer (Billing Head)", role = "Accountant", pin = "4444"),
                StaffUserEntity(username = "collector", fullName = "Amit Roy (Phlebotomist)", role = "Collection Staff", pin = "5555")
            )
            staffList.forEach { staffDao.insert(it) }

            // 3. Rate Lists
            val rateLists = listOf(
                RateListEntity(listName = "Main / Standard", discountPercentage = 0.0, isDefault = true),
                RateListEntity(listName = "Doctor Referral", discountPercentage = 10.0, isDefault = false),
                RateListEntity(listName = "Hospital IPD/OPD", discountPercentage = 15.0, isDefault = false),
                RateListEntity(listName = "Senior Citizen", discountPercentage = 10.0, isDefault = false),
                RateListEntity(listName = "Camp / BPL Scheme", discountPercentage = 25.0, isDefault = false)
            )
            testDao.insertRateLists(rateLists)

            // 4. Test Master
            val tests = listOf(
                TestMasterEntity(
                    testCode = "CBC",
                    testName = "Complete Blood Count (CBC with ESR)",
                    category = "Hematology",
                    specimenType = "EDTA Whole Blood",
                    containerTube = "Lavender / EDTA Tube",
                    method = "Automated 5-Part Differential Flow Cytometry",
                    defaultTurnaroundHours = 4,
                    standardRate = 350.0
                ),
                TestMasterEntity(
                    testCode = "LFT",
                    testName = "Liver Function Test (LFT)",
                    category = "Biochemistry",
                    specimenType = "Serum",
                    containerTube = "Yellow / SST Gel Tube",
                    method = "Spectrophotometry / Photometric Enzymatic",
                    defaultTurnaroundHours = 6,
                    standardRate = 750.0
                ),
                TestMasterEntity(
                    testCode = "KFT",
                    testName = "Kidney / Renal Function Test (KFT)",
                    category = "Biochemistry",
                    specimenType = "Serum",
                    containerTube = "Yellow / SST Gel Tube",
                    method = "Urease / Modified Jaffe Rate",
                    defaultTurnaroundHours = 6,
                    standardRate = 650.0
                ),
                TestMasterEntity(
                    testCode = "LIPID",
                    testName = "Lipid Profile (Fasting)",
                    category = "Biochemistry",
                    specimenType = "Serum (Fasting 12h)",
                    containerTube = "Yellow / SST Gel Tube",
                    method = "Enzymatic Colorimetric",
                    defaultTurnaroundHours = 6,
                    standardRate = 800.0
                ),
                TestMasterEntity(
                    testCode = "HBA1C",
                    testName = "Glycated Hemoglobin (HbA1c)",
                    category = "Biochemistry",
                    specimenType = "EDTA Whole Blood",
                    containerTube = "Lavender / EDTA Tube",
                    method = "HPLC (NGSP Certified)",
                    defaultTurnaroundHours = 4,
                    standardRate = 550.0
                ),
                TestMasterEntity(
                    testCode = "TSH",
                    testName = "Thyroid Stimulating Hormone (TSH)",
                    category = "Serology",
                    specimenType = "Serum",
                    containerTube = "Red / Plain Tube",
                    method = "Chemiluminescent Microparticle Immunoassay (CMIA)",
                    defaultTurnaroundHours = 6,
                    standardRate = 300.0
                ),
                TestMasterEntity(
                    testCode = "URINE-RE",
                    testName = "Urine Routine & Microscopic (R/M)",
                    category = "Clinical Pathology",
                    specimenType = "Random Mid-stream Urine",
                    containerTube = "Sterile Urine Container",
                    method = "Automated Strip & High-Power Microscopy",
                    defaultTurnaroundHours = 3,
                    standardRate = 200.0
                ),
                TestMasterEntity(
                    testCode = "DENGUE-NS1",
                    testName = "Dengue Antigen NS1 Rapid",
                    category = "Serology",
                    specimenType = "Serum",
                    containerTube = "Red / Plain Tube",
                    method = "Immunochromatographic Assay",
                    defaultTurnaroundHours = 2,
                    standardRate = 600.0
                ),
                TestMasterEntity(
                    testCode = "WIDAL",
                    testName = "Widal Slide Agglutination (Typhoid)",
                    category = "Serology",
                    specimenType = "Serum",
                    containerTube = "Red / Plain Tube",
                    method = "Qualitative Slide Agglutination",
                    defaultTurnaroundHours = 3,
                    standardRate = 250.0
                )
            )
            testDao.insertTests(tests)

            // 5. Test Parameters
            val params = listOf(
                // CBC
                TestParameterEntity(testCode = "CBC", paramCode = "HB", paramName = "Hemoglobin (Hb)", unit = "g/dL", normalLowMale = 13.0, normalHighMale = 17.0, normalLowFemale = 12.0, normalHighFemale = 15.0, normalRangeText = "13.0 - 17.0 (M), 12.0 - 15.0 (F)", criticalLow = 7.0, criticalHigh = 20.0, sortOrder = 1),
                TestParameterEntity(testCode = "CBC", paramCode = "TLC", paramName = "Total Leukocyte Count (TLC)", unit = "/cumm", normalLowMale = 4000.0, normalHighMale = 11000.0, normalLowFemale = 4000.0, normalHighFemale = 11000.0, normalRangeText = "4,000 - 11,000", criticalLow = 2000.0, criticalHigh = 30000.0, sortOrder = 2),
                TestParameterEntity(testCode = "CBC", paramCode = "PLT", paramName = "Platelet Count", unit = "Lakh/cumm", normalLowMale = 1.5, normalHighMale = 4.5, normalLowFemale = 1.5, normalHighFemale = 4.5, normalRangeText = "1.50 - 4.50", criticalLow = 0.5, criticalHigh = 8.0, sortOrder = 3),
                TestParameterEntity(testCode = "CBC", paramCode = "RBC", paramName = "RBC Count", unit = "mill/cumm", normalLowMale = 4.5, normalHighMale = 5.5, normalLowFemale = 3.8, normalHighFemale = 4.8, normalRangeText = "4.5 - 5.5", sortOrder = 4),
                TestParameterEntity(testCode = "CBC", paramCode = "PCV", paramName = "PCV / Hematocrit", unit = "%", normalLowMale = 40.0, normalHighMale = 50.0, normalLowFemale = 36.0, normalHighFemale = 46.0, normalRangeText = "40.0 - 50.0", sortOrder = 5),
                TestParameterEntity(testCode = "CBC", paramCode = "MCV", paramName = "MCV", unit = "fL", normalLowMale = 80.0, normalHighMale = 100.0, normalLowFemale = 80.0, normalHighFemale = 100.0, normalRangeText = "80.0 - 100.0", sortOrder = 6),
                TestParameterEntity(testCode = "CBC", paramCode = "NEUT", paramName = "Neutrophils", unit = "%", normalLowMale = 40.0, normalHighMale = 75.0, normalLowFemale = 40.0, normalHighFemale = 75.0, normalRangeText = "40 - 75", sortOrder = 7),
                TestParameterEntity(testCode = "CBC", paramCode = "LYMPH", paramName = "Lymphocytes", unit = "%", normalLowMale = 20.0, normalHighMale = 45.0, normalLowFemale = 20.0, normalHighFemale = 45.0, normalRangeText = "20 - 45", sortOrder = 8),
                TestParameterEntity(testCode = "CBC", paramCode = "EOSIN", paramName = "Eosinophils", unit = "%", normalLowMale = 1.0, normalHighMale = 6.0, normalLowFemale = 1.0, normalHighFemale = 6.0, normalRangeText = "1 - 6", sortOrder = 9),

                // LFT
                TestParameterEntity(testCode = "LFT", paramCode = "TBIL", paramName = "Bilirubin (Total)", unit = "mg/dL", normalLowMale = 0.2, normalHighMale = 1.2, normalLowFemale = 0.2, normalHighFemale = 1.2, normalRangeText = "0.2 - 1.2", criticalHigh = 10.0, sortOrder = 1),
                TestParameterEntity(testCode = "LFT", paramCode = "DBIL", paramName = "Bilirubin (Direct)", unit = "mg/dL", normalLowMale = 0.0, normalHighMale = 0.3, normalLowFemale = 0.0, normalHighFemale = 0.3, normalRangeText = "0.0 - 0.3", sortOrder = 2),
                TestParameterEntity(testCode = "LFT", paramCode = "SGOT", paramName = "SGOT / AST", unit = "U/L", normalLowMale = 5.0, normalHighMale = 40.0, normalLowFemale = 5.0, normalHighFemale = 35.0, normalRangeText = "5 - 40", criticalHigh = 250.0, sortOrder = 3),
                TestParameterEntity(testCode = "LFT", paramCode = "SGPT", paramName = "SGPT / ALT", unit = "U/L", normalLowMale = 5.0, normalHighMale = 45.0, normalLowFemale = 5.0, normalHighFemale = 35.0, normalRangeText = "5 - 45", criticalHigh = 250.0, sortOrder = 4),
                TestParameterEntity(testCode = "LFT", paramCode = "ALP", paramName = "Alkaline Phosphatase (ALP)", unit = "U/L", normalLowMale = 30.0, normalHighMale = 120.0, normalLowFemale = 30.0, normalHighFemale = 120.0, normalRangeText = "30 - 120", sortOrder = 5),
                TestParameterEntity(testCode = "LFT", paramCode = "TP", paramName = "Total Protein", unit = "g/dL", normalLowMale = 6.0, normalHighMale = 8.3, normalLowFemale = 6.0, normalHighFemale = 8.3, normalRangeText = "6.0 - 8.3", sortOrder = 6),
                TestParameterEntity(testCode = "LFT", paramCode = "ALB", paramName = "Serum Albumin", unit = "g/dL", normalLowMale = 3.5, normalHighMale = 5.0, normalLowFemale = 3.5, normalHighFemale = 5.0, normalRangeText = "3.5 - 5.0", sortOrder = 7),

                // KFT
                TestParameterEntity(testCode = "KFT", paramCode = "UREA", paramName = "Blood Urea", unit = "mg/dL", normalLowMale = 15.0, normalHighMale = 45.0, normalLowFemale = 15.0, normalHighFemale = 45.0, normalRangeText = "15.0 - 45.0", criticalHigh = 100.0, sortOrder = 1),
                TestParameterEntity(testCode = "KFT", paramCode = "CREAT", paramName = "Serum Creatinine", unit = "mg/dL", normalLowMale = 0.6, normalHighMale = 1.2, normalLowFemale = 0.5, normalHighFemale = 1.1, normalRangeText = "0.6 - 1.2 (M), 0.5 - 1.1 (F)", criticalHigh = 5.0, sortOrder = 2),
                TestParameterEntity(testCode = "KFT", paramCode = "URIC", paramName = "Serum Uric Acid", unit = "mg/dL", normalLowMale = 3.5, normalHighMale = 7.2, normalLowFemale = 2.6, normalHighFemale = 6.0, normalRangeText = "3.5 - 7.2 (M), 2.6 - 6.0 (F)", sortOrder = 3),

                // LIPID
                TestParameterEntity(testCode = "LIPID", paramCode = "CHOL", paramName = "Total Cholesterol", unit = "mg/dL", normalLowMale = 120.0, normalHighMale = 200.0, normalLowFemale = 120.0, normalHighFemale = 200.0, normalRangeText = "< 200 (Desirable)", sortOrder = 1),
                TestParameterEntity(testCode = "LIPID", paramCode = "TRIG", paramName = "Triglycerides", unit = "mg/dL", normalLowMale = 50.0, normalHighMale = 150.0, normalLowFemale = 50.0, normalHighFemale = 150.0, normalRangeText = "< 150 (Normal)", criticalHigh = 500.0, sortOrder = 2),
                TestParameterEntity(testCode = "LIPID", paramCode = "HDL", paramName = "HDL Cholesterol", unit = "mg/dL", normalLowMale = 40.0, normalHighMale = 60.0, normalLowFemale = 50.0, normalHighFemale = 60.0, normalRangeText = "> 40.0 (Protective)", sortOrder = 3),
                TestParameterEntity(testCode = "LIPID", paramCode = "LDL", paramName = "LDL Cholesterol (Calculated)", unit = "mg/dL", normalLowMale = 60.0, normalHighMale = 100.0, normalLowFemale = 60.0, normalHighFemale = 100.0, normalRangeText = "< 100 (Optimal)", sortOrder = 4),

                // HBA1C
                TestParameterEntity(testCode = "HBA1C", paramCode = "HBA1C_VAL", paramName = "HbA1c (Glycosylated Hb)", unit = "%", normalLowMale = 4.0, normalHighMale = 5.6, normalLowFemale = 4.0, normalHighFemale = 5.6, normalRangeText = "< 5.7 (Normal), 5.7 - 6.4 (Pre-Diabetic)", criticalHigh = 12.0, sortOrder = 1),
                TestParameterEntity(testCode = "HBA1C", paramCode = "EAG", paramName = "Estimated Avg Glucose (eAG)", unit = "mg/dL", normalLowMale = 70.0, normalHighMale = 114.0, normalLowFemale = 70.0, normalHighFemale = 114.0, normalRangeText = "70 - 114", sortOrder = 2),

                // TSH
                TestParameterEntity(testCode = "TSH", paramCode = "TSH_VAL", paramName = "Thyroid Stimulating Hormone", unit = "µIU/mL", normalLowMale = 0.35, normalHighMale = 4.94, normalLowFemale = 0.35, normalHighFemale = 4.94, normalRangeText = "0.35 - 4.94", criticalLow = 0.05, criticalHigh = 25.0, sortOrder = 1),

                // URINE-RE
                TestParameterEntity(testCode = "URINE-RE", paramCode = "U_COLOR", paramName = "Color", resultType = "TEXT", normalRangeText = "Pale Yellow", sortOrder = 1),
                TestParameterEntity(testCode = "URINE-RE", paramCode = "U_PROT", paramName = "Protein / Albumin", resultType = "QUALITATIVE", normalRangeText = "Nil / Negative", sortOrder = 2),
                TestParameterEntity(testCode = "URINE-RE", paramCode = "U_SUG", paramName = "Glucose / Sugar", resultType = "QUALITATIVE", normalRangeText = "Nil / Negative", sortOrder = 3),
                TestParameterEntity(testCode = "URINE-RE", paramCode = "U_PUS", paramName = "Pus Cells", unit = "/hpf", normalLowMale = 0.0, normalHighMale = 5.0, normalLowFemale = 0.0, normalHighFemale = 5.0, normalRangeText = "0 - 5", sortOrder = 4),

                // DENGUE-NS1
                TestParameterEntity(testCode = "DENGUE-NS1", paramCode = "DENGUE_RES", paramName = "Dengue NS1 Antigen", resultType = "QUALITATIVE", normalRangeText = "Non-Reactive", sortOrder = 1),

                // WIDAL
                TestParameterEntity(testCode = "WIDAL", paramCode = "WIDAL_O", paramName = "S. Typhi 'O' Titre", resultType = "TEXT", normalRangeText = "< 1:80 (Negative)", sortOrder = 1),
                TestParameterEntity(testCode = "WIDAL", paramCode = "WIDAL_H", paramName = "S. Typhi 'H' Titre", resultType = "TEXT", normalRangeText = "< 1:80 (Negative)", sortOrder = 2)
            )
            testDao.insertParameters(params)

            // 6. Prepopulate Patients & Registrations for instant clinical workflow testing
            val now = System.currentTimeMillis()
            val dayAgo = now - 86400000L

            // Patient 1: Released & Approved
            val p1 = PatientEntity(
                uhid = "UHID-202610-0001",
                fullName = "Rahul Mukhopadhyay",
                ageYears = 42,
                gender = "Male",
                mobile = "9830112233",
                email = "rahul.m@example.com",
                address = "Salt Lake, Sector 2, Kolkata",
                referringDoctor = "Dr. S. K. Banerjee (MD Med)",
                hospital = "Apollo Clinic",
                patientCategory = "General",
                createdAt = dayAgo
            )
            patientDao.insert(p1)

            val reg1 = RegistrationEntity(
                regNumber = "REG-202610-0001",
                uhid = "UHID-202610-0001",
                patientName = "Rahul Mukhopadhyay",
                patientAge = 42,
                patientGender = "Male",
                patientMobile = "9830112233",
                referringDoctor = "Dr. S. K. Banerjee (MD Med)",
                hospital = "Apollo Clinic",
                rateListName = "Main / Standard",
                priority = "Routine",
                collectionLocation = "Lab Centre",
                totalAmount = 1100.0,
                discountAmount = 0.0,
                netAmount = 1100.0,
                paidAmount = 1100.0,
                dueAmount = 0.0,
                paymentStatus = "PAID",
                overallWorkflowStatus = "APPROVED",
                registeredByStaff = "Pooja Verma",
                createdAt = dayAgo,
                updatedAt = now
            )
            regDao.insertRegistration(reg1)

            regDao.insertOrderedTests(
                listOf(
                    OrderedTestEntity(regNumber = "REG-202610-0001", testCode = "CBC", testName = "Complete Blood Count (CBC with ESR)", category = "Hematology", specimenType = "EDTA Whole Blood", priceCharged = 350.0, workflowStatus = "APPROVED", sampleBarcode = "SMP-202610-101"),
                    OrderedTestEntity(regNumber = "REG-202610-0001", testCode = "LFT", testName = "Liver Function Test (LFT)", category = "Biochemistry", specimenType = "Serum", priceCharged = 750.0, workflowStatus = "APPROVED", sampleBarcode = "SMP-202610-102")
                )
            )

            regDao.insertSamples(
                listOf(
                    SampleEntity(sampleBarcode = "SMP-202610-101", regNumber = "REG-202610-0001", patientName = "Rahul Mukhopadhyay", specimenType = "EDTA Whole Blood", containerTube = "Lavender / EDTA Tube", status = "COLLECTED", collectedAt = dayAgo + 1800000L, collectedBy = "Amit Roy"),
                    SampleEntity(sampleBarcode = "SMP-202610-102", regNumber = "REG-202610-0001", patientName = "Rahul Mukhopadhyay", specimenType = "Serum", containerTube = "Yellow / SST Gel Tube", status = "COLLECTED", collectedAt = dayAgo + 1800000L, collectedBy = "Amit Roy")
                )
            )

            // Results for Patient 1
            resultDao.insertOrUpdateResults(
                listOf(
                    TestResultEntity(regNumber = "REG-202610-0001", testCode = "CBC", paramCode = "HB", paramName = "Hemoglobin (Hb)", resultValue = "14.2", unit = "g/dL", referenceIntervalText = "13.0 - 17.0", flag = "NORMAL", status = "APPROVED", enteredBy = "Bikram Das", approvedBy = "Dr. Arindam Sen, MD", approvedAt = now - 3600000L),
                    TestResultEntity(regNumber = "REG-202610-0001", testCode = "CBC", paramCode = "TLC", paramName = "Total Leukocyte Count (TLC)", resultValue = "7800", unit = "/cumm", referenceIntervalText = "4,000 - 11,000", flag = "NORMAL", status = "APPROVED", enteredBy = "Bikram Das", approvedBy = "Dr. Arindam Sen, MD", approvedAt = now - 3600000L),
                    TestResultEntity(regNumber = "REG-202610-0001", testCode = "CBC", paramCode = "PLT", paramName = "Platelet Count", resultValue = "2.40", unit = "Lakh/cumm", referenceIntervalText = "1.50 - 4.50", flag = "NORMAL", status = "APPROVED", enteredBy = "Bikram Das", approvedBy = "Dr. Arindam Sen, MD", approvedAt = now - 3600000L),
                    TestResultEntity(regNumber = "REG-202610-0001", testCode = "LFT", paramCode = "TBIL", paramName = "Bilirubin (Total)", resultValue = "0.9", unit = "mg/dL", referenceIntervalText = "0.2 - 1.2", flag = "NORMAL", status = "APPROVED", enteredBy = "Bikram Das", approvedBy = "Dr. Arindam Sen, MD", approvedAt = now - 3600000L),
                    TestResultEntity(regNumber = "REG-202610-0001", testCode = "LFT", paramCode = "SGPT", paramName = "SGPT / ALT", resultValue = "34", unit = "U/L", referenceIntervalText = "5 - 45", flag = "NORMAL", status = "APPROVED", enteredBy = "Bikram Das", approvedBy = "Dr. Arindam Sen, MD", approvedAt = now - 3600000L)
                )
            )

            billingDao.insertInvoice(
                InvoiceEntity(invoiceNumber = "INV-202610-0001", regNumber = "REG-202610-0001", uhid = "UHID-202610-0001", patientName = "Rahul Mukhopadhyay", subtotal = 1100.0, discount = 0.0, netAmount = 1100.0, paidAmount = 1100.0, dueAmount = 0.0, createdAt = dayAgo)
            )
            billingDao.insertPayment(
                PaymentEntity(receiptNumber = "RCP-202610-0001", invoiceNumber = "INV-202610-0001", regNumber = "REG-202610-0001", amount = 1100.0, paymentMode = "UPI", transactionRef = "UPI9823471029", paymentType = "FULL", collectedBy = "Pooja Verma", notes = "GPay payment verified", createdAt = dayAgo)
            )

            // Patient 2: Result Entered (Ready for Pathologist Review)
            val p2 = PatientEntity(
                uhid = "UHID-202610-0002",
                fullName = "Sunita Sharma",
                ageYears = 35,
                gender = "Female",
                mobile = "9876543210",
                email = "sunita.s@example.com",
                address = "Behala, Kolkata",
                referringDoctor = "Dr. Neha Sen (Endocrinologist)",
                hospital = "Fortis Hospital",
                patientCategory = "General",
                createdAt = now - 14400000L
            )
            patientDao.insert(p2)

            val reg2 = RegistrationEntity(
                regNumber = "REG-202610-0002",
                uhid = "UHID-202610-0002",
                patientName = "Sunita Sharma",
                patientAge = 35,
                patientGender = "Female",
                patientMobile = "9876543210",
                referringDoctor = "Dr. Neha Sen (Endocrinologist)",
                hospital = "Fortis Hospital",
                rateListName = "Main / Standard",
                priority = "Routine",
                collectionLocation = "Lab Centre",
                totalAmount = 1350.0,
                discountAmount = 100.0,
                discountReason = "Doctor Privilege Card",
                netAmount = 1250.0,
                paidAmount = 1250.0,
                dueAmount = 0.0,
                paymentStatus = "PAID",
                overallWorkflowStatus = "RESULT_ENTERED",
                registeredByStaff = "Pooja Verma",
                createdAt = now - 14400000L,
                updatedAt = now - 3600000L
            )
            regDao.insertRegistration(reg2)

            regDao.insertOrderedTests(
                listOf(
                    OrderedTestEntity(regNumber = "REG-202610-0002", testCode = "LIPID", testName = "Lipid Profile (Fasting)", category = "Biochemistry", specimenType = "Serum (Fasting 12h)", priceCharged = 800.0, workflowStatus = "RESULT_ENTERED", sampleBarcode = "SMP-202610-103"),
                    OrderedTestEntity(regNumber = "REG-202610-0002", testCode = "HBA1C", testName = "Glycated Hemoglobin (HbA1c)", category = "Biochemistry", specimenType = "EDTA Whole Blood", priceCharged = 550.0, workflowStatus = "RESULT_ENTERED", sampleBarcode = "SMP-202610-104")
                )
            )

            regDao.insertSamples(
                listOf(
                    SampleEntity(sampleBarcode = "SMP-202610-103", regNumber = "REG-202610-0002", patientName = "Sunita Sharma", specimenType = "Serum", containerTube = "Yellow / SST Gel Tube", status = "IN_LAB", collectedAt = now - 12000000L, collectedBy = "Amit Roy", receivedInLabAt = now - 10000000L, receivedBy = "Bikram Das"),
                    SampleEntity(sampleBarcode = "SMP-202610-104", regNumber = "REG-202610-0002", patientName = "Sunita Sharma", specimenType = "EDTA Whole Blood", containerTube = "Lavender / EDTA Tube", status = "IN_LAB", collectedAt = now - 12000000L, collectedBy = "Amit Roy", receivedInLabAt = now - 10000000L, receivedBy = "Bikram Das")
                )
            )

            resultDao.insertOrUpdateResults(
                listOf(
                    TestResultEntity(regNumber = "REG-202610-0002", testCode = "LIPID", paramCode = "CHOL", paramName = "Total Cholesterol", resultValue = "225", unit = "mg/dL", referenceIntervalText = "< 200", flag = "HIGH", status = "VERIFIED", enteredBy = "Bikram Das", enteredAt = now - 3600000L),
                    TestResultEntity(regNumber = "REG-202610-0002", testCode = "LIPID", paramCode = "TRIG", paramName = "Triglycerides", resultValue = "180", unit = "mg/dL", referenceIntervalText = "< 150", flag = "HIGH", status = "VERIFIED", enteredBy = "Bikram Das", enteredAt = now - 3600000L),
                    TestResultEntity(regNumber = "REG-202610-0002", testCode = "HBA1C", paramCode = "HBA1C_VAL", paramName = "HbA1c (Glycosylated Hb)", resultValue = "6.8", unit = "%", referenceIntervalText = "< 5.7 (Normal), >= 6.5 (Diabetic)", flag = "HIGH", status = "VERIFIED", enteredBy = "Bikram Das", enteredAt = now - 3600000L)
                )
            )

            billingDao.insertInvoice(
                InvoiceEntity(invoiceNumber = "INV-202610-0002", regNumber = "REG-202610-0002", uhid = "UHID-202610-0002", patientName = "Sunita Sharma", subtotal = 1350.0, discount = 100.0, discountNote = "Doctor Referral", netAmount = 1250.0, paidAmount = 1250.0, dueAmount = 0.0, createdAt = now - 14400000L)
            )
            billingDao.insertPayment(
                PaymentEntity(receiptNumber = "RCP-202610-0002", invoiceNumber = "INV-202610-0002", regNumber = "REG-202610-0002", amount = 1250.0, paymentMode = "CASH", paymentType = "FULL", collectedBy = "Pooja Verma", notes = "Cash received", createdAt = now - 14400000L)
            )

            // Patient 3: In Processing (Has Outstanding Due)
            val p3 = PatientEntity(
                uhid = "UHID-202610-0003",
                fullName = "Anil Kumar Patel",
                ageYears = 58,
                gender = "Male",
                mobile = "9903123456",
                email = "anil.patel@example.com",
                address = "Howrah Station Road",
                referringDoctor = "Self / Walk-in",
                hospital = "Private",
                patientCategory = "Senior Citizen",
                createdAt = now - 7200000L
            )
            patientDao.insert(p3)

            val reg3 = RegistrationEntity(
                regNumber = "REG-202610-0003",
                uhid = "UHID-202610-0003",
                patientName = "Anil Kumar Patel",
                patientAge = 58,
                patientGender = "Male",
                patientMobile = "9903123456",
                referringDoctor = "Self / Walk-in",
                hospital = "Private",
                rateListName = "Senior Citizen",
                priority = "Urgent",
                collectionLocation = "Lab Centre",
                totalAmount = 650.0,
                discountAmount = 65.0,
                discountReason = "Senior Citizen 10%",
                netAmount = 585.0,
                paidAmount = 300.0,
                dueAmount = 285.0,
                paymentStatus = "PARTIAL",
                overallWorkflowStatus = "IN_PROCESSING",
                registeredByStaff = "Pooja Verma",
                createdAt = now - 7200000L,
                updatedAt = now - 3600000L
            )
            regDao.insertRegistration(reg3)

            regDao.insertOrderedTests(
                listOf(
                    OrderedTestEntity(regNumber = "REG-202610-0003", testCode = "KFT", testName = "Kidney / Renal Function Test (KFT)", category = "Biochemistry", specimenType = "Serum", priceCharged = 585.0, workflowStatus = "IN_PROCESSING", sampleBarcode = "SMP-202610-105")
                )
            )

            regDao.insertSamples(
                listOf(
                    SampleEntity(sampleBarcode = "SMP-202610-105", regNumber = "REG-202610-0003", patientName = "Anil Kumar Patel", specimenType = "Serum", containerTube = "Yellow / SST Gel Tube", status = "IN_LAB", collectedAt = now - 5400000L, collectedBy = "Amit Roy", receivedInLabAt = now - 3600000L, receivedBy = "Bikram Das")
                )
            )

            billingDao.insertInvoice(
                InvoiceEntity(invoiceNumber = "INV-202610-0003", regNumber = "REG-202610-0003", uhid = "UHID-202610-0003", patientName = "Anil Kumar Patel", subtotal = 650.0, discount = 65.0, discountNote = "Senior Citizen 10%", netAmount = 585.0, paidAmount = 300.0, dueAmount = 285.0, createdAt = now - 7200000L)
            )
            billingDao.insertPayment(
                PaymentEntity(receiptNumber = "RCP-202610-0003", invoiceNumber = "INV-202610-0003", regNumber = "REG-202610-0003", amount = 300.0, paymentMode = "CASH", paymentType = "ADVANCE", collectedBy = "Pooja Verma", notes = "Advance paid; ₹285 due at report delivery", createdAt = now - 7200000L)
            )

            // Patient 4: Just Booked
            val p4 = PatientEntity(
                uhid = "UHID-202610-0004",
                fullName = "Priya Ghosh",
                ageYears = 28,
                gender = "Female",
                mobile = "9831987654",
                email = "priya.ghosh@example.com",
                address = "New Town Action Area 1, Kolkata",
                referringDoctor = "Dr. A. Dey (Gynecologist)",
                hospital = "Peerless Hospital",
                patientCategory = "General",
                createdAt = now - 1800000L
            )
            patientDao.insert(p4)

            val reg4 = RegistrationEntity(
                regNumber = "REG-202610-0004",
                uhid = "UHID-202610-0004",
                patientName = "Priya Ghosh",
                patientAge = 28,
                patientGender = "Female",
                patientMobile = "9831987654",
                referringDoctor = "Dr. A. Dey (Gynecologist)",
                hospital = "Peerless Hospital",
                rateListName = "Main / Standard",
                priority = "Routine",
                collectionLocation = "Home Visit",
                totalAmount = 500.0,
                discountAmount = 0.0,
                netAmount = 500.0,
                paidAmount = 500.0,
                dueAmount = 0.0,
                paymentStatus = "PAID",
                overallWorkflowStatus = "BOOKED",
                registeredByStaff = "Pooja Verma",
                createdAt = now - 1800000L,
                updatedAt = now - 1800000L
            )
            regDao.insertRegistration(reg4)

            regDao.insertOrderedTests(
                listOf(
                    OrderedTestEntity(regNumber = "REG-202610-0004", testCode = "TSH", testName = "Thyroid Stimulating Hormone (TSH)", category = "Serology", specimenType = "Serum", priceCharged = 300.0, workflowStatus = "BOOKED", sampleBarcode = "SMP-202610-106"),
                    OrderedTestEntity(regNumber = "REG-202610-0004", testCode = "URINE-RE", testName = "Urine Routine & Microscopic (R/M)", category = "Clinical Pathology", specimenType = "Random Mid-stream Urine", priceCharged = 200.0, workflowStatus = "BOOKED", sampleBarcode = "SMP-202610-107")
                )
            )

            regDao.insertSamples(
                listOf(
                    SampleEntity(sampleBarcode = "SMP-202610-106", regNumber = "REG-202610-0004", patientName = "Priya Ghosh", specimenType = "Serum", containerTube = "Red / Plain Tube", status = "BOOKED"),
                    SampleEntity(sampleBarcode = "SMP-202610-107", regNumber = "REG-202610-0004", patientName = "Priya Ghosh", specimenType = "Random Mid-stream Urine", containerTube = "Sterile Urine Container", status = "BOOKED")
                )
            )

            billingDao.insertInvoice(
                InvoiceEntity(invoiceNumber = "INV-202610-0004", regNumber = "REG-202610-0004", uhid = "UHID-202610-0004", patientName = "Priya Ghosh", subtotal = 500.0, discount = 0.0, netAmount = 500.0, paidAmount = 500.0, dueAmount = 0.0, createdAt = now - 1800000L)
            )
            billingDao.insertPayment(
                PaymentEntity(receiptNumber = "RCP-202610-0004", invoiceNumber = "INV-202610-0004", regNumber = "REG-202610-0004", amount = 500.0, paymentMode = "UPI", transactionRef = "UPI55299102", paymentType = "FULL", collectedBy = "Pooja Verma", notes = "UPI transfer", createdAt = now - 1800000L)
            )

            // Audit Log
            auditDao.insertLog(
                AuditLogEntity(action = "SYSTEM_INIT", entityType = "SYSTEM", entityId = "LIMS", performedBy = "System", details = "Clinical database initialized with master data and test records")
            )
        }
    }
}
