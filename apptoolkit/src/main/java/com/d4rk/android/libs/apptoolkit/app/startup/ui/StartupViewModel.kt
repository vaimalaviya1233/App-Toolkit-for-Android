package com.d4rk.android.libs.apptoolkit.app.startup.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.app.ads.domain.usecases.LoadConsentInfoUseCase
import com.d4rk.android.libs.apptoolkit.app.startup.domain.actions.StartupEvent
import com.d4rk.android.libs.apptoolkit.app.startup.domain.model.StartupUiData
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.setErrors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.setLoading
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
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

class StartupViewModel(private val loadConsentInfoUseCase : LoadConsentInfoUseCase , private val dispatcherProvider : DispatcherProvider) : ViewModel() {

    private val _screenState = MutableStateFlow(value = UiStateScreen(data = StartupUiData()))
    val screenState : StateFlow<UiStateScreen<StartupUiData>> = _screenState.asStateFlow()

    init {
        loadConsentInfo()
    }

    fun sendEvent(event : StartupEvent , activity : Activity) {
        when (event) {
            StartupEvent.OpenConsentForm -> openConsentForm(activity = activity)
        }
    }

    private fun loadConsentInfo() {
        viewModelScope.launch {
            loadConsentInfoUseCase().flowOn(context = dispatcherProvider.io).stateIn(scope = viewModelScope , started = SharingStarted.Lazily , initialValue = DataState.Loading()).collect { result ->
                when (result) {
                    is DataState.Success -> {
                        val consentInfo : ConsentInformation = result.data
                        if (consentInfo.isConsentFormAvailable && consentInfo.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                            _screenState.updateData(newDataState = ScreenState.Success()) { it.copy(consentRequired = true) }
                        }
                        else {
                            _screenState.updateData(newDataState = ScreenState.Success()) { it.copy(consentRequired = false) }
                        }
                    }

                    is DataState.Error -> {
                        _screenState.setErrors(errors = listOf(UiSnackbar(message = UiTextHelper.DynamicString(content = result.error.toString()))))
                    }

                    else -> _screenState.setLoading()
                }
            }
        }
    }

    private fun openConsentForm(activity : Activity) {
        val consentInfo = _screenState.value.data?.consentInformation
        if (consentInfo == null) {
            loadConsentInfo()
            return
        }

        val params : ConsentRequestParameters = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()

        consentInfo.requestConsentInfoUpdate(activity , params , {
            UserMessagingPlatform.loadConsentForm(activity , { consentForm ->
                if (consentInfo.consentStatus == ConsentInformation.ConsentStatus.REQUIRED || consentInfo.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
                    consentForm.show(activity) {
                        onConsentFormLoaded()
                    }
                }
            } , {
                onConsentFormLoaded()
            })
        } , {
            onConsentFormLoaded()
        })
    }

    private fun onConsentFormLoaded() {
        viewModelScope.launch {
            _screenState.updateData(newDataState = _screenState.value.screenState) { it.copy(consentFormLoaded = true) }
        }
    }
}