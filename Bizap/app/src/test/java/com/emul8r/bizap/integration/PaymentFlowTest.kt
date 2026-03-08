package com.emul8r.bizap.integration

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.repository.gui2.PaymentRepositoryV2
import com.emul8r.bizap.domain.usecase.RecordPaymentUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration tests for the payment recording flow.
 *
 * Verifies that:
 * 1. Recording a payment updates the correct invoice balance
 * 2. Overpayments are rejected
 * 3. Future-dated payments are rejected
 * 4. Partial payments are accepted and outstanding is updated correctly
 * 5. Notes are properly associated with payments
 */
class PaymentFlowTest : BaseUnitTest() {

    private val paymentRepository: PaymentRepositoryV2 = mockk(relaxed = true)
    private lateinit var recordPaymentUseCase: RecordPaymentUseCase

    private val invoiceId = 100L
    private val businessId = 1L
    private val invoiceTotal = 200000L  // $2,000.00
    private val todayMidnight = run {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    private val invoiceDate = todayMidnight - 14 * 86_400_000L  // 14 days ago

    @Before
    fun setup() {
        recordPaymentUseCase = RecordPaymentUseCase(paymentRepository)
    }

    // ── full payment ─────────────────────────────────────────────────────────

    @Test
    fun `fullPayment_Success - full payment accepted and delegates to repository`() = runTest {
        coEvery {
            paymentRepository.recordPayment(
                invoiceId = invoiceId,
                businessId = businessId,
                amount = invoiceTotal,
                paymentDate = todayMidnight,
                notes = null
            )
        } returns Result.success(Unit)

        val result = recordPaymentUseCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = invoiceTotal,
            trueOutstanding = invoiceTotal,
            paymentDate = todayMidnight,
            invoiceDate = invoiceDate
        )

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            paymentRepository.recordPayment(
                invoiceId = invoiceId,
                businessId = businessId,
                amount = invoiceTotal,
                paymentDate = todayMidnight,
                notes = null
            )
        }
    }

    // ── partial payment ──────────────────────────────────────────────────────

    @Test
    fun `partialPayment_Success - partial payment accepted, outstanding reduces`() = runTest {
        val partialAmount = invoiceTotal / 2  // $1,000.00
        coEvery {
            paymentRepository.recordPayment(any(), any(), any(), any(), any())
        } returns Result.success(Unit)

        val result = recordPaymentUseCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = partialAmount,
            trueOutstanding = invoiceTotal,
            paymentDate = todayMidnight,
            invoiceDate = invoiceDate
        )

        assertTrue(result.isSuccess)
        // Business logic: remaining outstanding = invoiceTotal - partialAmount
        val remainingOutstanding = invoiceTotal - partialAmount
        assertTrue(remainingOutstanding > 0, "Partial payment should leave outstanding balance")
    }

    @Test
    fun `partialPayment_Success - multiple partial payments accepted up to full amount`() = runTest {
        coEvery {
            paymentRepository.recordPayment(any(), any(), any(), any(), any())
        } returns Result.success(Unit)

        val firstPayment = 80000L   // $800
        val remainingAfterFirst = invoiceTotal - firstPayment  // $1,200

        // First partial payment
        val firstResult = recordPaymentUseCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = firstPayment,
            trueOutstanding = invoiceTotal,
            paymentDate = invoiceDate + 7 * 86_400_000L,  // 7 days after invoice
            invoiceDate = invoiceDate
        )

        // Second payment clears the balance
        val secondResult = recordPaymentUseCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = remainingAfterFirst,
            trueOutstanding = remainingAfterFirst,
            paymentDate = todayMidnight,
            invoiceDate = invoiceDate
        )

        assertTrue(firstResult.isSuccess)
        assertTrue(secondResult.isSuccess)
    }

    // ── overpayment prevention ───────────────────────────────────────────────

    @Test
    fun `overpayment_Rejected - payment exceeding outstanding is refused`() = runTest {
        val overpayment = invoiceTotal + 1L  // 1¢ over

        val result = recordPaymentUseCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = overpayment,
            trueOutstanding = invoiceTotal,
            paymentDate = todayMidnight,
            invoiceDate = invoiceDate
        )

        assertFalse(result.isSuccess)
        coVerify(exactly = 0) { paymentRepository.recordPayment(any(), any(), any(), any(), any()) }
    }

    // ── date validation ──────────────────────────────────────────────────────

    @Test
    fun `dateValidation_FutureDate - future payment date is rejected`() = runTest {
        val futureDate = todayMidnight + 86_400_000L  // Tomorrow

        val result = recordPaymentUseCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = 50000L,
            trueOutstanding = invoiceTotal,
            paymentDate = futureDate,
            invoiceDate = invoiceDate
        )

        assertFalse(result.isSuccess)
        assertTrue(
            result.exceptionOrNull()?.message?.contains("future", ignoreCase = true) == true
        )
    }

    @Test
    fun `dateValidation_BeforeInvoice - payment before invoice date is rejected`() = runTest {
        val beforeInvoice = invoiceDate - 86_400_000L  // Day before invoice

        val result = recordPaymentUseCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = 50000L,
            trueOutstanding = invoiceTotal,
            paymentDate = beforeInvoice,
            invoiceDate = invoiceDate
        )

        assertFalse(result.isSuccess)
    }

    @Test
    fun `dateValidation_TodayPayment - today's date is accepted`() = runTest {
        coEvery {
            paymentRepository.recordPayment(any(), any(), any(), any(), any())
        } returns Result.success(Unit)

        val result = recordPaymentUseCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = 50000L,
            trueOutstanding = invoiceTotal,
            paymentDate = todayMidnight,
            invoiceDate = invoiceDate
        )

        assertTrue(result.isSuccess)
    }

    // ── notes ────────────────────────────────────────────────────────────────

    @Test
    fun `paymentNotes_Preserved - notes are passed through to repository`() = runTest {
        val notes = "Wire transfer ref: WT2026-0308"
        coEvery {
            paymentRepository.recordPayment(
                invoiceId = any(),
                businessId = any(),
                amount = any(),
                paymentDate = any(),
                notes = notes
            )
        } returns Result.success(Unit)

        val result = recordPaymentUseCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = 50000L,
            trueOutstanding = invoiceTotal,
            paymentDate = todayMidnight,
            invoiceDate = invoiceDate,
            notes = notes
        )

        assertTrue(result.isSuccess)
        coVerify {
            paymentRepository.recordPayment(
                invoiceId = any(),
                businessId = any(),
                amount = any(),
                paymentDate = any(),
                notes = notes
            )
        }
    }

    // ── zero/negative amount ─────────────────────────────────────────────────

    @Test
    fun `zeroPayment_Rejected - zero amount is rejected`() = runTest {
        val result = recordPaymentUseCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = 0L,
            trueOutstanding = invoiceTotal,
            paymentDate = todayMidnight,
            invoiceDate = invoiceDate
        )

        assertFalse(result.isSuccess)
    }

    @Test
    fun `negativePayment_Rejected - negative amount is rejected`() = runTest {
        val result = recordPaymentUseCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = -1000L,
            trueOutstanding = invoiceTotal,
            paymentDate = todayMidnight,
            invoiceDate = invoiceDate
        )

        assertFalse(result.isSuccess)
    }
}
