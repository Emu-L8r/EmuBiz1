package com.emul8r.bizap.domain.exception

import com.emul8r.bizap.domain.error.BizapException
import com.emul8r.bizap.domain.error.ErrorSeverity
import com.emul8r.bizap.domain.error.isRetryable
import com.emul8r.bizap.domain.error.severity
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for [BizapException] sealed class, [isRetryable], and [severity] extensions.
 */
class BizapExceptionTest {

    // ── isRetryable ────────────────────────────────────────────────────────────

    @Test
    fun `isRetryable - ValidationError is never retryable`() {
        val ex = BizapException.ValidationError(field = "email", message = "Invalid email")
        assertFalse(ex.isRetryable())
    }

    @Test
    fun `isRetryable - TimeoutError is always retryable`() {
        val ex = BizapException.TimeoutError(endpoint = "/api/rates", timeoutMs = 30_000)
        assertTrue(ex.isRetryable())
    }

    @Test
    fun `isRetryable - ConnectivityError is retryable`() {
        val ex = BizapException.ConnectivityError()
        assertTrue(ex.isRetryable())
    }

    @Test
    fun `isRetryable - NetworkError marked retryable returns true`() {
        val ex = BizapException.NetworkError(endpoint = "/api", message = "503", isRetryable = true)
        assertTrue(ex.isRetryable())
    }

    @Test
    fun `isRetryable - NetworkError marked not retryable returns false`() {
        val ex = BizapException.NetworkError(endpoint = "/api", message = "400", isRetryable = false)
        assertFalse(ex.isRetryable())
    }

    @Test
    fun `isRetryable - DatabaseError SELECT is retryable`() {
        val ex = BizapException.DatabaseError(operation = "SELECT", table = "invoices", details = "Timeout")
        assertTrue(ex.isRetryable())
    }

    @Test
    fun `isRetryable - MigrationError is never retryable`() {
        val ex = BizapException.MigrationError(fromVersion = 23, toVersion = 24, reason = "Schema mismatch")
        assertFalse(ex.isRetryable())
    }

    @Test
    fun `isRetryable - FileError is not retryable`() {
        val ex = BizapException.FileError(operation = "PDF_GENERATION", filePath = "/tmp/invoice.pdf", reason = "No permission")
        assertFalse(ex.isRetryable())
    }

    @Test
    fun `isRetryable - StorageError is retryable`() {
        val ex = BizapException.StorageError()
        assertTrue(ex.isRetryable())
    }

    @Test
    fun `isRetryable - BusinessLogicError is not retryable`() {
        val ex = BizapException.BusinessLogicError(rule = "No edits", action = "Edit", reason = "Locked")
        assertFalse(ex.isRetryable())
    }

    @Test
    fun `isRetryable - DuplicateError is not retryable`() {
        val ex = BizapException.DuplicateError(entityType = "Customer", identifier = "joe@test.com", existingId = 42)
        assertFalse(ex.isRetryable())
    }

    @Test
    fun `isRetryable - NotFoundError is not retryable`() {
        val ex = BizapException.NotFoundError(entityType = "Invoice", identifier = "999")
        assertFalse(ex.isRetryable())
    }

    @Test
    fun `isRetryable - UnknownError is not retryable`() {
        val ex = BizapException.UnknownError(message = "Unexpected crash")
        assertFalse(ex.isRetryable())
    }

    // ── severity ──────────────────────────────────────────────────────────────

    @Test
    fun `severity - DatabaseError is CRITICAL`() {
        val ex = BizapException.DatabaseError(operation = "INSERT", table = "invoices", details = "Error")
        assertEquals(ErrorSeverity.CRITICAL, ex.severity())
    }

    @Test
    fun `severity - MigrationError is CRITICAL`() {
        val ex = BizapException.MigrationError(fromVersion = 1, toVersion = 2, reason = "Fail")
        assertEquals(ErrorSeverity.CRITICAL, ex.severity())
    }

    @Test
    fun `severity - ValidationError is HIGH`() {
        val ex = BizapException.ValidationError(field = "name", message = "Empty")
        assertEquals(ErrorSeverity.HIGH, ex.severity())
    }

    @Test
    fun `severity - NetworkError with no retries is LOW`() {
        val ex = BizapException.NetworkError(endpoint = "/api", message = "Error", retryCount = 0)
        assertEquals(ErrorSeverity.LOW, ex.severity())
    }

    @Test
    fun `severity - NetworkError after retries is MEDIUM`() {
        val ex = BizapException.NetworkError(endpoint = "/api", message = "Error", retryCount = 2)
        assertEquals(ErrorSeverity.MEDIUM, ex.severity())
    }

    @Test
    fun `severity - UnknownError is CRITICAL`() {
        val ex = BizapException.UnknownError(message = "Unexpected")
        assertEquals(ErrorSeverity.CRITICAL, ex.severity())
    }

    // ── message formatting ────────────────────────────────────────────────────

    @Test
    fun `DatabaseError message contains operation and table`() {
        val ex = BizapException.DatabaseError(operation = "INSERT", table = "customers", details = "FK violation")
        assertTrue(ex.message!!.contains("INSERT"))
        assertTrue(ex.message!!.contains("customers"))
    }

    @Test
    fun `NotFoundError message contains entity type and identifier`() {
        val ex = BizapException.NotFoundError(entityType = "Invoice", identifier = "42")
        assertTrue(ex.message!!.contains("Invoice"))
        assertTrue(ex.message!!.contains("42"))
    }

    @Test
    fun `DuplicateError message contains entity type and identifier`() {
        val ex = BizapException.DuplicateError(entityType = "Customer", identifier = "test@mail.com", existingId = 7)
        assertTrue(ex.message!!.contains("Customer"))
        assertTrue(ex.message!!.contains("test@mail.com"))
    }

    @Test
    fun `TimeoutError message contains endpoint and timeout`() {
        val ex = BizapException.TimeoutError(endpoint = "/api/rates", timeoutMs = 30_000)
        assertNotNull(ex.message)
        assertTrue(ex.message!!.contains("/api/rates"))
    }

    @Test
    fun `UnknownError wraps original exception`() {
        val original = RuntimeException("Root cause")
        val ex = BizapException.UnknownError(message = "Wrapped", originalException = original)
        assertEquals(original, ex.originalException)
    }

    @Test
    fun `ValidationError carries field and actual value`() {
        val ex = BizapException.ValidationError(
            field = "email",
            message = "Invalid format",
            actualValue = "not-an-email",
            validationRule = "Must contain @"
        )
        assertEquals("email", ex.field)
        assertEquals("not-an-email", ex.actualValue)
        assertEquals("Must contain @", ex.validationRule)
    }
}

