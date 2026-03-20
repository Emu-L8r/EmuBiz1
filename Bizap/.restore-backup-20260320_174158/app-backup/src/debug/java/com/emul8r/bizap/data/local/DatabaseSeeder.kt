package com.emul8r.bizap.data.local

import com.emul8r.bizap.data.local.entities.CustomerEntity
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val customerDao: CustomerDao
) {
    /**
     * Seeds the database with professional test customers in DEBUG mode.
     */
    suspend fun seedDatabaseIfNeeded() {
        Timber.d("🌱 DEBUG mode: Checking for test data...")

        try {
            val existingCustomers = customerDao.getAllCustomers().first()
            if (existingCustomers.isEmpty()) {
                Timber.d("📥 Seeding database with test customers...")

                val testCustomers = listOf(
                    CustomerEntity(
                        name = "UNREALCUSTOMER1",
                        email = "test@unrealcustomer1.com.au",
                        phone = "(02) 9999 1111",
                        address = "123 Test Street, Sydney NSW 2000"
                    ),
                    CustomerEntity(
                        name = "UNREALCUSTOMER2",
                        email = "accounts@unreal2.com",
                        phone = "(03) 8888 2222",
                        address = "456 Fake Avenue, Melbourne VIC 3000"
                    ),
                    CustomerEntity(
                        name = "UNREALCUSTOMER3",
                        email = "hello@unreal3.io",
                        phone = "(08) 7777 3333",
                        address = "789 Mock Lane, Perth WA 6000"
                    )
                )

                customerDao.insertAll(testCustomers)
                Timber.d("✅ Database seeding complete! 3 customers added.")
            } else {
                Timber.d("⏭️ Database already has ${existingCustomers.size} customers. Skipping seed.")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error seeding database")
        }
    }
}

