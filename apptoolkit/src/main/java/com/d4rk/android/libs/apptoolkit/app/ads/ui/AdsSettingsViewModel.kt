package com.d4rk.android.libs.apptoolkit.app.ads.ui

import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.ads.domain.actions.AdsSettingsAction
import com.d4rk.android.libs.apptoolkit.app.ads.domain.actions.AdsSettingsEvent
import com.d4rk.android.libs.apptoolkit.app.ads.domain.model.AdsSettingsData
import com.d4rk.android.libs.apptoolkit.app.ads.domain.usecases.LoadConsentInfoUseCase
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.applyResult
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.showSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.successData
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.ScreenMessageType
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class AdsSettingsViewModel(private val loadConsentInfoUseCase : LoadConsentInfoUseCase , private val dispatcherProvider : DispatcherProvider) : ScreenViewModel<AdsSettingsData , AdsSettingsEvent , AdsSettingsAction>(initialState = UiStateScreen(data = AdsSettingsData())) {

    init {
        onEvent(event = AdsSettingsEvent.LoadAdsSettings)
    }

    override fun onEvent(event : AdsSettingsEvent) {
        when (event) {
            is AdsSettingsEvent.LoadAdsSettings -> loadAdsSettings()
            is AdsSettingsEvent.AdsSettingChanged -> onAdsSettingChanged(isEnabled = event.isEnabled)
            is AdsSettingsEvent.OpenConsentForm -> openConsentForm(activity = event.activity)
        }
    }

    private fun loadAdsSettings() {
        launch(context = dispatcherProvider.io) {
            loadConsentInfoUseCase().stateIn(scope = viewModelScope , started = SharingStarted.Lazily , initialValue = DataState.Loading()).collect { result : DataState<ConsentInformation , Errors> ->
                screenState.applyResult(result = result , errorMessage = UiTextHelper.StringResource(R.string.error_failed_to_load_ads_settings)) { consentInfo : ConsentInformation , current : AdsSettingsData ->
                    current.copy(consentInformation = consentInfo)
                }
            }
        }
    }

    private fun onAdsSettingChanged(isEnabled : Boolean) {
        launch(context = dispatcherProvider.io) {
            screenState.successData {
                copy(adsEnabled = isEnabled)
            }
            val message : UiTextHelper.StringResource = UiTextHelper.StringResource(resourceId = if (isEnabled) R.string.ads_enabled else R.string.ads_disabled)
            screenState.showSnackbar(UiSnackbar(message = message , type = ScreenMessageType.SNACKBAR , isError = false))
        }
    }


    private fun openConsentForm(activity : AdsSettingsActivity) {
        screenData?.consentInformation?.let { consentInfo : ConsentInformation ->
            launch(context = dispatcherProvider.io) {
                val params : ConsentRequestParameters = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()

                consentInfo.requestConsentInfoUpdate(activity , params , {
                    UserMessagingPlatform.loadConsentForm(activity , { consentForm : ConsentForm ->
                        if (consentInfo.consentStatus == ConsentInformation.ConsentStatus.REQUIRED || consentInfo.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
                            consentForm.show(activity) {
                                onEvent(event = AdsSettingsEvent.LoadAdsSettings)
                            }
                        }
                    } , {})
                } , {})
            }
        } ?: return onEvent(event = AdsSettingsEvent.LoadAdsSettings)
    }
}