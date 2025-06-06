package com.d4rk.android.libs.apptoolkit.app.startup.ui

import android.app.Activity
import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.ads.domain.usecases.LoadConsentInfoUseCase
import com.d4rk.android.libs.apptoolkit.app.startup.domain.actions.StartupAction
import com.d4rk.android.libs.apptoolkit.app.startup.domain.actions.StartupEvent
import com.d4rk.android.libs.apptoolkit.app.startup.domain.model.StartupUiData
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.applyResult
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.successData
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ConsentFormHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.google.android.ump.ConsentInformation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class StartupViewModel(private val loadConsentInfoUseCase : LoadConsentInfoUseCase , private val dispatcherProvider : DispatcherProvider) : ScreenViewModel<StartupUiData , StartupEvent , StartupAction>(initialState = UiStateScreen(data = StartupUiData())) {

    init {
        onEvent(event = StartupEvent.LoadConsentInfo)
    }

    override fun onEvent(event : StartupEvent) {
        when (event) {
            is StartupEvent.OpenConsentForm -> openConsentForm(activity = event.activity)
            is StartupEvent.LoadConsentInfo -> loadConsentInfo()
        }
    }

    private fun loadConsentInfo() {
        launch(context = dispatcherProvider.io) {
            loadConsentInfoUseCase().stateIn(scope = viewModelScope , started = SharingStarted.Lazily , initialValue = DataState.Loading()).collect { result : DataState<ConsentInformation , Errors> ->
                screenState.applyResult(result = result , errorMessage = UiTextHelper.StringResource(R.string.error_loading_consent_info)) { consentInfo : ConsentInformation , current : StartupUiData ->
                    current.copy(
                        consentRequired = consentInfo.isConsentFormAvailable &&
                            (consentInfo.consentStatus == ConsentInformation.ConsentStatus.REQUIRED ||
                                consentInfo.consentStatus == ConsentInformation.ConsentStatus.UNKNOWN),
                        consentInformation = consentInfo
                    )
                }
            }
        }
    }

    private fun openConsentForm(activity : Activity) {
        screenData?.consentInformation?.let { consentInfo : ConsentInformation ->
            launch(context = dispatcherProvider.io) {
                ConsentFormHelper.loadAndShow(activity = activity , consentInfo = consentInfo) {
                    onConsentFormLoaded()
                }
            }
        } ?: return onConsentFormLoaded()
    }

    private fun onConsentFormLoaded() {
        launch {
            screenState.successData {
                copy(consentFormLoaded = true)
            }
        }
    }
}