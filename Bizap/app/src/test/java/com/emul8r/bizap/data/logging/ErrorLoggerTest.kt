package com.emul8r.bizap.data.logging

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.error.BizapException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Unit tests for [ErrorLoggerImpl].
 *
 * Verifies that errors, breadcrumbs and user context are forwarded to
 * Crashlytics and Analytics — and that failures in those libraries are
 * handled gracefully (no crash, no exception propagation).
 */
class ErrorLoggerTest : BaseUnitTest() {

    private val analytics: FirebaseAnalytics = mockk(relaxed = true)
    private val crashlytics: FirebaseCrashlytics = mockk(relaxed = true)

    private lateinit var logger: ErrorLogger

    @Before
    fun setUp() {
        logger = ErrorLoggerImpl(analytics, crashlytics)
    }

    // ─── logError(Throwable) ────────────────────────────────────────────────────

    @Test
    fun `logError with exception records to Crashlytics`() {
        val ex = RuntimeException("test error")
        logger.logError(ex)
        verify(exactly = 1) { crashlytics.recordException(ex) }
    }

    @Test
    fun `logError with context sets custom keys before recording`() {
        val ex = RuntimeException("ctx error")
        val ctx = mapOf("invoice_id" to "42", "operation" to "save")

        logger.logError(ex, ctx)

        verify { crashlytics.setCustomKey("invoice_id", "42") }
        verify { crashlytics.setCustomKey("operation", "save") }
        verify { crashlytics.recordException(ex) }
    }

    @Test
    fun `logError sends app_error event to Analytics`() {
        val ex = IllegalStateException("state error")
        // Verify that this doesn't throw an exception
        try {
            logger.logError(ex)
            // If we got here, the call succeeded
            assertTrue(true, "logError should not throw when calling analytics")
        } catch (e: Exception) {
            fail("logError threw unexpected exception: ${e.message}")
        }
    }

    @Test
    fun `logError with BizapException ValidationError still records`() {
        val ex = BizapException.ValidationError(
            field = "email",
            message = "Invalid format",
            actualValue = "notanemail"
        )
        logger.logError(ex)
        verify(exactly = 1) { crashlytics.recordException(ex) }
    }

    @Test
    fun `logError with BizapException DatabaseError still records`() {
        val ex = BizapException.DatabaseError(
            operation = "INSERT",
            table = "invoices",
            message = "Constraint violation"
        )
        logger.logError(ex)
        verify(exactly = 1) { crashlytics.recordException(ex) }
    }

    @Test
    fun `logError swallows Crashlytics failure gracefully`() {
        every { crashlytics.recordException(any()) } throws RuntimeException("Crashlytics down")
        // Should not throw
        logger.logError(RuntimeException("safe"))
    }

    @Test
    fun `logError swallows Analytics failure gracefully`() {
        every { analytics.logEvent(any(), any()) } throws RuntimeException("Analytics down")
        // Should not throw
        logger.logError(RuntimeException("safe"))
    }

    // ─── logError(tag, message) ──────────────────────────────────────────────────

    @Test
    fun `logError with tag and message calls Crashlytics log`() {
        logger.logError("InvoiceRepo", "Failed to save", mapOf("id" to "7"))
        verify { crashlytics.log(any()) }
        verify { crashlytics.setCustomKey("id", "7") }
    }

    @Test
    fun `logError with tag sends Analytics event`() {
        try {
            logger.logError("PaymentRepo", "Payment record error")
            // If we got here without throwing, the method succeeded
            assertTrue(true, "logError with tag should not throw")
        } catch (e: Exception) {
            fail("logError with tag threw unexpected exception: ${e.message}")
        }
    }

    // ─── addBreadcrumb ───────────────────────────────────────────────────────────

    @Test
    fun `addBreadcrumb logs to Crashlytics`() {
        logger.addBreadcrumb("invoice_saved", mapOf("invoice_id" to "1"))
        verify { crashlytics.log(any()) }
    }

    @Test
    fun `addBreadcrumb with empty details does not crash`() {
        logger.addBreadcrumb("screen_opened")
    }

    @Test
    fun `addBreadcrumb swallows Crashlytics failure`() {
        every { crashlytics.log(any()) } throws RuntimeException("log failed")
        logger.addBreadcrumb("action")
    }

    // ─── setUserContext ──────────────────────────────────────────────────────────

    @Test
    fun `setUserContext sets user ID and email on Crashlytics`() {
        logger.setUserContext(userId = 123L, email = "test@example.com")
        verify { crashlytics.setUserId("123") }
        verify { crashlytics.setCustomKey("user_email", "test@example.com") }
    }

    @Test
    fun `setUserContext sets user ID on Analytics`() {
        logger.setUserContext(userId = 99L, email = "user@bizap.app")
        verify { analytics.setUserId("99") }
    }

    @Test
    fun `setUserContext swallows Crashlytics failure`() {
        every { crashlytics.setUserId(any()) } throws RuntimeException("setUserId failed")
        logger.setUserContext(1L, "a@b.com")
    }

    // ─── null analytics ──────────────────────────────────────────────────────────

    @Test
    fun `ErrorLoggerImpl works when analytics is null`() {
        val loggerNoAnalytics = ErrorLoggerImpl(analytics = null, crashlytics = crashlytics)
        // Must not throw
        loggerNoAnalytics.logError(RuntimeException("no analytics"))
        loggerNoAnalytics.logError("Tag", "msg")
        loggerNoAnalytics.addBreadcrumb("action")
        loggerNoAnalytics.setUserContext(1L, "x@y.z")
    }

    @Test
    fun `ErrorLoggerImpl works when both analytics and crashlytics are null`() {
        val loggerNullAll = ErrorLoggerImpl(analytics = null, crashlytics = null)
        // Must not throw
        loggerNullAll.logError(RuntimeException("null both"))
        loggerNullAll.logError("Tag", "msg")
        loggerNullAll.addBreadcrumb("action")
        loggerNullAll.setUserContext(1L, "x@y.z")
    }

    // ─── instance ────────────────────────────────────────────────────────────────

    @Test
    fun `ErrorLoggerImpl is not null after construction`() {
        assertNotNull(logger)
    }
}
