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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HelpViewModel(private val getFAQsUseCase : GetFAQsUseCase , private val requestReviewFlowUseCase : RequestReviewFlowUseCase , private val launchReviewFlowUseCase : LaunchReviewFlowUseCase , private val dispatcherProvider : DispatcherProvider) : ViewModel() {

    private val _screenState : MutableStateFlow<UiStateScreen<UiHelpScreen>> = MutableStateFlow(value = UiStateScreen(screenState = ScreenState.IsLoading() , data = UiHelpScreen()))
    val screenState : StateFlow<UiStateScreen<UiHelpScreen>> = _screenState.asStateFlow()

    init {
        sendEvent(event = HelpAction.LoadHelp)
    }

    fun sendEvent(event : HelpAction) {
        when (event) {
            HelpAction.LoadHelp -> loadHelpData()
            HelpAction.RequestReview -> requestReviewFlow()
            is HelpAction.LaunchReviewFlow -> launchReviewFlow(activity = event.activity , reviewInfo = event.reviewInfo)
        }
    }

    private fun loadHelpData() {
        viewModelScope.launch {
            val faqFlow : StateFlow<DataState<List<UiHelpQuestion> , Errors>> = getFAQsUseCase().flowOn(context = dispatcherProvider.io).stateIn(scope = viewModelScope , started = SharingStarted.Lazily , initialValue = DataState.Loading())
            val reviewFlow : StateFlow<DataState<ReviewInfo , Errors>> = requestReviewFlowUseCase().flowOn(context = dispatcherProvider.io).stateIn(scope = viewModelScope , started = SharingStarted.Lazily , initialValue = DataState.Loading())
            val faqResult : DataState<List<UiHelpQuestion> , Errors> = faqFlow.first()
            val reviewResult : DataState<ReviewInfo , Errors> = reviewFlow.first()

            when {
                faqResult is DataState.Success<List<UiHelpQuestion> , *> && reviewResult is DataState.Success<ReviewInfo , *> -> {
                    _screenState.updateData(newDataState = ScreenState.Success()) { current ->
                        current.copy(questions = ArrayList(faqResult.data) , reviewInfo = reviewResult.data)
                    }
                }

                faqResult is DataState.Error || reviewResult is DataState.Error -> {
                    _screenState.updateState(newValues = ScreenState.Error())
                }

                else -> {
                    _screenState.updateState(newValues = ScreenState.IsLoading())
                }
            }
        }
    }

    private fun requestReviewFlow() {
        viewModelScope.launch {
            requestReviewFlowUseCase().flowOn(context = dispatcherProvider.io).stateIn(scope = viewModelScope , started = SharingStarted.Lazily , initialValue = DataState.Loading()).collect { result ->
                when (result) {
                    is DataState.Success -> {
                        _screenState.updateData(newDataState = ScreenState.Success()) { current ->
                            current.copy(reviewInfo = result.data)
                        }
                    }

                    is DataState.Error -> {
                        _screenState.updateState(newValues = ScreenState.Error())
                    }

                    is DataState.Loading -> {
                        _screenState.updateState(newValues = ScreenState.IsLoading())
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun launchReviewFlow(activity : Activity , reviewInfo : ReviewInfo) {
        viewModelScope.launch {
            launchReviewFlowUseCase(param = Pair(first = activity , second = reviewInfo)).flowOn(context = dispatcherProvider.io).collect { /* Optionally handle result */ }
        }
    }
}