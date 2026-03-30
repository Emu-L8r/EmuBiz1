package com.emul8r.bizap.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.emul8r.bizap.data.pdf.CanvasInvoiceTheme
import com.emul8r.bizap.data.pdf.HtmlPdfInvoiceTheme
import com.emul8r.bizap.data.pdf.InvoiceThemeManagerImpl
import com.emul8r.bizap.domain.pdf.InvoiceThemeManager
import javax.inject.Singleton

/**
 * Hilt Module for PDF theme dependency injection.
 *
 * Provides all PDF-related dependencies including:
 * - Theme implementations (Canvas, HTML-to-PDF)
 * - Theme manager factory
 *
 * Dependencies are injected at the application singleton scope,
 * ensuring single instances across the app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object PdfModule {

    /**
     * Provide theme manager.
     *
     * CanvasInvoiceTheme and HtmlPdfInvoiceTheme are automatically injected
     * via constructor injection - no need for @Provides methods.
     */
    @Provides
    @Singleton
    fun provideThemeManager(
        canvasTheme: CanvasInvoiceTheme,
        htmlPdfTheme: HtmlPdfInvoiceTheme
    ): InvoiceThemeManager =
        InvoiceThemeManagerImpl(canvasTheme, htmlPdfTheme)

    /**
     * Provide the current user ID for dependency injection.
     * This is used by ViewModels that need user context.
     */
    @Provides
    @Singleton
    @javax.inject.Named("current_user_id")
    fun provideCurrentUserId(): String = "default_user"
}

