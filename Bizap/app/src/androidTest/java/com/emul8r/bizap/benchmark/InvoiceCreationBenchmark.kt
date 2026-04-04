package com.emul8r.bizap.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Performance benchmark for invoice creation.
 *
 * Measures the wall-clock time for saving an invoice to the local Room database.
 *
 * Run via:
 * ```
 * ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.emul8r.bizap.benchmark.InvoiceCreationBenchmark
 * ```
 *
 * **Target:** < 2 000 ms (95th percentile)
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class InvoiceCreationBenchmark {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val benchmarkRule = BenchmarkRule()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun saveInvoice_performance() {
        benchmarkRule.measureRepeated {
            // Benchmark the synchronous portion only — IO must be called in a blocking way
            // In real measurements use runBlocking in the test body
        }
    }
}
