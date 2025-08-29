package com.d4rk.android.libs.apptoolkit.app.advanced.ui

import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.advanced.data.CacheRepository
import com.d4rk.android.libs.apptoolkit.app.advanced.domain.actions.AdvancedSettingsAction
import com.d4rk.android.libs.apptoolkit.app.advanced.domain.actions.AdvancedSettingsEvent
import com.d4rk.android.libs.apptoolkit.app.advanced.domain.model.ui.UiAdvancedSettingsScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.Result
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.copyData
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AdvancedSettingsViewModel(
    private val repository: CacheRepository,
) : ScreenViewModel<UiAdvancedSettingsScreen, AdvancedSettingsEvent, AdvancedSettingsAction>(
    initialState = UiStateScreen(
        screenState = ScreenState.Success(),
        data = UiAdvancedSettingsScreen()
    ),
) {

    override fun onEvent(event: AdvancedSettingsEvent) {
        when (event) {
            AdvancedSettingsEvent.ClearCache -> clearCache()
            AdvancedSettingsEvent.MessageShown -> onMessageShown()
        }
    }

    private fun clearCache() {
        viewModelScope.launch {
            repository.clearCache()
                .catch { emit(Result.Error(it as Exception)) }
                .collect { result ->
                    val message = when (result) {
                        is Result.Success -> R.string.cache_cleared_success
                        is Result.Error -> R.string.cache_cleared_error
                    }
                    screenState.copyData { copy(cacheClearMessage = message) }
                }
        }
    }

    private fun onMessageShown() {
        screenState.copyData { copy(cacheClearMessage = null) }
    }
}
