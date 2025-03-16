package com.d4rk.android.libs.apptoolkit.app.help.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.app.help.domain.actions.HelpAction
import com.d4rk.android.libs.apptoolkit.app.help.domain.model.ui.UiHelpQuestion
import com.d4rk.android.libs.apptoolkit.app.help.domain.model.ui.UiHelpScreen
import com.d4rk.android.libs.apptoolkit.app.help.domain.usecases.GetFAQsUseCase
import com.d4rk.android.libs.apptoolkit.app.help.domain.usecases.LaunchReviewFlowUseCase
import com.d4rk.android.libs.apptoolkit.app.help.domain.usecases.RequestReviewFlowUseCase
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateState
import com.google.android.play.core.review.ReviewInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HelpViewModel(private val getFAQsUseCase : GetFAQsUseCase , private val requestReviewFlowUseCase : RequestReviewFlowUseCase , private val launchReviewFlowUseCase : LaunchReviewFlowUseCase , private val dispatcherProvider : DispatcherProvider) : ViewModel() {

    private val _screenState : MutableStateFlow<UiStateScreen<UiHelpScreen>> = MutableStateFlow(value = UiStateScreen(screenState = ScreenState.IsLoading() , data = UiHelpScreen()))
    val screenState : StateFlow<UiStateScreen<UiHelpScreen>> = _screenState.asStateFlow()

    init {
        sendEvent(HelpAction.LoadHelp)
    }

    fun sendEvent(event : HelpAction) {
        when (event) {
            HelpAction.LoadHelp -> loadHelpData()
            HelpAction.RequestReview -> requestReviewFlow()
            is HelpAction.LaunchReviewFlow -> launchReviewFlow(event.activity , event.reviewInfo)
        }
    }

    private fun loadHelpData() {
        viewModelScope.launch(dispatcherProvider.io) {
            val faqResult = getFAQsUseCase().flowOn(dispatcherProvider.io).first()
            val reviewResult = requestReviewFlowUseCase().flowOn(dispatcherProvider.io).first()
            withContext(dispatcherProvider.main) {
                if (faqResult is DataState.Success<List<UiHelpQuestion> , *> && reviewResult is DataState.Success<ReviewInfo , *>) {
                    _screenState.updateData(newDataState = ScreenState.Success()) { current ->
                        current.copy(
                            questions = ArrayList(faqResult.data) , reviewInfo = reviewResult.data
                        )
                    }
                }
                else {
                    _screenState.updateState(ScreenState.Error())
                }
            }
        }
    }

    private fun requestReviewFlow() {
        viewModelScope.launch(dispatcherProvider.io) {
            requestReviewFlowUseCase().flowOn(dispatcherProvider.io).collect { result : DataState<ReviewInfo , Errors> ->
                        if (result is DataState.Success) {
                            withContext(dispatcherProvider.main) {
                                _screenState.updateData(newDataState = ScreenState.Success()) { current ->
                                    current.copy(reviewInfo = result.data)
                                }
                            }
                        }
                    }
        }
    }

    private fun launchReviewFlow(activity : Activity , reviewInfo : ReviewInfo) {
        viewModelScope.launch(dispatcherProvider.io) {
            launchReviewFlowUseCase(Pair(activity , reviewInfo)).flowOn(dispatcherProvider.io).collect { /* Optionally handle result */ }
        }
    }
}