package com.emul8r.bizap.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.emul8r.bizap.domain.model.DashboardSettings
import com.emul8r.bizap.domain.repository.DashboardSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : DashboardSettingsRepository {

    private object Keys {
        val SHOW_REVENUE_TREND = booleanPreferencesKey("show_revenue_trend")
        val SHOW_MTD_CARD = booleanPreferencesKey("show_mtd_card")
        val SHOW_YTD_CARD = booleanPreferencesKey("show_ytd_card")
        val SHOW_TOTAL_CLIENTS_CARD = booleanPreferencesKey("show_total_clients_card")
        val SHOW_CURRENCY_BREAKDOWN = booleanPreferencesKey("show_currency_breakdown")
        val SHOW_RECENT_INVOICES = booleanPreferencesKey("show_recent_invoices")
    }

    override val settings: Flow<DashboardSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            DashboardSettings(
                showRevenueTrend = preferences[Keys.SHOW_REVENUE_TREND] ?: true,
                showMtdCard = preferences[Keys.SHOW_MTD_CARD] ?: true,
                showYtdCard = preferences[Keys.SHOW_YTD_CARD] ?: true,
                showTotalClientsCard = preferences[Keys.SHOW_TOTAL_CLIENTS_CARD] ?: true,
                showCurrencyBreakdown = preferences[Keys.SHOW_CURRENCY_BREAKDOWN] ?: true,
                showRecentInvoices = preferences[Keys.SHOW_RECENT_INVOICES] ?: true
            )
        }

    override suspend fun updateSettings(settings: DashboardSettings) {
        dataStore.edit { preferences ->
            preferences[Keys.SHOW_REVENUE_TREND] = settings.showRevenueTrend
            preferences[Keys.SHOW_MTD_CARD] = settings.showMtdCard
            preferences[Keys.SHOW_YTD_CARD] = settings.showYtdCard
            preferences[Keys.SHOW_TOTAL_CLIENTS_CARD] = settings.showTotalClientsCard
            preferences[Keys.SHOW_CURRENCY_BREAKDOWN] = settings.showCurrencyBreakdown
            preferences[Keys.SHOW_RECENT_INVOICES] = settings.showRecentInvoices
        }
    }
}
