package com.emul8r.bizap.di

import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.local.dao.PaymentDaoV2
import com.emul8r.bizap.data.repository.gui2.BusinessContextRepositoryV2
import com.emul8r.bizap.data.repository.gui2.PaymentAnalyticsRepositoryV2
import com.emul8r.bizap.data.repository.gui2.PaymentRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RevenueRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RiskAnalyticsRepositoryV2
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for all GUI2 dependencies.
 * Provides repositories used by GUI2 ViewModels.
 */
@Module
@InstallIn(SingletonComponent::class)
object GuiV2Module {

    @Provides
    @Singleton
    fun provideRevenueRepositoryV2(invoiceDaoV2: InvoiceDaoV2): RevenueRepositoryV2 =
        RevenueRepositoryV2(invoiceDaoV2)

    @Provides
    @Singleton
    fun providePaymentAnalyticsRepositoryV2(invoiceDaoV2: InvoiceDaoV2): PaymentAnalyticsRepositoryV2 =
        PaymentAnalyticsRepositoryV2(invoiceDaoV2)

    @Provides
    @Singleton
    fun provideRiskAnalyticsRepositoryV2(invoiceDaoV2: InvoiceDaoV2): RiskAnalyticsRepositoryV2 =
        RiskAnalyticsRepositoryV2(invoiceDaoV2)

    @Provides
    @Singleton
    fun provideBusinessContextRepositoryV2(
        businessProfileRepository: BusinessProfileRepository
    ): BusinessContextRepositoryV2 =
        BusinessContextRepositoryV2(businessProfileRepository)

    @Provides
    @Singleton
    fun providePaymentRepositoryV2(
        database: AppDatabase,
        invoiceDaoV2: InvoiceDaoV2,
        paymentDaoV2: PaymentDaoV2
    ): PaymentRepositoryV2 = PaymentRepositoryV2(database, invoiceDaoV2, paymentDaoV2)
}
