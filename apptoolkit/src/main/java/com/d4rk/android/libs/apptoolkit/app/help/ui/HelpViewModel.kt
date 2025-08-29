package com.d4rk.android.libs.apptoolkit.app.help.ui

import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.app.help.domain.actions.HelpAction
import com.d4rk.android.libs.apptoolkit.app.help.domain.actions.HelpEvent
import com.d4rk.android.libs.apptoolkit.app.help.domain.model.ui.UiHelpScreen
import com.d4rk.android.libs.apptoolkit.app.help.domain.repository.HelpRepository
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.dismissSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.showSnackbar
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.ScreenMessageType
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class HelpViewModel(
    private val helpRepository: HelpRepository,
) : ScreenViewModel<UiHelpScreen, HelpEvent, HelpAction>(
    initialState = UiStateScreen(screenState = ScreenState.IsLoading(), data = UiHelpScreen())
) {

    override fun onEvent(event: HelpEvent) {
        when (event) {
            HelpEvent.LoadFaq -> loadFaq()
            HelpEvent.DismissSnackbar -> screenState.dismissSnackbar()
        }
    }

    private fun loadFaq() {
        viewModelScope.launch {
            helpRepository.fetchFaq()
                .onStart { screenState.updateState(ScreenState.IsLoading()) }
                .catch { error ->
                    if (error is CancellationException) throw error
                    screenState.showSnackbar(
                        UiSnackbar(
                            message = UiTextHelper.DynamicString(error.message ?: "Failed to load FAQs"),
                            type = ScreenMessageType.SNACKBAR,
                            isError = true,
                            timeStamp = System.currentTimeMillis(),
                        )
                    )
                    screenState.updateState(ScreenState.Error())
                }
                .collect { questions ->
                    if (questions.isEmpty()) {
                        screenState.updateState(ScreenState.NoData())
                    } else {
                        screenState.updateData(newState = ScreenState.Success()) { current ->
                            current.copy(questions = questions)
                        }
                    }
                }
        }
    }
}

