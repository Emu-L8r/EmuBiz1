package com.emul8r.bizap.domain.usecase.settings

import com.emul8r.bizap.domain.repository.SettingsRepository
import javax.inject.Inject

/** Persists notification preference changes. */
class UpdateNotificationSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    /** Toggle the master in-app notification switch. */
    suspend fun setNotificationsEnabled(enabled: Boolean) =
        repository.updateNotificationsEnabled(enabled)

    /** Toggle the e-mail reminder switch for overdue invoices. */
    suspend fun setEmailNotificationsEnabled(enabled: Boolean) =
        repository.updateEmailNotificationsEnabled(enabled)
}
