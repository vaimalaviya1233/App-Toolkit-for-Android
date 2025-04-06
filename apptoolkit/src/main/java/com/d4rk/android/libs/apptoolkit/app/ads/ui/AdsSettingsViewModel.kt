package com.d4rk.android.libs.apptoolkit.app.ads.ui

import com.d4rk.android.libs.apptoolkit.app.ads.domain.actions.AdsSettingsAction
import com.d4rk.android.libs.apptoolkit.app.ads.domain.actions.AdsSettingsEvent
import com.d4rk.android.libs.apptoolkit.app.ads.domain.model.AdsSettingsData
import com.d4rk.android.libs.apptoolkit.app.ads.domain.usecases.LoadConsentInfoUseCase
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.applyResult
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.successData
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class AdsSettingsViewModel(private val loadConsentInfoUseCase : LoadConsentInfoUseCase , private val dispatcherProvider : DispatcherProvider) : ScreenViewModel<AdsSettingsData , AdsSettingsEvent , AdsSettingsAction>(initialState = UiStateScreen(data = AdsSettingsData())) {

    init {
        onEvent(AdsSettingsEvent.LoadAdsSettings)
    }

    override fun onEvent(event : AdsSettingsEvent) {
        when (event) {
            is AdsSettingsEvent.LoadAdsSettings -> loadAdsSettings()
            is AdsSettingsEvent.AdsSettingChanged -> onAdsSettingChanged(event.isEnabled)
            is AdsSettingsEvent.OpenConsentForm -> openConsentForm(event.activity)
        }
    }

    private fun loadAdsSettings() {
        launch(dispatcherProvider.io) {
            loadConsentInfoUseCase().collect { result ->
                screenState.applyResult(result , "Failed to load ads settings") { consentInfo , current ->
                    current.copy(consentInformation = consentInfo)
                }
            }
        }
    }

    private fun onAdsSettingChanged(isEnabled : Boolean) {
        launch(dispatcherProvider.io) {
            screenState.successData {
                copy(adsEnabled = isEnabled)
            }
        }
    }

    private fun openConsentForm(activity : AdsSettingsActivity) {
        val consentInfo = screenData?.consentInformation
        if (consentInfo == null) {
            onEvent(AdsSettingsEvent.LoadAdsSettings)
            return
        }

        launch(dispatcherProvider.io) {
            val params = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()

            consentInfo.requestConsentInfoUpdate(activity , params , {
                UserMessagingPlatform.loadConsentForm(activity , { consentForm ->
                    if (consentInfo.consentStatus == ConsentInformation.ConsentStatus.REQUIRED || consentInfo.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
                        consentForm.show(activity) {
                            onEvent(AdsSettingsEvent.LoadAdsSettings)
                        }
                    }
                } , {})
            } , {})
        }
    }
}