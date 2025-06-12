package com.d4rk.android.libs.apptoolkit.app.help.ui

import com.d4rk.android.libs.apptoolkit.app.help.domain.actions.HelpActions
import com.d4rk.android.libs.apptoolkit.app.help.domain.events.HelpEvents
import com.d4rk.android.libs.apptoolkit.app.help.domain.model.ui.UiHelpQuestion
import com.d4rk.android.libs.apptoolkit.app.help.domain.model.ui.UiHelpScreen
import com.d4rk.android.libs.apptoolkit.app.help.domain.usecases.GetFAQsUseCase
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.applyResult
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.successData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateState
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import kotlinx.coroutines.flow.flowOn

class HelpViewModel(private val getFAQsUseCase : GetFAQsUseCase , private val dispatcherProvider : DispatcherProvider) :
    ScreenViewModel<UiHelpScreen , HelpEvents , HelpActions>(initialState = UiStateScreen(data = UiHelpScreen())) {

    init {
        onEvent(event = HelpEvents.LoadHelp)
    }

    override fun onEvent(event : HelpEvents) {
        when (event) {
            is HelpEvents.LoadHelp -> loadHelpData()
        }
    }

    private fun loadHelpData() {
        launch(context = dispatcherProvider.io) {
            getFAQsUseCase().flowOn(context = dispatcherProvider.default).collect { faqResult: DataState<List<UiHelpQuestion>, Errors> ->
                when (faqResult) {
                    is DataState.Success -> screenState.successData {
                        copy(questions = ArrayList(faqResult.data))
                    }
                    is DataState.Error -> screenState.updateState(newValues = ScreenState.Error())
                }
            }
        }
    }

}