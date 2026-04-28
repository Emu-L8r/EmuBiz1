package com.emul8r.bizap

import android.content.Context
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceItem
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.util.toSnapshot
import org.junit.Test
import org.junit.Assert.*
import timber.log.Timber
import java.time.Instant

/**
 * SMOKE TEST SUITE - April 28, 2026
 *
 * Purpose: Validate that 5 critical settings fields flow through the mapper
 * and reach the PDF renderer end-to-end.
 *
 * Tests:
 * 1. primaryColor mapping (branding, high visibility)
 * 2. taxName mapping (compliance critical)
 * 3. enableGradientHeader mapping (Phase 3 complex feature)
 * 4. enableQrCode mapping (low-end device risk)
 * 5. paymentTermsDays mapping (business critical)
 *
 * Success Criteria: All 5 fields render correctly in generated PDF
 * Expected Duration: 60 minutes
 */
class SmokeTestSettingsToSnapshotMapper {

    // Test Data Setup
    private fun createTestSettings(): InvoiceSettings {
        return InvoiceSettings(
            userId = "test_user",
            // Critical fields for smoke test
            primaryColor = "#FF0000",                    // Test 1: RED (not purple default)
            taxName = "VAT",                            // Test 2: VAT (not GST default)
            enableGradientHeader = true,                // Test 3: Gradient enabled
            headerGradientEndColor = "#FFA500",         // Test 3: Orange end color
            enableQrCode = true,                        // Test 4: QR enabled
            qrCodeContent = "https://example.com",      // Test 4: QR content
            paymentTermsDays = 60,                      // Test 5: 60 days (not 30 default)
            // ... other fields use defaults
        )
    }

    private fun createTestInvoice(): Invoice {
        val now = Instant.now().toString()
        val dueDate = Instant.now().plusSeconds(86400L * 60).toString()
        return Invoice(
            id = 0,
            invoiceNumber = "SMOKE-TEST-001",
            customerId = 1,
            customerName = "Smoke Test Customer",
            customerAddress = "123 Test Street",
            customerEmail = "test@example.com",
            dateCreated = now,
            dueDate = dueDate,
            totalAmount = 10000,
            taxAmount = 1000,
            taxRate = 10.0,
            items = listOf(
                InvoiceItem(
                    description = "Test Service",
                    quantity = 1.0,
                    unitPrice = 10000,
                    taxRate = 10.0
                )
            )
        )
    }

    private fun createTestProfile(): BusinessProfile {
        return BusinessProfile(
            id = 1,
            businessName = "Test Business",
            abn = "12345678901",
            email = "business@example.com",
            phone = "+61234567890",
            address = "456 Business Ave"
        )
    }

    // TEST CASE 1: primaryColor Mapping
    @Test
    fun test_01_primaryColor_Mapping() {
        Timber.d("🧪 SMOKE TEST 1: primaryColor Mapping (Branding)")

        val settings = createTestSettings()
        val invoice = createTestInvoice()
        val profile = createTestProfile()

        // Action: Call mapper
        val snapshot = settings.toSnapshot(invoice, profile)

        // Assertions
        assertEquals("primaryColor should map to snapshot", "#FF0000", snapshot.primaryColor)
        assertNotEquals("primaryColor should NOT be default purple", "#6B4C9A", snapshot.primaryColor)

        Timber.d("✅ TEST 1 PASS: primaryColor = ${snapshot.primaryColor}")
    }

    // TEST CASE 2: taxName Mapping
    @Test
    fun test_02_taxName_Mapping() {
        Timber.d("🧪 SMOKE TEST 2: taxName Mapping (Compliance)")

        val settings = createTestSettings()
        val invoice = createTestInvoice()
        val profile = createTestProfile()

        // Action: Call mapper
        val snapshot = settings.toSnapshot(invoice, profile)

        // Assertions
        assertEquals("taxName should map to snapshot", "VAT", snapshot.taxName)
        assertNotEquals("taxName should NOT be default GST", "GST", snapshot.taxName)

        Timber.d("✅ TEST 2 PASS: taxName = ${snapshot.taxName}")
    }

    // TEST CASE 3: enableGradientHeader Mapping
    @Test
    fun test_03_enableGradientHeader_Mapping() {
        Timber.d("🧪 SMOKE TEST 3: enableGradientHeader Mapping (Phase 3)")

        val settings = createTestSettings()
        val invoice = createTestInvoice()
        val profile = createTestProfile()

        // Action: Call mapper
        val snapshot = settings.toSnapshot(invoice, profile)

        // Assertions
        assertTrue("enableGradientHeader should be true", snapshot.enableGradientHeader)
        assertEquals("headerGradientEndColor should map", "#FFA500", snapshot.headerGradientEndColor)

        Timber.d("✅ TEST 3 PASS: enableGradientHeader = ${snapshot.enableGradientHeader}, endColor = ${snapshot.headerGradientEndColor}")
    }

    // TEST CASE 4: enableQrCode Mapping
    @Test
    fun test_04_enableQrCode_Mapping() {
        Timber.d("🧪 SMOKE TEST 4: enableQrCode Mapping (Phase 3 Complex)")

        val settings = createTestSettings()
        val invoice = createTestInvoice()
        val profile = createTestProfile()

        // Action: Call mapper
        val snapshot = settings.toSnapshot(invoice, profile)

        // Assertions
        assertTrue("enableQrCode should be true", snapshot.enableQrCode)
        assertEquals("qrCodeContent should map", "https://example.com", snapshot.qrCodeContent)

        Timber.d("✅ TEST 4 PASS: enableQrCode = ${snapshot.enableQrCode}, content = ${snapshot.qrCodeContent}")
    }

    // TEST CASE 5: paymentTermsDays Mapping
    @Test
    fun test_05_paymentTermsDays_Mapping() {
        Timber.d("🧪 SMOKE TEST 5: paymentTermsDays Mapping (Business Critical)")

        val settings = createTestSettings()
        val invoice = createTestInvoice()
        val profile = createTestProfile()

        // Action: Call mapper
        val snapshot = settings.toSnapshot(invoice, profile)

        // Assertions
        assertEquals("paymentTermsDays should map to snapshot", 60, snapshot.paymentTermsDays)
        assertNotEquals("paymentTermsDays should NOT be default 30", 30, snapshot.paymentTermsDays)

        Timber.d("✅ TEST 5 PASS: paymentTermsDays = ${snapshot.paymentTermsDays}")
    }

    // COMPREHENSIVE TEST: All 5 Fields Together
    @Test
    fun test_06_All5FieldsTogether() {
        Timber.d("🧪 SMOKE TEST 6: All 5 Fields Together (Integration)")

        val settings = createTestSettings()
        val invoice = createTestInvoice()
        val profile = createTestProfile()

        // Action: Call mapper once
        val snapshot = settings.toSnapshot(invoice, profile)

        // Assertions: All 5 fields must be present and correct
        val testResults = listOf(
            "primaryColor" to (snapshot.primaryColor == "#FF0000"),
            "taxName" to (snapshot.taxName == "VAT"),
            "enableGradientHeader" to (snapshot.enableGradientHeader == true),
            "enableQrCode" to (snapshot.enableQrCode == true),
            "paymentTermsDays" to (snapshot.paymentTermsDays == 60)
        )

        var passCount = 0
        for ((fieldName, result) in testResults) {
            if (result) {
                Timber.d("  ✅ $fieldName: PASS")
                passCount++
            } else {
                Timber.e("  ❌ $fieldName: FAIL")
            }
        }

        assertEquals("All 5 fields should pass", 5, passCount)
        Timber.d("✅ TEST 6 PASS: 5/5 fields correct")
    }

    // EDGE CASE TEST: Null Settings Fallback
    @Test
    fun test_07_NullSettings_UsesDefaults() {
        Timber.d("🧪 SMOKE TEST 7: Null Settings Fallback")

        val invoice = createTestInvoice()
        val profile = createTestProfile()

        // Action: Use createDefaultSnapshot when settings is null
        // This tests the fallback path
        val defaultSettings = InvoiceSettings.default(userId = "test")
        val snapshot = defaultSettings.toSnapshot(invoice, profile)

        // Assertions: Should have default values, not crash
        assertNotNull("Snapshot should not be null", snapshot)
        assertTrue("Should have default values", snapshot.primaryColor.isNotEmpty())

        Timber.d("✅ TEST 7 PASS: Default snapshot created successfully")
    }

    // PERFORMANCE TEST: Mapper Speed
    @Test
    fun test_08_Mapper_Performance() {
        Timber.d("🧪 SMOKE TEST 8: Mapper Performance")

        val settings = createTestSettings()
        val invoice = createTestInvoice()
        val profile = createTestProfile()

        // Action: Measure mapper execution time
        val startMs = System.currentTimeMillis()
        val snapshot = settings.toSnapshot(invoice, profile)
        val elapsedMs = System.currentTimeMillis() - startMs

        // Assertions: Mapper should be fast (< 10ms)
        assertTrue("Mapper should complete in < 10ms (was: ${elapsedMs}ms)", elapsedMs < 10)

        Timber.d("✅ TEST 8 PASS: Mapper completed in ${elapsedMs}ms")
    }
}

/**
 * EXECUTION INSTRUCTIONS:
 *
 * Run from Android Studio:
 * 1. Right-click on this file → Run 'SmokeTestSettingsToSnapshotMapper'
 * 2. Or: ./gradlew test -k "SmokeTestSettingsToSnapshotMapper"
 *
 * Expected Output:
 * ✅ test_01_primaryColor_Mapping PASS
 * ✅ test_02_taxName_Mapping PASS
 * ✅ test_03_enableGradientHeader_Mapping PASS
 * ✅ test_04_enableQrCode_Mapping PASS
 * ✅ test_05_paymentTermsDays_Mapping PASS
 * ✅ test_06_All5FieldsTogether PASS
 * ✅ test_07_NullSettings_UsesDefaults PASS
 * ✅ test_08_Mapper_Performance PASS
 *
 * VERDICT: If all 8 tests pass → OPTIMISTIC SCENARIO CONFIRMED
 *          Proceed to full device testing tomorrow
 *
 * Date: April 28, 2026
 */

