package com.d4rk.android.libs.apptoolkit.app.ads.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.app.ads.domain.repository.AdsSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** ViewModel for Ads settings screen. */
class AdsSettingsViewModel(
    private val repository: AdsSettingsRepository,
) : ViewModel() {

    val adsEnabled: StateFlow<Boolean> = repository.observeAdsEnabled()
        .catch { emit(repository.defaultAdsEnabled) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = repository.defaultAdsEnabled,
        )

    fun setAdsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAdsEnabled(enabled)
        }
    }
}
