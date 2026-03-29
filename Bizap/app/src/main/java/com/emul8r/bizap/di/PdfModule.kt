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
     * Provide Canvas theme implementation.
     */
    @Provides
    @Singleton
    fun provideCanvasTheme(
        theme: CanvasInvoiceTheme
    ): CanvasInvoiceTheme = theme

    /**
     * Provide HTML-to-PDF theme implementation.
     */
    @Provides
    @Singleton
    fun provideHtmlPdfTheme(
        theme: HtmlPdfInvoiceTheme
    ): HtmlPdfInvoiceTheme = theme

    /**
     * Provide theme manager factory.
     *
     * The theme manager handles theme selection and provides
     * appropriate theme renderers based on user settings.
     */
    @Provides
    @Singleton
    fun provideThemeManager(
        canvasTheme: CanvasInvoiceTheme,
        htmlPdfTheme: HtmlPdfInvoiceTheme
    ): InvoiceThemeManager =
        InvoiceThemeManagerImpl(canvasTheme, htmlPdfTheme)
}

