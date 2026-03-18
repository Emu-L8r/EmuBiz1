package com.emul8r.bizap.domain.model

import kotlinx.serialization.Serializable

/**
 * Aggregate of all user preferences managed by [com.emul8r.bizap.domain.repository.SettingsRepository].
 *
 * All fields have sensible defaults so a freshly-installed app works without any explicit
 * configuration.  The class is [@Serializable] so it can be stored via DataStore or passed
 * between processes if needed.
 */
@Serializable
data class Settings(
    /** Light / Dark / follow system. */
    val themePreference: ThemePreference = ThemePreference.AUTO,
    /** How list screens (invoices, customers) are laid out. */
    val displayMode: DisplayMode = DisplayMode.LIST_VIEW,
    /** Default status filter applied when opening the invoice list. */
    val defaultInvoiceStatusFilter: String = "ALL",
    /** Number of days the invoice list looks back by default. */
    val defaultDaysLookback: Int = 30,
    /** Visual density of UI cards and list rows. */
    val uiDensity: UiDensity = UiDensity.COMFORTABLE,
    /** Master switch for all in-app notifications. */
    val notificationsEnabled: Boolean = true,
    /** Whether to send reminder e-mails for overdue invoices. */
    val emailNotificationsEnabled: Boolean = true,
    /** ISO 4217 currency code for display (e.g. "USD", "GBP"). */
    val currencyCode: String = "USD",
    /** BCP-47 language tag used for locale-sensitive formatting. */
    val localeLanguage: String = "en",
    /** Whether the app should automatically sync data in the background. */
    val autoSyncEnabled: Boolean = true,
    /** How often background sync should run (minutes). */
    val syncFrequencyMinutes: Int = 15,
    /** Epoch-millis timestamp of the last settings write. */
    val lastUpdated: Long = 0
)
