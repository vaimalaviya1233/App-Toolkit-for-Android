package com.d4rk.android.libs.apptoolkit.app.diagnostics.ui

import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.app.diagnostics.domain.actions.UsageAndDiagnosticsAction
import com.d4rk.android.libs.apptoolkit.app.diagnostics.domain.actions.UsageAndDiagnosticsEvent
import com.d4rk.android.libs.apptoolkit.app.diagnostics.domain.model.ui.UiUsageAndDiagnosticsScreen
import com.d4rk.android.libs.apptoolkit.app.diagnostics.domain.repository.UsageAndDiagnosticsRepository
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.getData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.successData
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ConsentManagerHelper
import kotlinx.coroutines.launch

class UsageAndDiagnosticsViewModel(
    private val repository: UsageAndDiagnosticsRepository,
) : ScreenViewModel<UiUsageAndDiagnosticsScreen, UsageAndDiagnosticsEvent, UsageAndDiagnosticsAction>(
    initialState = UiStateScreen(data = UiUsageAndDiagnosticsScreen()),
) {

    init {
        observeConsents()
    }

    override fun onEvent(event: UsageAndDiagnosticsEvent) {
        when (event) {
            is UsageAndDiagnosticsEvent.SetUsageAndDiagnostics -> updateUsageAndDiagnostics(event.enabled)
            is UsageAndDiagnosticsEvent.SetAnalyticsConsent -> updateAnalyticsConsent(event.granted)
            is UsageAndDiagnosticsEvent.SetAdStorageConsent -> updateAdStorageConsent(event.granted)
            is UsageAndDiagnosticsEvent.SetAdUserDataConsent -> updateAdUserDataConsent(event.granted)
            is UsageAndDiagnosticsEvent.SetAdPersonalizationConsent -> updateAdPersonalizationConsent(event.granted)
        }
    }

    private fun observeConsents() {
        viewModelScope.launch {
            repository.observeSettings().collect { settings ->
                screenState.successData {
                    UiUsageAndDiagnosticsScreen(
                        usageAndDiagnostics = settings.usageAndDiagnostics,
                        analyticsConsent = settings.analyticsConsent,
                        adStorageConsent = settings.adStorageConsent,
                        adUserDataConsent = settings.adUserDataConsent,
                        adPersonalizationConsent = settings.adPersonalizationConsent,
                    )
                }
            }
        }
    }

    private fun updateUsageAndDiagnostics(enabled: Boolean) {
        viewModelScope.launch { repository.setUsageAndDiagnostics(enabled) }
    }

    private fun updateAnalyticsConsent(granted: Boolean) {
        viewModelScope.launch {
            repository.setAnalyticsConsent(granted)
            updateAllConsents()
        }
    }

    private fun updateAdStorageConsent(granted: Boolean) {
        viewModelScope.launch {
            repository.setAdStorageConsent(granted)
            updateAllConsents()
        }
    }

    private fun updateAdUserDataConsent(granted: Boolean) {
        viewModelScope.launch {
            repository.setAdUserDataConsent(granted)
            updateAllConsents()
        }
    }

    private fun updateAdPersonalizationConsent(granted: Boolean) {
        viewModelScope.launch {
            repository.setAdPersonalizationConsent(granted)
            updateAllConsents()
        }
    }

    private fun updateAllConsents() {
        val data = screenState.getData()
        ConsentManagerHelper.updateConsent(
            analyticsGranted = data.analyticsConsent,
            adStorageGranted = data.adStorageConsent,
            adUserDataGranted = data.adUserDataConsent,
            adPersonalizationGranted = data.adPersonalizationConsent,
        )
    }
}
