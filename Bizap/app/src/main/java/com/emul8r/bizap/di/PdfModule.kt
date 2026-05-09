package com.emul8r.bizap.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.emul8r.bizap.data.pdf.CanvasInvoiceTheme
import com.emul8r.bizap.data.pdf.HtmlPdfInvoiceTheme
import com.emul8r.bizap.data.pdf.InvoiceThemeManagerImpl
import com.emul8r.bizap.data.service.pdf.CssGenerator
import com.emul8r.bizap.data.service.pdf.PdfPreviewManager
import com.emul8r.bizap.data.service.pdf.LayoutSelector
import com.emul8r.bizap.data.service.pdf.PdfSettingsResolver
import com.emul8r.bizap.data.service.pdf_services.PdfCustomizationService
import com.emul8r.bizap.data.service.pdf_services.PdfDataSourceResolver
import com.emul8r.bizap.data.service.pdf_services.CssGenerator as CssGeneratorPdfServices
import com.emul8r.bizap.data.repository.InvoiceSettingsRepository
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.data.local.BusinessProfileDao
import com.emul8r.bizap.domain.pdf.InvoiceThemeManager
import javax.inject.Singleton

/**
 * Hilt Module for PDF services dependency injection.
 *
 * Provides all PDF-related dependencies including:
 * - Theme implementations (Canvas, HTML-to-PDF)
 * - Theme manager factory
 * - Phase 3B+3C PDF customization services (CSS, Preview, Production)
 * - Phase 3D PDF integration services (Settings, Layouts, Data Resolution)
 *
 * Dependencies are injected at the application singleton scope,
 * ensuring single instances across the app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object PdfModule {

    // ─────────────────────────────────────────────────────────
    // PHASE 3B: CSS & PREVIEW SERVICES
    // ─────────────────────────────────────────────────────────

    /**
     * Provide CSS Generator for dynamic CSS generation
     */
    @Provides
    @Singleton
    fun provideCssGenerator(
        @ApplicationContext context: Context
    ): CssGenerator = CssGenerator(context)

    /**
     * Provide PDF Preview Manager for preview PDF generation
     */
    @Provides
    @Singleton
    fun providePdfPreviewManager(
        pdfSettingsResolver: PdfSettingsResolver,
        cssGenerator: CssGenerator,
        layoutSelector: LayoutSelector
    ): PdfPreviewManager = PdfPreviewManager(pdfSettingsResolver, cssGenerator, layoutSelector)

    /**
     * Provide PDF Customization Service for production PDF generation
     */
    @Provides
    @Singleton
    fun providePdfCustomizationService(
        @ApplicationContext context: Context,
        cssGenerator: CssGeneratorPdfServices
    ): PdfCustomizationService = PdfCustomizationService(context, cssGenerator)

    // ─────────────────────────────────────────────────────────
    // PHASE 3D: SETTINGS & INTEGRATION SERVICES
    // ─────────────────────────────────────────────────────────

    /**
     * Provide PDF Settings Resolver for 3-tier settings hierarchy
     * Phase 3E: Fully integrated with repositories for data persistence
     */
    @Provides
    @Singleton
    fun providePdfSettingsResolver(
        invoiceSettingsRepository: InvoiceSettingsRepository,
        businessProfileRepository: BusinessProfileRepository,
        businessProfileDao: BusinessProfileDao
    ): PdfSettingsResolver = PdfSettingsResolver(
        invoiceSettingsRepository,
        businessProfileRepository,
        businessProfileDao
    )

    /**
     * Provide Layout Selector for smart layout selection
     */
    @Provides
    @Singleton
    fun provideLayoutSelector(): LayoutSelector = LayoutSelector()

    /**
     * Provide PDF Data Source Resolver for source-of-truth data resolution
     */
    @Provides
    @Singleton
    fun providePdfDataSourceResolver(): PdfDataSourceResolver = PdfDataSourceResolver()

    // ─────────────────────────────────────────────────────────
    // THEME SERVICES (EXISTING)
    // ─────────────────────────────────────────────────────────

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

