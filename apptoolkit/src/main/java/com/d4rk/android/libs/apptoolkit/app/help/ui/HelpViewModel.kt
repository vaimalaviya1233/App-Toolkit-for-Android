package com.d4rk.android.libs.apptoolkit.app.help.ui

import android.app.Activity
import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.app.help.domain.actions.HelpEvent
import com.d4rk.android.libs.apptoolkit.app.help.domain.model.ui.UiHelpScreen
import com.d4rk.android.libs.apptoolkit.app.help.domain.usecases.GetFAQsUseCase
import com.d4rk.android.libs.apptoolkit.app.help.domain.usecases.LaunchReviewFlowUseCase
import com.d4rk.android.libs.apptoolkit.app.help.domain.usecases.RequestReviewFlowUseCase
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
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

class HelpViewModel(
    private val getFAQsUseCase : GetFAQsUseCase , private val requestReviewFlowUseCase : RequestReviewFlowUseCase , private val launchReviewFlowUseCase : LaunchReviewFlowUseCase , private val dispatcherProvider : DispatcherProvider
) : ScreenViewModel<UiHelpScreen , HelpEvent , Nothing>(
    initialState = UiStateScreen(data = UiHelpScreen())
) {

    init {
        onEvent(HelpEvent.LoadHelp)
    }

    override fun onEvent(event : HelpEvent) {
        when (event) {
            is HelpEvent.LoadHelp -> loadHelpData()
            is HelpEvent.RequestReview -> requestReviewFlow()
            is HelpEvent.LaunchReviewFlow -> launchReviewFlow(event.activity , event.reviewInfo)
        }
    }

    private fun loadHelpData() {
        launch(dispatcherProvider.io) {
            combine(
                getFAQsUseCase().flowOn(dispatcherProvider.io) , requestReviewFlowUseCase().flowOn(dispatcherProvider.io)
            ) { faqResult , reviewResult -> faqResult to reviewResult }.collect { (faqResult , reviewResult) ->
                when {
                    faqResult is DataState.Success && reviewResult is DataState.Success -> {
                        screenState.successData {
                            copy(
                                questions = ArrayList(faqResult.data) , reviewInfo = reviewResult.data
                            )
                        }
                    }

                    faqResult is DataState.Error || reviewResult is DataState.Error -> {
                        screenState.updateState(ScreenState.Error())
                    }
                }
            }
        }
    }

    private fun requestReviewFlow() {
        launch(dispatcherProvider.io) {
            requestReviewFlowUseCase().stateIn(viewModelScope , SharingStarted.Lazily , DataState.Loading()).collect { result ->
                screenState.applyResult(result) { reviewInfo , current ->
                    current.copy(reviewInfo = reviewInfo)
                }
            }
        }
    }

    private fun launchReviewFlow(activity : Activity , reviewInfo : ReviewInfo) {
        launch(dispatcherProvider.io) {
            launchReviewFlowUseCase(Pair(activity , reviewInfo)).collect { /* Optionally handle result */ }
        }
    }
}