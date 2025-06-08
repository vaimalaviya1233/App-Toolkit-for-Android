package com.d4rk.android.libs.apptoolkit.app.oboarding.ui

import android.app.Activity
import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.ads.domain.usecases.LoadConsentInfoUseCase
import com.d4rk.android.libs.apptoolkit.app.oboarding.domain.actions.OnboardingAction
import com.d4rk.android.libs.apptoolkit.app.oboarding.domain.actions.OnboardingEvent
import com.d4rk.android.libs.apptoolkit.app.oboarding.domain.model.OnboardingUiData
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.applyResult
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.successData
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.d4rk.android.libs.apptoolkit.core.utils.extensions.loadAndShowIfNeeded
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.google.android.ump.ConsentInformation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class OnboardingViewModel(
    private val loadConsentInfoUseCase: LoadConsentInfoUseCase,
    private val dispatcherProvider: DispatcherProvider
) : ScreenViewModel<OnboardingUiData, OnboardingEvent, OnboardingAction>(
    initialState = UiStateScreen(data = OnboardingUiData())
) {

    init {
        onEvent(event = OnboardingEvent.LoadConsentInfo())
    }

    override fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.OpenConsentForm -> openConsentForm(activity = event.activity)
            is OnboardingEvent.LoadConsentInfo -> loadConsentInfo(activity = event.activity)
        }
    }

    private fun loadConsentInfo(activity: Activity? = null) {
        launch(context = dispatcherProvider.io) {
            loadConsentInfoUseCase().stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = DataState.Loading()
            ).collect { result: DataState<ConsentInformation, Errors> ->
                screenState.applyResult(
                    result = result,
                    errorMessage = UiTextHelper.StringResource(R.string.error_loading_consent_info)
                ) { consentInfo: ConsentInformation, current: OnboardingUiData ->
                    current.copy(
                        consentRequired = consentInfo.isConsentFormAvailable &&
                            (
                                consentInfo.consentStatus == ConsentInformation.ConsentStatus.REQUIRED ||
                                    consentInfo.consentStatus == ConsentInformation.ConsentStatus.UNKNOWN
                                ),
                        consentInformation = consentInfo
                    )
                }
                if (result is DataState.Success && activity != null) {
                    openConsentForm(activity = activity)
                }
            }
        }
    }

    private fun openConsentForm(activity: Activity) {
        screenData?.consentInformation.loadAndShowIfNeeded(
            scope = viewModelScope,
            dispatcherProvider = dispatcherProvider,
            activity = activity,
            onFormShown = { onConsentFormLoaded() },
        )
    }

    private fun onConsentFormLoaded() {
        launch {
            screenState.successData {
                copy(consentFormLoaded = true)
            }
        }
    }
}
