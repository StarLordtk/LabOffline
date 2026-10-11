package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PatientEntity>>

    @Query("""
        SELECT * FROM patients 
        WHERE fullName LIKE '%' || :query || '%' 
           OR uhid LIKE '%' || :query || '%' 
           OR mobile LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun search(query: String): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE uhid = :uhid LIMIT 1")
    suspend fun getByUhid(uhid: String): PatientEntity?

    @Query("SELECT * FROM patients WHERE mobile = :mobile LIMIT 1")
    suspend fun getByMobile(mobile: String): PatientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patient: PatientEntity): Long

    @Update
    suspend fun update(patient: PatientEntity)

    @Query("SELECT COUNT(*) FROM patients")
    suspend fun getCount(): Int
}

@Dao
interface StaffUserDao {
    @Query("SELECT * FROM staff_users WHERE isActive = 1")
    fun getAllActive(): Flow<List<StaffUserEntity>>

    @Query("SELECT * FROM staff_users")
    fun getAll(): Flow<List<StaffUserEntity>>

    @Query("SELECT * FROM staff_users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): StaffUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: StaffUserEntity): Long

    @Update
    suspend fun update(user: StaffUserEntity)
}

@Dao
interface TestDao {
    @Query("SELECT * FROM test_master ORDER BY category ASC, testName ASC")
    fun getAllTests(): Flow<List<TestMasterEntity>>

    @Query("SELECT * FROM test_master WHERE isActive = 1 ORDER BY testName ASC")
    fun getActiveTests(): Flow<List<TestMasterEntity>>

    @Query("SELECT * FROM test_master WHERE testCode = :code LIMIT 1")
    suspend fun getTestByCode(code: String): TestMasterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: TestMasterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTests(tests: List<TestMasterEntity>)

    @Update
    suspend fun updateTest(test: TestMasterEntity)

    @Query("SELECT * FROM test_parameters WHERE testCode = :testCode ORDER BY sortOrder ASC")
    fun getParametersForTest(testCode: String): Flow<List<TestParameterEntity>>

    @Query("SELECT * FROM test_parameters WHERE testCode = :testCode ORDER BY sortOrder ASC")
    suspend fun getParametersForTestSync(testCode: String): List<TestParameterEntity>

    @Query("SELECT * FROM test_parameters ORDER BY testCode, sortOrder ASC")
    fun getAllParameters(): Flow<List<TestParameterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParameters(params: List<TestParameterEntity>)

    @Query("SELECT * FROM rate_lists")
    fun getAllRateLists(): Flow<List<RateListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRateLists(rateLists: List<RateListEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRateList(rateList: RateListEntity): Long

    @Update
    suspend fun updateRateList(rateList: RateListEntity)
}

@Dao
interface RegistrationDao {
    @Query("SELECT * FROM registrations ORDER BY createdAt DESC")
    fun getAllRegistrations(): Flow<List<RegistrationEntity>>

    @Query("SELECT * FROM registrations WHERE createdAt >= :startTs AND createdAt <= :endTs ORDER BY createdAt DESC")
    fun getRegistrationsByDateRange(startTs: Long, endTs: Long): Flow<List<RegistrationEntity>>

    @Query("SELECT * FROM registrations WHERE regNumber = :regNumber LIMIT 1")
    fun getRegistration(regNumber: String): Flow<RegistrationEntity?>

    @Query("SELECT * FROM registrations WHERE regNumber = :regNumber LIMIT 1")
    suspend fun getRegistrationSync(regNumber: String): RegistrationEntity?

    @Query("SELECT * FROM registrations WHERE uhid = :uhid ORDER BY createdAt DESC")
    fun getRegistrationsForPatient(uhid: String): Flow<List<RegistrationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegistration(reg: RegistrationEntity): Long

    @Update
    suspend fun updateRegistration(reg: RegistrationEntity)

    @Query("SELECT * FROM ordered_tests WHERE regNumber = :regNumber")
    fun getOrderedTests(regNumber: String): Flow<List<OrderedTestEntity>>

    @Query("SELECT * FROM ordered_tests WHERE regNumber = :regNumber")
    suspend fun getOrderedTestsSync(regNumber: String): List<OrderedTestEntity>

    @Query("SELECT * FROM ordered_tests ORDER BY id DESC")
    fun getAllOrderedTests(): Flow<List<OrderedTestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderedTests(tests: List<OrderedTestEntity>)

    @Update
    suspend fun updateOrderedTest(test: OrderedTestEntity)

    @Query("SELECT * FROM samples WHERE regNumber = :regNumber")
    fun getSamplesForRegistration(regNumber: String): Flow<List<SampleEntity>>

    @Query("SELECT * FROM samples WHERE regNumber = :regNumber")
    suspend fun getSamplesForRegistrationSync(regNumber: String): List<SampleEntity>

    @Query("SELECT * FROM samples ORDER BY id DESC")
    fun getAllSamples(): Flow<List<SampleEntity>>

    @Query("SELECT * FROM samples WHERE sampleBarcode = :barcode LIMIT 1")
    suspend fun getSampleByBarcode(barcode: String): SampleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSamples(samples: List<SampleEntity>)

    @Update
    suspend fun updateSample(sample: SampleEntity)

    @Query("SELECT COUNT(*) FROM registrations")
    suspend fun getRegistrationCount(): Int
}

@Dao
interface TestResultDao {
    @Query("SELECT * FROM test_results WHERE regNumber = :regNumber ORDER BY id ASC")
    fun getResultsForRegistration(regNumber: String): Flow<List<TestResultEntity>>

    @Query("SELECT * FROM test_results WHERE regNumber = :regNumber ORDER BY id ASC")
    suspend fun getResultsForRegistrationSync(regNumber: String): List<TestResultEntity>

    @Query("SELECT * FROM test_results ORDER BY id DESC")
    fun getAllResults(): Flow<List<TestResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateResults(results: List<TestResultEntity>)

    @Update
    suspend fun updateResult(result: TestResultEntity)

    @Query("UPDATE test_results SET status = 'APPROVED', approvedBy = :approvedBy, approvedAt = :approvedAt WHERE regNumber = :regNumber")
    suspend fun approveResults(regNumber: String, approvedBy: String, approvedAt: Long)
}

@Dao
interface BillingDao {
    @Query("SELECT * FROM invoices ORDER BY createdAt DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE invoiceNumber = :invNumber LIMIT 1")
    suspend fun getInvoiceByNumber(invNumber: String): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE regNumber = :regNumber LIMIT 1")
    fun getInvoiceForReg(regNumber: String): Flow<InvoiceEntity?>

    @Query("SELECT * FROM invoices WHERE regNumber = :regNumber LIMIT 1")
    suspend fun getInvoiceForRegSync(regNumber: String): InvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Query("SELECT * FROM payments ORDER BY createdAt DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE regNumber = :regNumber ORDER BY createdAt ASC")
    fun getPaymentsForReg(regNumber: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE regNumber = :regNumber ORDER BY createdAt ASC")
    suspend fun getPaymentsForRegSync(regNumber: String): List<PaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun getInvoiceCount(): Int

    @Query("SELECT COUNT(*) FROM payments")
    suspend fun getPaymentCount(): Int
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Insert
    suspend fun insertLog(log: AuditLogEntity): Long
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM lab_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<LabSettingsEntity?>

    @Query("SELECT * FROM lab_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSync(): LabSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: LabSettingsEntity)

    @Update
    suspend fun updateSettings(settings: LabSettingsEntity)
}
