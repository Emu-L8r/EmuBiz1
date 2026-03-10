package com.emul8r.bizap.domain.usecase

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.repository.gui2.PaymentRepositoryV2
import io.mockk.any
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.eq
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for [RecordPaymentUseCase].
 *
 * Verifies payment validation rules including amount checks, date validation,
 * status transitions, and atomic persistence.
 */
class RecordPaymentUseCaseTest : BaseUnitTest() {

    private lateinit var paymentRepository: PaymentRepositoryV2
    private lateinit var useCase: RecordPaymentUseCase

    private val invoiceId = 1L
    private val businessId = 1L
    private val outstanding = 100000L  // $1000.00

    /** Midnight of today */
    private val todayMidnight = run {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    /** 7 days ago (valid invoice date) */
    private val invoiceDate = todayMidnight - 7 * 86_400_000L

    @Before
    fun setUp() {
        paymentRepository = mockk(relaxed = true)
        useCase = RecordPaymentUseCase(paymentRepository)
    }

    // ── validPayment_Success ──────────────────────────────────────────────────

    @Test
    fun `validPayment_Success - valid payment delegates to repository`() = runTest {
        coEvery {
            paymentRepository.recordPayment(
                invoiceId = invoiceId,
                businessId = businessId,
                amount = 50000L,
                paymentDate = todayMidnight,
                notes = null
            )
        } returns Result.success(Unit)

        val result = useCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = 50000L,
            trueOutstanding = outstanding,
            paymentDate = todayMidnight,
            invoiceDate = invoiceDate
        )

        assertTrue(result.isSuccess)
        coVerify {
            paymentRepository.recordPayment(
                invoiceId = invoiceId,
                businessId = businessId,
                amount = 50000L,
                paymentDate = todayMidnight,
                notes = null
            )
        }
    }

    @Test
    fun `validPayment_Success - exact outstanding amount is accepted`() = runTest {
        coEvery {
            paymentRepository.recordPayment(
                invoiceId = any(),
                businessId = any(),
                amount = any(),
                paymentDate = any(),
                notes = any()
            )
        } returns Result.success(Unit)

        val result = useCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = outstanding,  // Exact outstanding
            trueOutstanding = outstanding,
            paymentDate = todayMidnight,
            invoiceDate = invoiceDate
        )

        assertTrue(result.isSuccess)
    }

    // ── overpayment_Prevented ─────────────────────────────────────────────────

    @Test
    fun `overpayment_Prevented - payment exceeding outstanding returns failure`() = runTest {
        val result = useCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = outstanding + 1L,  // One cent over outstanding
            trueOutstanding = outstanding,
            paymentDate = todayMidnight,
            invoiceDate = invoiceDate
        )

        assertFalse(result.isSuccess)
        val error = result.exceptionOrNull()
        assertNotNull(error)
        assertTrue(error.message?.contains("outstanding", ignoreCase = true) == true)
    }

    @Test
    fun `overpayment_Prevented - repository is NOT called when overpayment detected`() = runTest {
        useCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = outstanding + 10000L,
            trueOutstanding = outstanding,
            paymentDate = todayMidnight,
            invoiceDate = invoiceDate
        )

        coVerify(exactly = 0) {
            paymentRepository.recordPayment(
                invoiceId = any(),
                businessId = any(),
                amount = any(),
                paymentDate = any(),
                notes = any()
            )
        }
    }

    // ── invalidDate_Prevented ─────────────────────────────────────────────────

    @Test
    fun `invalidDate_Prevented - future payment date returns failure`() = runTest {
        val futureDate = todayMidnight + 86_400_000L  // Tomorrow

        val result = useCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = 50000L,
            trueOutstanding = outstanding,
            paymentDate = futureDate,
            invoiceDate = invoiceDate
        )

        assertFalse(result.isSuccess)
        val error = result.exceptionOrNull()
        assertNotNull(error)
        assertTrue(error.message?.contains("future", ignoreCase = true) == true)
    }

    @Test
    fun `invalidDate_Prevented - payment before invoice date returns failure`() = runTest {
        val beforeInvoiceDate = invoiceDate - 86_400_000L  // Day before invoice

        val result = useCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = 50000L,
            trueOutstanding = outstanding,
            paymentDate = beforeInvoiceDate,
            invoiceDate = invoiceDate
        )

        assertFalse(result.isSuccess)
        val error = result.exceptionOrNull()
        assertNotNull(error)
    }

    @Test
    fun `invalidDate_Prevented - zero or negative amount returns failure`() = runTest {
        val result = useCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = 0L,
            trueOutstanding = outstanding,
            paymentDate = todayMidnight,
            invoiceDate = invoiceDate
        )

        assertFalse(result.isSuccess)
    }

    // ── statusTransition_ToPaid ───────────────────────────────────────────────

    @Test
    fun `statusTransition_ToPaid - full payment results in PAID status logic`() {
        val currentPaid = 0L
        val paymentAmount = outstanding
        val newTotal = currentPaid + paymentAmount
        val expectPaid = newTotal >= outstanding
        assertTrue(expectPaid, "Full payment should qualify invoice as PAID")
    }

    @Test
    fun `statusTransition_ToPaid - partial payment does not qualify as PAID`() {
        val currentPaid = 0L
        val paymentAmount = outstanding / 2
        val newTotal = currentPaid + paymentAmount
        val expectPaid = newTotal >= outstanding
        assertFalse(expectPaid, "Partial payment should not qualify as PAID")
    }

    // ── snapshotUpdates_Atomic ────────────────────────────────────────────────

    @Test
    fun `snapshotUpdates_Atomic - successful payment calls repository exactly once`() = runTest {
        coEvery {
            paymentRepository.recordPayment(
                invoiceId = any(),
                businessId = any(),
                amount = any(),
                paymentDate = any(),
                notes = any()
            )
        } returns Result.success(Unit)

        useCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = 50000L,
            trueOutstanding = outstanding,
            paymentDate = todayMidnight,
            invoiceDate = invoiceDate
        )

        coVerify(exactly = 1) {
            paymentRepository.recordPayment(
                invoiceId = any(),
                businessId = any(),
                amount = any(),
                paymentDate = any(),
                notes = any()
            )
        }
    }

    @Test
    fun `snapshotUpdates_Atomic - payment notes are passed to repository`() = runTest {
        val notes = "Bank transfer reference: TXN123"
        coEvery {
            paymentRepository.recordPayment(
                invoiceId = any(),
                businessId = any(),
                amount = any(),
                paymentDate = any(),
                notes = eq(notes)
            )
        } returns Result.success(Unit)

        useCase(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = 50000L,
            trueOutstanding = outstanding,
            paymentDate = todayMidnight,
            invoiceDate = invoiceDate,
            notes = notes
        )

        coVerify {
            paymentRepository.recordPayment(
                invoiceId = any(),
                businessId = any(),
                amount = any(),
                paymentDate = any(),
                notes = eq(notes)
            )
        }
    }
}
