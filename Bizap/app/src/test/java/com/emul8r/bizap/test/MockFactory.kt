package com.emul8r.bizap.test

import io.mockk.mockk
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.repository.ThemeRepository
import com.emul8r.bizap.domain.repository.AnalyticsRepository
import com.emul8r.bizap.domain.repository.DocumentRepository

/**
 * Factory for creating mocks of repository interfaces.
 * Use these in ViewModel tests to isolate UI logic from data layer.
 */
object MockFactory {

    /**
     * Creates a relaxed mock of InvoiceRepository.
     * Relaxed mocks return default values for any method call.
     */
    fun createMockInvoiceRepository() = mockk<InvoiceRepository>(relaxed = true)

    /**
     * Creates a relaxed mock of ThemeRepository.
     */
    fun createMockThemeRepository() = mockk<ThemeRepository>(relaxed = true)

    /**
     * Creates a relaxed mock of AnalyticsRepository.
     */
    fun createMockAnalyticsRepository() = mockk<AnalyticsRepository>(relaxed = true)

    /**
     * Creates a relaxed mock of DocumentRepository.
     */
    fun createMockDocumentRepository() = mockk<DocumentRepository>(relaxed = true)
}





