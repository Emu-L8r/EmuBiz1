package com.emul8r.bizap.di

import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.dao.PaymentDaoV2
import com.emul8r.bizap.data.repository.SnapshotSyncHelper
import com.emul8r.bizap.data.repository.analytics.AnalyticsCalculator
import com.emul8r.bizap.data.repository.analytics.AnalyticsDiagnostics
import com.emul8r.bizap.data.repository.analytics.AnalyticsEventBus
import com.emul8r.bizap.data.repository.analytics.AnalyticsValidator
import com.emul8r.bizap.data.repository.gui2.BusinessContextRepositoryV2
import com.emul8r.bizap.data.repository.gui2.PaymentAnalyticsRepositoryV2
import com.emul8r.bizap.data.repository.gui2.PaymentRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RiskAnalyticsRepositoryV2
import com.emul8r.bizap.domain.payment.repository.PaymentRepository
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for all GUI2 dependencies.
 * Provides repositories used by GUI2 ViewModels.
 *
 * SPRINT 3: Simplified - only necessary bindings.
 * Removed SnapshotSyncRepository and AnalyticsRepository (unnecessary abstractions).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class GuiV2Module {

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(impl: PaymentRepositoryV2): PaymentRepository

    companion object {

        @Provides
        @Singleton
        fun provideAnalyticsValidator(): AnalyticsValidator = AnalyticsValidator()

        @Provides
        @Singleton
        fun provideAnalyticsCalculator(): AnalyticsCalculator = AnalyticsCalculator()

        @Provides
        @Singleton
        fun provideAnalyticsEventBus(): AnalyticsEventBus = AnalyticsEventBus()

        @Provides
        @Singleton
        fun provideAnalyticsDiagnostics(
            invoiceDaoV2: InvoiceDaoV2,
            calculator: AnalyticsCalculator,
            validator: AnalyticsValidator
        ): AnalyticsDiagnostics = AnalyticsDiagnostics(invoiceDaoV2, calculator, validator)

        @Provides
        @Singleton
        fun providePaymentAnalyticsRepositoryV2(
            invoiceDaoV2: InvoiceDaoV2,
            calculator: AnalyticsCalculator,
            validator: AnalyticsValidator
        ): PaymentAnalyticsRepositoryV2 = PaymentAnalyticsRepositoryV2(invoiceDaoV2, calculator, validator)

        @Provides
        @Singleton
        fun provideRiskAnalyticsRepositoryV2(
            invoiceDaoV2: InvoiceDaoV2,
            calculator: AnalyticsCalculator
        ): RiskAnalyticsRepositoryV2 = RiskAnalyticsRepositoryV2(invoiceDaoV2, calculator)

        @Provides
        @Singleton
        fun provideBusinessContextRepositoryV2(
            businessProfileRepository: BusinessProfileRepository,
            invoiceDaoV2: InvoiceDaoV2
        ): BusinessContextRepositoryV2 =
            BusinessContextRepositoryV2(businessProfileRepository, invoiceDaoV2)

        @Provides
        @Singleton
        fun providePaymentRepositoryV2(
            database: AppDatabase,
            invoiceDaoV2: InvoiceDaoV2,
            paymentDaoV2: PaymentDaoV2,
            snapshotSyncHelper: SnapshotSyncHelper
        ): PaymentRepositoryV2 = PaymentRepositoryV2(database, invoiceDaoV2, paymentDaoV2, snapshotSyncHelper)
    }
}
