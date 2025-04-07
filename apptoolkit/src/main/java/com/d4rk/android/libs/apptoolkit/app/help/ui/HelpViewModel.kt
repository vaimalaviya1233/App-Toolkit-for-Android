package com.d4rk.android.libs.apptoolkit.app.help.ui

import android.app.Activity
import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.app.help.domain.actions.HelpAction
import com.d4rk.android.libs.apptoolkit.app.help.domain.actions.HelpEvent
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
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.applyResult
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.successData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateState
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.google.android.play.core.review.ReviewInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

class HelpViewModel(private val getFAQsUseCase : GetFAQsUseCase , private val requestReviewFlowUseCase : RequestReviewFlowUseCase , private val launchReviewFlowUseCase : LaunchReviewFlowUseCase , private val dispatcherProvider : DispatcherProvider) : ScreenViewModel<UiHelpScreen , HelpEvent , HelpAction>(initialState = UiStateScreen(data = UiHelpScreen())) {

    init {
        onEvent(event = HelpEvent.LoadHelp)
    }

    override fun onEvent(event : HelpEvent) {
        when (event) {
            is HelpEvent.LoadHelp -> loadHelpData()
            is HelpEvent.RequestReview -> requestReviewFlow()
            is HelpEvent.LaunchReviewFlow -> launchReviewFlow(activity = event.activity , reviewInfo = event.reviewInfo)
        }
    }

    private fun loadHelpData() {
        launch(context = dispatcherProvider.io) {
            combine(flow = getFAQsUseCase().flowOn(context = dispatcherProvider.io) , flow2 = requestReviewFlowUseCase().flowOn(context = dispatcherProvider.io)) { faqResult : DataState<List<UiHelpQuestion>, Errors> , reviewResult : DataState<ReviewInfo, Errors> -> faqResult to reviewResult }.collect { (faqResult : DataState<List<UiHelpQuestion>, Errors> , reviewResult : DataState<ReviewInfo, Errors>) ->
                when {
                    faqResult is DataState.Success && reviewResult is DataState.Success -> {
                        screenState.successData {
                            copy(questions = ArrayList(faqResult.data) , reviewInfo = reviewResult.data)
                        }
                    }

                    faqResult is DataState.Error || reviewResult is DataState.Error -> {
                        screenState.updateState(newValues = ScreenState.Error())
                    }
                }
            }
        }
    }

    private fun requestReviewFlow() {
        launch(context = dispatcherProvider.io) {
            requestReviewFlowUseCase().stateIn(scope = viewModelScope , started = SharingStarted.Lazily , initialValue = DataState.Loading()).collect { result : DataState<ReviewInfo, Errors> ->
                screenState.applyResult(result = result) { reviewInfo : ReviewInfo , current : UiHelpScreen ->
                    current.copy(reviewInfo = reviewInfo)
                }
            }
        }
    }

    private fun launchReviewFlow(activity : Activity , reviewInfo : ReviewInfo) {
        launch(context = dispatcherProvider.io) {
            launchReviewFlowUseCase(param = Pair(first = activity , second = reviewInfo)).collect { /* Optionally handle result */ }
        }
    }
}