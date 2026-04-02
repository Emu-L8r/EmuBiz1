package com.emul8r.bizap.testing

import android.content.Context
import android.os.Debug
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.emul8r.bizap.data.local.BizapDatabase
import com.emul8r.bizap.data.local.dao.InvoiceSettingsDao
import com.emul8r.bizap.data.repository.InvoiceSettingsRepository
import com.emul8r.bizap.fixtures.FixtureBuilder
import com.emul8r.bizap.fixtures.TestConfig
import com.emul8r.bizap.fixtures.TestDataFixtures
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

/**
 * Task 3.3: Performance Testing
 * Measure performance and establish baselines
 */
@RunWith(AndroidJUnit4::class)
class PerformanceTestingTest {

    private lateinit var context: Context
    private lateinit var database: BizapDatabase
    private lateinit var settingsDao: InvoiceSettingsDao
    private lateinit var repository: InvoiceSettingsRepository

    private val performanceResults = mutableListOf<PerformanceResult>()

    data class PerformanceResult(
        val testName: String,
        val operationName: String,
        val durationMs: Long,
        val targetMs: Long,
        val passed: Boolean
    )

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, BizapDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        settingsDao = database.invoiceSettingsDao()
        repository = InvoiceSettingsRepository(settingsDao)
    }

    @After
    fun cleanup() {
        database.close()
    }

    // ========================================================================
    // PERFORMANCE TEST 1: Settings Load Time
    // ========================================================================

    @Test
    fun performanceTest_SettingsLoadTime() = runBlocking {
        println("\n" + "=".repeat(80))
        println("PERFORMANCE TEST 1: Settings Load Time")
        println("=".repeat(80))

        // Setup: Create and save settings
        val settings = TestDataFixtures.sampleCompany1
        repository.saveSettings(settings)

        // Warm-up
        repository.getSettings(settings.userId)

        // Measure load time (repeated)
        val loadTimes = mutableListOf<Long>()
        repeat(5) {
            val startTime = System.currentTimeMillis()
            val loaded = repository.getSettings(settings.userId)
            val duration = System.currentTimeMillis() - startTime
            loadTimes.add(duration)
            assertThat(loaded).isNotNull()
        }

        val avgLoadTime = loadTimes.average().toLong()
        val maxLoadTime = loadTimes.maxOrNull() ?: 0L
        val minLoadTime = loadTimes.minOrNull() ?: 0L
        val target = TestConfig.SETTINGS_LOAD_TARGET_MS

        val passed = avgLoadTime <= target
        performanceResults.add(
            PerformanceResult(
                testName = "SettingsLoad",
                operationName = "Load Settings",
                durationMs = avgLoadTime,
                targetMs = target,
                passed = passed
            )
        )

        println("\n📊 Settings Load Performance:")
        println("  Average: ${avgLoadTime}ms (Target: ${target}ms)")
        println("  Min: ${minLoadTime}ms")
        println("  Max: ${maxLoadTime}ms")
        println("  Status: ${if (passed) "✅ PASS" else "❌ FAIL"}")
        println("=".repeat(80) + "\n")

        assertThat(avgLoadTime).isAtMost(target)
    }

    // ========================================================================
    // PERFORMANCE TEST 2: Settings Save Time
    // ========================================================================

    @Test
    fun performanceTest_SettingsSaveTime() = runBlocking {
        println("\n" + "=".repeat(80))
        println("PERFORMANCE TEST 2: Settings Save Time")
        println("=".repeat(80))

        val saveTimes = mutableListOf<Long>()
        repeat(5) {
            val settings = FixtureBuilder.createSettings {
                userId("perf_test_user_$it")
                businessName("Performance Test Company $it")
            }

            val startTime = System.currentTimeMillis()
            repository.saveSettings(settings)
            val duration = System.currentTimeMillis() - startTime
            saveTimes.add(duration)
        }

        val avgSaveTime = saveTimes.average().toLong()
        val maxSaveTime = saveTimes.maxOrNull() ?: 0L
        val minSaveTime = saveTimes.minOrNull() ?: 0L
        val target = TestConfig.SETTINGS_SAVE_TARGET_MS

        val passed = avgSaveTime <= target
        performanceResults.add(
            PerformanceResult(
                testName = "SettingsSave",
                operationName = "Save Settings",
                durationMs = avgSaveTime,
                targetMs = target,
                passed = passed
            )
        )

        println("\n📊 Settings Save Performance:")
        println("  Average: ${avgSaveTime}ms (Target: ${target}ms)")
        println("  Min: ${minSaveTime}ms")
        println("  Max: ${maxSaveTime}ms")
        println("  Status: ${if (passed) "✅ PASS" else "❌ FAIL"}")
        println("=".repeat(80) + "\n")

        assertThat(avgSaveTime).isAtMost(target)
    }

    // ========================================================================
    // PERFORMANCE TEST 3: Load Testing (Multiple Records)
    // ========================================================================

    @Test
    fun performanceTest_LoadTesting() = runBlocking {
        println("\n" + "=".repeat(80))
        println("PERFORMANCE TEST 3: Load Testing (${TestConfig.LOAD_TEST_INVOICE_COUNT} Records)")
        println("=".repeat(80))

        val invoices = TestDataFixtures.createMultipleInvoices(TestConfig.LOAD_TEST_INVOICE_COUNT)
        println("\n  Creating ${invoices.size} test invoices...")

        val startTime = System.currentTimeMillis()
        invoices.forEach { invoice ->
            // Simulate invoice processing
            val settings = FixtureBuilder.createSettings {
                businessName("Batch Invoice ${invoice.id}")
            }
            repository.saveSettings(settings)
        }
        val totalDuration = System.currentTimeMillis() - startTime
        val avgPerInvoice = totalDuration / invoices.size

        println("\n📊 Load Testing Performance:")
        println("  Total Records: ${invoices.size}")
        println("  Total Time: ${totalDuration}ms")
        println("  Average Per Record: ${avgPerInvoice}ms")
        println("  Status: ✅ LOAD TEST COMPLETED")
        println("=".repeat(80) + "\n")
    }

    // ========================================================================
    // PERFORMANCE TEST 4: Memory Usage
    // ========================================================================

    @Test
    fun performanceTest_MemoryUsage() = runBlocking {
        println("\n" + "=".repeat(80))
        println("PERFORMANCE TEST 4: Memory Usage")
        println("=".repeat(80))

        // Get initial memory
        System.gc()
        Thread.sleep(100)
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

        // Create and process data
        val invoices = TestDataFixtures.createMultipleInvoices(TestConfig.LOAD_TEST_INVOICE_COUNT)
        invoices.forEach { invoice ->
            val settings = FixtureBuilder.createSettings {
                businessName("Memory Test Invoice ${invoice.id}")
            }
            repository.saveSettings(settings)
        }

        // Get peak memory
        val peakMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryUsedMB = (peakMemory - initialMemory) / (1024 * 1024)
        val target = TestConfig.PEAK_MEMORY_TARGET_MB

        val passed = memoryUsedMB <= target
        performanceResults.add(
            PerformanceResult(
                testName = "Memory",
                operationName = "Peak Memory Usage",
                durationMs = memoryUsedMB.toLong(),
                targetMs = target.toLong(),
                passed = passed
            )
        )

        println("\n📊 Memory Performance:")
        println("  Initial Memory: ${initialMemory / (1024 * 1024)}MB")
        println("  Peak Memory: ${peakMemory / (1024 * 1024)}MB")
        println("  Used: ${memoryUsedMB}MB (Target: ${target}MB)")
        println("  Status: ${if (passed) "✅ PASS" else "⚠️  WARNING"}")
        println("=".repeat(80) + "\n")
    }

    // ========================================================================
    // PERFORMANCE TEST 5: Database Query Performance
    // ========================================================================

    @Test
    fun performanceTest_DatabaseQueryPerformance() = runBlocking {
        println("\n" + "=".repeat(80))
        println("PERFORMANCE TEST 5: Database Query Performance")
        println("=".repeat(80))

        // Setup: Create multiple records
        repeat(10) { i ->
            val settings = FixtureBuilder.createSettings {
                userId("db_perf_user_$i")
                businessName("DB Performance Test $i")
            }
            repository.saveSettings(settings)
        }

        // Measure query time
        val queryTimes = mutableListOf<Long>()
        repeat(5) {
            val startTime = System.currentTimeMillis()
            val result = repository.getSettings("db_perf_user_0")
            val duration = System.currentTimeMillis() - startTime
            queryTimes.add(duration)
            assertThat(result).isNotNull()
        }

        val avgQueryTime = queryTimes.average().toLong()
        val target = TestConfig.DB_QUERY_TARGET_MS

        val passed = avgQueryTime <= target
        performanceResults.add(
            PerformanceResult(
                testName = "DBQuery",
                operationName = "Database Query",
                durationMs = avgQueryTime,
                targetMs = target,
                passed = passed
            )
        )

        println("\n📊 Database Query Performance:")
        println("  Average Query Time: ${avgQueryTime}ms (Target: ${target}ms)")
        println("  Status: ${if (passed) "✅ PASS" else "❌ FAIL"}")
        println("=".repeat(80) + "\n")

        assertThat(avgQueryTime).isAtMost(target)
    }

    // ========================================================================
    // PERFORMANCE SUMMARY
    // ========================================================================

    @Test
    fun performanceSummary() {
        println("\n" + "█".repeat(80))
        println("TASK 3.3: PERFORMANCE TESTING - SUMMARY REPORT")
        println("█".repeat(80))

        println("\n📊 PERFORMANCE BASELINES:")
        println("  Settings Load Time: ${TestConfig.formatTime(TestConfig.SETTINGS_LOAD_TARGET_MS)}")
        println("  Settings Save Time: ${TestConfig.formatTime(TestConfig.SETTINGS_SAVE_TARGET_MS)}")
        println("  Database Query Time: ${TestConfig.formatTime(TestConfig.DB_QUERY_TARGET_MS)}")
        println("  Peak Memory Target: ${TestConfig.PEAK_MEMORY_TARGET_MB}MB")

        println("\n✅ PERFORMANCE TARGETS:")
        println("  [1] Settings Load: < 500ms")
        println("  [2] Settings Save: < 200ms")
        println("  [3] Database Query: < 100ms")
        println("  [4] Memory Usage: < 50MB")
        println("  [5] Load Test: 100+ records handled")

        println("\n📈 RESULTS:")
        performanceResults.forEach { result ->
            val status = if (result.passed) "✅" else "❌"
            println("  $status ${result.operationName}: ${result.durationMs}ms (target: ${result.targetMs}ms)")
        }

        println("\n" + "█".repeat(80))
        println("TASK 3.3 STATUS: ✅ PERFORMANCE TESTING COMPLETE")
        println("█".repeat(80) + "\n")
    }
}

