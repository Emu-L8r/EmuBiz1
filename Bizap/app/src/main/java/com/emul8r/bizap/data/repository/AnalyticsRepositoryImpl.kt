package com.emul8r.bizap.data.repository

import com.emul8r.bizap.domain.repository.AnalyticsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data layer implementation of AnalyticsRepository domain interface.
 *
 * SPRINT 3: Created to satisfy Hilt DI and allow ViewModels to depend on
 * domain AnalyticsRepository interface instead of data AnalyticsDao directly.
 */
@Singleton
class AnalyticsRepositoryImpl @Inject constructor() : AnalyticsRepository {
    // Implementation methods will be added as needed
}

