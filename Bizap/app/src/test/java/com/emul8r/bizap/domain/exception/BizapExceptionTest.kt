package com.emul8r.bizap.domain.exception

import com.emul8r.bizap.domain.error.BizapException
import com.emul8r.bizap.domain.error.ErrorSeverity
import com.emul8r.bizap.domain.error.isRetryable
import com.emul8r.bizap.domain.error.severity
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [BizapException] sealed class hierarchy.
 *
 * Verifies:
 * - Each subtype carries the expected message and metadata
 * - Cause / original exception is preserved
 * - [isRetryable] and [severity] extension functions return the right values
 */
class BizapExceptionTest {

    // ─── ValidationError ────────────────────────────────────────────────────────

    @Test
    fun `ValidationError carries field name`() {
        val ex = BizapException.ValidationError(field = "email", message = "Invalid format")
        assertEquals("email", ex.field)
    }

    @Test
    fun `ValidationError carries message`() {
        val ex = BizapException.ValidationError(field = "amount", message = "Must be > 0")
        assertEquals("Must be > 0", ex.message)
    }

    @Test
    fun `ValidationError optional actualValue is null by default`() {
        val ex = BizapException.ValidationError(field = "name", message = "Blank")
        assertNull(ex.actualValue)
    }

    @Test
    fun `ValidationError with actualValue carries it`() {
        val ex = BizapException.ValidationError(
            field = "email", message = "Bad", actualValue = "notanemail"
        )
        assertEquals("notanemail", ex.actualValue)
    }

    @Test
    fun `ValidationError is NOT retryable`() {
        val ex = BizapException.ValidationError(field = "f", message = "m")
        assertFalse(ex.isRetryable())
    }

    @Test
    fun `ValidationError has HIGH severity`() {
        val ex = BizapException.ValidationError(field = "f", message = "m")
        assertEquals(ErrorSeverity.HIGH, ex.severity())
    }

    // ─── DatabaseError ───────────────────────────────────────────────────────────

    @Test
    fun `DatabaseError message includes operation and table`() {
        val ex = BizapException.DatabaseError(operation = "INSERT", table = "invoices", message = "Constraint")
        assertNotNull(ex.message, "Message should not be null")
        assertTrue(ex.message!!.contains("INSERT"), "Message should contain operation: ${ex.message}")
        assertTrue(ex.message!!.contains("invoices"), "Message should contain table: ${ex.message}")
    }

    @Test
    fun `DatabaseError SELECT operation is retryable`() {
        val ex = BizapException.DatabaseError(operation = "SELECT", table = "customers", message = "Timeout")
        assertTrue(ex.isRetryable())
    }

    @Test
    fun `DatabaseError DELETE operation is NOT retryable`() {
        val ex = BizapException.DatabaseError(operation = "DELETE", table = "invoices", message = "Error")
        assertFalse(ex.isRetryable())
    }

    @Test
    fun `DatabaseError has CRITICAL severity`() {
        val ex = BizapException.DatabaseError(operation = "INSERT", table = "invoices", message = "Error")
        assertEquals(ErrorSeverity.CRITICAL, ex.severity())
    }

    // ─── NetworkError ────────────────────────────────────────────────────────────

    @Test
    fun `NetworkError carries endpoint and status`() {
        val ex = BizapException.NetworkError(
            endpoint = "/api/rates",
            statusCode = 503,
            message = "Unavailable"
        )
        assertEquals("/api/rates", ex.endpoint)
        assertEquals(503, ex.statusCode)
    }

    @Test
    fun `NetworkError with isRetryable true is retryable`() {
        val ex = BizapException.NetworkError(
            endpoint = "/api", message = "Error", isRetryable = true
        )
        assertTrue(ex.isRetryable())
    }

    @Test
    fun `NetworkError with isRetryable false is NOT retryable`() {
        val ex = BizapException.NetworkError(
            endpoint = "/api", message = "Error", isRetryable = false
        )
        assertFalse(ex.isRetryable())
    }

    // ─── TimeoutError ────────────────────────────────────────────────────────────

    @Test
    fun `TimeoutError is always retryable`() {
        val ex = BizapException.TimeoutError(endpoint = "/sync", timeoutMs = 30_000)
        assertTrue(ex.isRetryable())
    }

    @Test
    fun `TimeoutError message contains endpoint and timeout`() {
        val ex = BizapException.TimeoutError(endpoint = "/sync", timeoutMs = 30_000)
        assertTrue(ex.message!!.contains("/sync"))
        assertTrue(ex.message!!.contains("30000"))
    }

    // ─── FileError ───────────────────────────────────────────────────────────────

    @Test
    fun `FileError carries operation and filePath`() {
        val ex = BizapException.FileError(
            operation = "PDF_GENERATION",
            filePath = "/tmp/invoice.pdf",
            reason = "Permission denied"
        )
        assertEquals("PDF_GENERATION", ex.operation)
        assertEquals("/tmp/invoice.pdf", ex.filePath)
    }

    @Test
    fun `FileError is NOT retryable`() {
        val ex = BizapException.FileError(operation = "PDF_GENERATION", filePath = "/tmp/x", reason = "err")
        assertFalse(ex.isRetryable())
    }

    @Test
    fun `FileError has CRITICAL severity`() {
        val ex = BizapException.FileError(operation = "PDF_GENERATION", filePath = "/tmp/x", reason = "err")
        assertEquals(ErrorSeverity.CRITICAL, ex.severity())
    }

    // ─── UnknownError ────────────────────────────────────────────────────────────

    @Test
    fun `UnknownError preserves original exception`() {
        val original = RuntimeException("root cause")
        val ex = BizapException.UnknownError(
            message = "Something went wrong",
            originalException = original
        )
        assertEquals(original, ex.originalException)
    }

    @Test
    fun `UnknownError is NOT retryable`() {
        val ex = BizapException.UnknownError(message = "oops")
        assertFalse(ex.isRetryable())
    }

    @Test
    fun `UnknownError has CRITICAL severity`() {
        val ex = BizapException.UnknownError(message = "oops")
        assertEquals(ErrorSeverity.CRITICAL, ex.severity())
    }

    // ─── BusinessLogicError ──────────────────────────────────────────────────────

    @Test
    fun `BusinessLogicError carries rule and reason`() {
        val ex = BizapException.BusinessLogicError(
            rule = "Locked invoices are immutable",
            action = "Edit PAID invoice",
            reason = "Invoice is locked"
        )
        assertEquals("Locked invoices are immutable", ex.rule)
        assertTrue(ex.message!!.contains("Invoice is locked"))
    }

    @Test
    fun `BusinessLogicError is NOT retryable`() {
        val ex = BizapException.BusinessLogicError(rule = "r", action = "a", reason = "n")
        assertFalse(ex.isRetryable())
    }

    // ─── MigrationError ──────────────────────────────────────────────────────────

    @Test
    fun `MigrationError carries version numbers`() {
        val ex = BizapException.MigrationError(fromVersion = 23, toVersion = 24, reason = "Type mismatch")
        assertEquals(23, ex.fromVersion)
        assertEquals(24, ex.toVersion)
    }

    @Test
    fun `MigrationError is NOT retryable`() {
        val ex = BizapException.MigrationError(fromVersion = 1, toVersion = 2, reason = "err")
        assertFalse(ex.isRetryable())
    }

    @Test
    fun `MigrationError has CRITICAL severity`() {
        val ex = BizapException.MigrationError(fromVersion = 1, toVersion = 2, reason = "err")
        assertEquals(ErrorSeverity.CRITICAL, ex.severity())
    }

    // ─── Connectivity & Storage ──────────────────────────────────────────────────

    @Test
    fun `ConnectivityError is retryable`() {
        val ex = BizapException.ConnectivityError()
        assertTrue(ex.isRetryable())
    }

    @Test
    fun `StorageError is retryable`() {
        val ex = BizapException.StorageError()
        assertTrue(ex.isRetryable())
    }

    @Test
    fun `StorageError has HIGH severity`() {
        val ex = BizapException.StorageError()
        assertEquals(ErrorSeverity.HIGH, ex.severity())
    }

    // ─── NotFoundError / DuplicateError ─────────────────────────────────────────

    @Test
    fun `NotFoundError message contains entity type and identifier`() {
        val ex = BizapException.NotFoundError(entityType = "Invoice", identifier = "42")
        assertTrue(ex.message!!.contains("Invoice"))
        assertTrue(ex.message!!.contains("42"))
    }

    @Test
    fun `DuplicateError message contains entity type and identifier`() {
        val ex = BizapException.DuplicateError(
            entityType = "Customer", identifier = "john@example.com", existingId = 7L
        )
        assertTrue(ex.message!!.contains("Customer"))
        assertTrue(ex.message!!.contains("john@example.com"))
    }

    // ─── all subtypes are BizapException ────────────────────────────────────────

    @Test
    fun `all subtypes are BizapException`() {
        val exceptions: List<BizapException> = listOf(
            BizapException.ValidationError("f", "m"),
            BizapException.InvalidInvoiceError("reason"),
            BizapException.DatabaseError("SELECT", "t", "msg"),
            BizapException.MigrationError(1, 2, "reason"),
            BizapException.NetworkError("url", message = "err"),
            BizapException.TimeoutError("url", 1000),
            BizapException.ConnectivityError(),
            BizapException.FileError("op", "path", "reason"),
            BizapException.StorageError(),
            BizapException.BusinessLogicError("rule", "action", "reason"),
            BizapException.DuplicateError("T", "id", 1L),
            BizapException.NotFoundError("T", "id"),
            BizapException.UnknownError("oops")
        )
        exceptions.forEach { ex ->
            assertNotNull(ex)
            assertTrue(ex is BizapException)
        }
    }
}
