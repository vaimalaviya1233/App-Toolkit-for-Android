package com.d4rk.android.libs.apptoolkit.app.help.ui

import android.app.Activity
import com.d4rk.android.libs.apptoolkit.app.help.domain.actions.HelpActions
import com.d4rk.android.libs.apptoolkit.app.help.domain.events.HelpEvents
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

class HelpViewModel(private val getFAQsUseCase : GetFAQsUseCase , private val requestReviewFlowUseCase : RequestReviewFlowUseCase , private val launchReviewFlowUseCase : LaunchReviewFlowUseCase , private val dispatcherProvider : DispatcherProvider) :
    ScreenViewModel<UiHelpScreen , HelpEvents , HelpActions>(initialState = UiStateScreen(data = UiHelpScreen())) {

    init {
        onEvent(event = HelpEvents.LoadHelp)
    }

    override fun onEvent(event : HelpEvents) {
        when (event) {
            is HelpEvents.LoadHelp -> loadHelpData()
            is HelpEvents.RequestReview -> requestReviewFlow()
            is HelpEvents.LaunchReviewFlow -> launchReviewFlow(activity = event.activity , reviewInfo = event.reviewInfo)
        }
    }

    private fun loadHelpData() {
        launch(context = dispatcherProvider.io) {
            combine(getFAQsUseCase() , requestReviewFlowUseCase()) { faqResult , reviewResult ->
                faqResult to reviewResult
            }.flowOn(context = dispatcherProvider.default).collect { (faqResult , reviewResult) ->
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
            requestReviewFlowUseCase().collect { result : DataState<ReviewInfo , Errors> ->
                screenState.applyResult(result) { reviewInfo , current ->
                    current.copy(reviewInfo = reviewInfo)
                }
            }
        }
    }

    private fun launchReviewFlow(activity : Activity , reviewInfo : ReviewInfo) {
        launch(context = dispatcherProvider.io) {
            launchReviewFlowUseCase(activity to reviewInfo).collect {
                // Optional: Handle review flow result
            }
        }
    }
}