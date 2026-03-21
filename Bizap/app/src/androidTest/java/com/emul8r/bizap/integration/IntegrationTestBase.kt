package com.emul8r.bizap.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.entities.BusinessProfileEntity
import com.emul8r.bizap.data.repository.BusinessProfileRepositoryImpl
import com.emul8r.bizap.data.repository.CustomerRepositoryImpl
import com.emul8r.bizap.data.repository.InvoiceRepositoryImpl
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Base class for integration tests.
 * 
 * Provides:
 * - In-memory Room database for testing
 * - Repository instances for common operations
 * - Helper methods for creating test data
 * - Cleanup after each test
 * 
 * Integration tests validate that multiple components work together correctly,
 * including repositories, DAOs, and database operations.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
abstract class IntegrationTestBase {
    
    protected lateinit var database: AppDatabase
    protected lateinit var context: Context
    protected lateinit var invoiceRepository: InvoiceRepository
    protected lateinit var customerRepository: CustomerRepository
    protected lateinit var businessProfileRepository: BusinessProfileRepository
    protected val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setupDatabase() {
        context = ApplicationProvider.getApplicationContext()
        
        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries() // Allow synchronous queries in tests
            .build()
        
        // Initialize repositories
        invoiceRepository = InvoiceRepositoryImpl(
            database.invoiceDao(),
            database.invoiceDaoV2(),
            database.customerDao(),
            database.businessProfileDao(),
            database.analyticsDao()
        )
        
        customerRepository = CustomerRepositoryImpl(
            database.customerDao(),
            database.customerDaoV2(),
            database.customerAnalyticsDao()
        )
        
        businessProfileRepository = BusinessProfileRepositoryImpl(
            database.businessProfileDao()
        )
    }
    
    @After
    fun closeDatabase() {
        database.close()
    }
    
    /**
     * Creates a test customer with default values.
     * 
     * @param id Customer ID (default: auto-generated)
     * @param name Customer name
     * @param email Customer email
     * @param businessId Associated business profile ID
     * @return The customer ID
     */
    protected suspend fun createTestCustomer(
        id: Long = 0L,
        name: String = "Test Customer",
        email: String = "test@example.com",
        businessId: Long = 1L
    ): Result<Long> {
        val customer = Customer(
            id = id,
            businessProfileId = businessId,
            name = name,
            email = email,
            phone = "+1234567890",
            address = "123 Test St",
            city = "Test City",
            state = "TS",
            zipCode = "12345",
            country = "Test Country",
            notes = "Test customer",
            isActive = true
        )
        return customerRepository.insert(customer)
    }
    
    /**
     * Creates a test invoice with default values.
     * 
     * @param customerId Customer ID for the invoice
     * @param amount Total amount in cents
     * @param status Invoice status
     * @param businessId Associated business profile ID
     * @return Result containing the invoice ID
     */
    protected suspend fun createTestInvoice(
        customerId: Long,
        amount: Long = 10000L, // $100.00
        status: InvoiceStatus = InvoiceStatus.SENT,
        businessId: Long = 1L
    ): Result<Long> {
        val invoice = Invoice(
            id = 0L,
            businessProfileId = businessId,
            customerId = customerId,
            invoiceNumber = "INV-${System.currentTimeMillis()}",
            year = LocalDate.now().year,
            sequenceNumber = 1,
            version = 1,
            date = LocalDate.now(),
            dueDate = LocalDate.now().plusDays(30),
            subtotal = amount,
            taxAmount = 0L,
            totalAmount = amount,
            amountPaid = 0L,
            status = status,
            currency = "USD",
            notes = "Test invoice",
            items = emptyList(),
            pdfPath = null,
            isActive = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            termsAndConditions = null,
            templateId = null,
            customFields = emptyMap()
        )
        return invoiceRepository.saveInvoice(invoice)
    }
    
    /**
     * Creates a test business profile with default values.
     * 
     * @param id Business profile ID
     * @param name Business name
     * @return Result containing the business profile ID
     */
    protected suspend fun createTestBusinessProfile(
        id: Long = 1L,
        name: String = "Test Business"
    ): Result<Long> {
        val entity = BusinessProfileEntity(
            id = id,
            businessName = name,
            ownerName = "Test Owner",
            email = "business@test.com",
            phone = "+1234567890",
            address = "456 Business Ave",
            city = "Business City",
            state = "BS",
            zipCode = "54321",
            country = "Test Country",
            logoPath = null,
            taxId = "123456789",
            isActive = true
        )
        
        return try {
            database.businessProfileDao().insert(entity)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
