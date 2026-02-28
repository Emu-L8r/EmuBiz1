package com.emul8r.bizap.data.local

import android.util.Log
import com.emul8r.bizap.BuildConfig
import com.emul8r.bizap.data.local.entities.CustomerEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val customerDao: CustomerDao
) {
    /**
     * Seeds the database with professional test customers if in DEBUG mode 
     * and the table is currently empty.
     */
    suspend fun seedDatabaseIfNeeded() {
        if (!BuildConfig.DEBUG) return

        Log.d("DatabaseSeeder", "🌱 DEBUG mode: Checking for test data...")
        
        try {
            val existingCustomers = customerDao.getAllCustomers().first()
            if (existingCustomers.isEmpty()) {
                Log.d("DatabaseSeeder", "📥 Seeding database with test customers...")
                
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
                Log.d("DatabaseSeeder", "✅ Database seeding complete! 3 customers added.")
            } else {
                Log.d("DatabaseSeeder", "✓ Database already has ${existingCustomers.size} customers. Skipping seeding.")
            }
        } catch (e: Exception) {
            Log.e("DatabaseSeeder", "❌ Seeding failed: ${e.message}", e)
        }
    }
}
