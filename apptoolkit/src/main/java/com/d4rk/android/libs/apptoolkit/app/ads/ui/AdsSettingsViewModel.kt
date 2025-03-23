package com.d4rk.android.libs.apptoolkit.app.ads.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.app.ads.domain.actions.AdsSettingsEvent
import com.d4rk.android.libs.apptoolkit.app.ads.domain.model.AdsSettingsData
import com.d4rk.android.libs.apptoolkit.app.ads.domain.usecases.LoadConsentInfoUseCase
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.setErrors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateState
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdsSettingsViewModel(private val loadConsentInfoUseCase : LoadConsentInfoUseCase , private val dispatcherProvider : DispatcherProvider) : ViewModel() {

    private val _screenState : MutableStateFlow<UiStateScreen<AdsSettingsData>> = MutableStateFlow(value = UiStateScreen(screenState = ScreenState.IsLoading() , data = AdsSettingsData()))
    val screenState : StateFlow<UiStateScreen<AdsSettingsData>> = _screenState.asStateFlow()

    init {
        sendEvent(event = AdsSettingsEvent.LoadAdsSettings)
    }

    fun sendEvent(event : AdsSettingsEvent) {
        when (event) {
            AdsSettingsEvent.LoadAdsSettings -> loadAdsSettings()
            is AdsSettingsEvent.AdsSettingChanged -> onAdsSettingChanged(isEnabled = event.isEnabled)
            is AdsSettingsEvent.OpenConsentForm -> openConsentForm(activity = event.activity)
        }
    }

    private fun loadAdsSettings() {
        viewModelScope.launch() {
            loadConsentInfoUseCase().flowOn(context = dispatcherProvider.io).stateIn(scope = viewModelScope , started = SharingStarted.Lazily , initialValue = DataState.Loading()).collect { result ->
                when (result) {
                    is DataState.Success -> {
                        _screenState.updateData(newDataState = ScreenState.Success()) { current ->
                            current.copy(consentInformation = result.data)
                        }
                    }

                    is DataState.Error -> {
                        _screenState.setErrors(
                            errors = listOf(element = UiSnackbar(message = UiTextHelper.DynamicString(content = "Failed to load ads settings")))
                        )
                        _screenState.updateState(newValues = ScreenState.Error())
                    }

                    else -> {}
                }
            }
        }
    }

    private fun onAdsSettingChanged(isEnabled : Boolean) {
        _screenState.updateData(newDataState = ScreenState.Success()) { current : AdsSettingsData ->
            current.copy(adsEnabled = isEnabled)
        }
    }

    private fun openConsentForm(activity : AdsSettingsActivity) {
        val consentInfo = _screenState.value.data?.consentInformation
        if (consentInfo == null) {
            sendEvent(event = AdsSettingsEvent.LoadAdsSettings)
            return
        }

        val params : ConsentRequestParameters = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()

        consentInfo.requestConsentInfoUpdate(activity , params , {
            UserMessagingPlatform.loadConsentForm(activity , { consentForm ->
                if (consentInfo.consentStatus == ConsentInformation.ConsentStatus.REQUIRED || consentInfo.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
                    consentForm.show(activity) {
                        sendEvent(event = AdsSettingsEvent.LoadAdsSettings)
                    }
                }
            } , {})
        } , {})
    }
}