package com.d4rk.android.libs.apptoolkit.app.privacy.routes.ads.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.app.privacy.routes.ads.domain.model.AdsSettingsData
import com.d4rk.android.libs.apptoolkit.app.privacy.routes.ads.domain.usecases.LoadConsentInfoUseCase
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.setErrors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateState
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdsSettingsViewModel(configProvider : BuildInfoProvider , private val dataStore : CommonDataStore , private val loadConsentInfoUseCase : LoadConsentInfoUseCase , private val dispatcherProvider : DispatcherProvider) : ViewModel() {

    private val _screenState = MutableStateFlow(UiStateScreen(screenState = ScreenState.IsLoading() , data = AdsSettingsData(adsEnabled = ! configProvider.isDebugBuild , consentInformation = null)))
    val screenState : StateFlow<UiStateScreen<AdsSettingsData>> = _screenState.asStateFlow()

    init {
        loadAdsSettings()
    }

    private fun loadAdsSettings() {
        viewModelScope.launch(dispatcherProvider.io) {
            loadConsentInfoUseCase().collect { result ->
                when (result) {
                    is DataState.Success -> {
                        dataStore.ads.collect { isEnabled ->
                            _screenState.updateData(ScreenState.Success()) { current ->
                                current.copy(adsEnabled = isEnabled , consentInformation = result.data)
                            }
                        }
                    }

                    is DataState.Error -> {
                        _screenState.setErrors(
                            listOf(UiSnackbar(message = UiTextHelper.DynamicString("Failed to load ads settings")))
                        )
                        _screenState.updateState(ScreenState.Error())
                    }

                    else -> {}
                }
            }
        }
    }

    fun toggleAds(isChecked : Boolean) {
        viewModelScope.launch(dispatcherProvider.io) {
            dataStore.saveAds(isChecked)
            _screenState.updateData(ScreenState.Success()) { current ->
                current.copy(adsEnabled = isChecked)
            }
        }
    }

    fun openConsentForm(activity : AdsSettingsActivity) {
        _screenState.value.data?.consentInformation?.let { consentInfo ->
            UserMessagingPlatform.loadConsentForm(activity , { consentForm ->
                if (consentInfo.consentStatus == ConsentInformation.ConsentStatus.REQUIRED || consentInfo.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
                    consentForm.show(activity) {
                        loadAdsSettings()
                    }
                }
            } , {})
        }
    }
}
