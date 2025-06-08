package com.d4rk.android.libs.apptoolkit.app.ads.ui

import com.d4rk.android.libs.apptoolkit.R
import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.app.ads.domain.actions.AdsSettingsActions
import com.d4rk.android.libs.apptoolkit.app.ads.domain.events.AdsSettingsEvents
import com.d4rk.android.libs.apptoolkit.app.ads.domain.model.AdsSettingsData
import com.d4rk.android.libs.apptoolkit.app.ads.domain.usecases.LoadConsentInfoUseCase
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.applyResult
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.d4rk.android.libs.apptoolkit.core.utils.extensions.loadAndShowIfNeeded
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.google.android.ump.ConsentInformation
import kotlinx.coroutines.flow.flowOn

class AdsSettingsViewModel(private val loadConsentInfoUseCase : LoadConsentInfoUseCase , private val dispatcherProvider : DispatcherProvider) : ScreenViewModel<AdsSettingsData , AdsSettingsEvents , AdsSettingsActions>(initialState = UiStateScreen(data = AdsSettingsData())) {

    init {
        onEvent(event = AdsSettingsEvents.LoadAdsSettings)
    }

    override fun onEvent(event : AdsSettingsEvents) {
        when (event) {
            is AdsSettingsEvents.LoadAdsSettings -> loadAdsSettings()
        }
    }

    private fun loadAdsSettings() {
        launch(context = dispatcherProvider.io) {
            loadConsentInfoUseCase().flowOn(dispatcherProvider.default).collect { result : DataState<ConsentInformation , Errors> ->
                screenState.applyResult(result = result , errorMessage = UiTextHelper.StringResource(R.string.error_failed_to_load_ads_settings)) { consentInfo , current ->
                    current.copy(consentInformation = consentInfo)
                }
            }
        }
    }
}