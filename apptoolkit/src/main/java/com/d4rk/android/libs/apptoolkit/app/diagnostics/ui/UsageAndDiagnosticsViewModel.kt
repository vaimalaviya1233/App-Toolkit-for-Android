package com.d4rk.android.libs.apptoolkit.app.diagnostics.ui

import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.app.diagnostics.domain.actions.UsageAndDiagnosticsAction
import com.d4rk.android.libs.apptoolkit.app.diagnostics.domain.actions.UsageAndDiagnosticsEvent
import com.d4rk.android.libs.apptoolkit.app.diagnostics.domain.model.ui.UiUsageAndDiagnosticsScreen
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.getData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.successData
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ConsentManagerHelper
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsageAndDiagnosticsViewModel(
    private val dataStore: CommonDataStore,
    private val configProvider: BuildInfoProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ScreenViewModel<UiUsageAndDiagnosticsScreen, UsageAndDiagnosticsEvent, UsageAndDiagnosticsAction>(
    initialState = UiStateScreen(
        data = UiUsageAndDiagnosticsScreen(
            usageAndDiagnostics = !configProvider.isDebugBuild,
            analyticsConsent = !configProvider.isDebugBuild,
            adStorageConsent = !configProvider.isDebugBuild,
            adUserDataConsent = !configProvider.isDebugBuild,
            adPersonalizationConsent = !configProvider.isDebugBuild,
        )
    ),
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
            combine(
                dataStore.usageAndDiagnostics(default = !configProvider.isDebugBuild),
                dataStore.analyticsConsent(default = !configProvider.isDebugBuild),
                dataStore.adStorageConsent(default = !configProvider.isDebugBuild),
                dataStore.adUserDataConsent(default = !configProvider.isDebugBuild),
                dataStore.adPersonalizationConsent(default = !configProvider.isDebugBuild),
            ) { usage, analytics, adStorage, adUserData, adPersonalization ->
                UiUsageAndDiagnosticsScreen(
                    usageAndDiagnostics = usage,
                    analyticsConsent = analytics,
                    adStorageConsent = adStorage,
                    adUserDataConsent = adUserData,
                    adPersonalizationConsent = adPersonalization,
                )
            }.collect { data ->
                screenState.successData { data }
            }
        }
    }

    private fun updateUsageAndDiagnostics(enabled: Boolean) {
        viewModelScope.launch {
            withContext(dispatcher) {
                dataStore.saveUsageAndDiagnostics(isChecked = enabled)
            }
        }
    }

    private fun updateAnalyticsConsent(granted: Boolean) {
        viewModelScope.launch {
            withContext(dispatcher) {
                dataStore.saveAnalyticsConsent(isGranted = granted)
            }
            updateAllConsents()
        }
    }

    private fun updateAdStorageConsent(granted: Boolean) {
        viewModelScope.launch {
            withContext(dispatcher) {
                dataStore.saveAdStorageConsent(isGranted = granted)
            }
            updateAllConsents()
        }
    }

    private fun updateAdUserDataConsent(granted: Boolean) {
        viewModelScope.launch {
            withContext(dispatcher) {
                dataStore.saveAdUserDataConsent(isGranted = granted)
            }
            updateAllConsents()
        }
    }

    private fun updateAdPersonalizationConsent(granted: Boolean) {
        viewModelScope.launch {
            withContext(dispatcher) {
                dataStore.saveAdPersonalizationConsent(isGranted = granted)
            }
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
