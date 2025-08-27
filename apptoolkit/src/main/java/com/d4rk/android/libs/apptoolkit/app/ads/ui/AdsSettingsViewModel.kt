package com.d4rk.android.libs.apptoolkit.app.ads.ui

import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.app.ads.domain.actions.AdsSettingsAction
import com.d4rk.android.libs.apptoolkit.app.ads.domain.actions.AdsSettingsEvent
import com.d4rk.android.libs.apptoolkit.app.ads.domain.model.ui.UiAdsSettingsScreen
import com.d4rk.android.libs.apptoolkit.app.ads.domain.repository.AdsSettingsRepository
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.setLoading
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/** ViewModel for Ads settings screen. */
class AdsSettingsViewModel(
    private val repository: AdsSettingsRepository,
) : ScreenViewModel<UiAdsSettingsScreen, AdsSettingsEvent, AdsSettingsAction>(
    initialState = UiStateScreen(
        screenState = ScreenState.IsLoading(),
        data = UiAdsSettingsScreen()
    )
) {

    init {
        viewModelScope.launch {
            repository.observeAdsEnabled()
                .onStart { screenState.setLoading() }
                .catch {
                    screenState.updateData(newState = ScreenState.Error()) { current ->
                        current.copy(adsEnabled = repository.defaultAdsEnabled)
                    }
                }
                .collect { enabled ->
                    screenState.updateData(newState = ScreenState.Success()) { current ->
                        current.copy(adsEnabled = enabled)
                    }
                }
        }
    }

    override fun onEvent(event: AdsSettingsEvent) {
        when (event) {
            is AdsSettingsEvent.SetAdsEnabled -> setAdsEnabled(event.enabled)
        }
    }

    private fun setAdsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAdsEnabled(enabled)
        }
    }
}

