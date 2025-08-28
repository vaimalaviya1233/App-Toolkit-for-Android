package com.d4rk.android.libs.apptoolkit.app.advanced.ui

import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.advanced.data.CacheRepository
import com.d4rk.android.libs.apptoolkit.app.advanced.domain.actions.AdvancedSettingsAction
import com.d4rk.android.libs.apptoolkit.app.advanced.domain.actions.AdvancedSettingsEvent
import com.d4rk.android.libs.apptoolkit.app.advanced.domain.model.ui.UiAdvancedSettingsScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.copyData
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import kotlinx.coroutines.launch

class AdvancedSettingsViewModel(
    private val repository: CacheRepository,
) : ScreenViewModel<UiAdvancedSettingsScreen, AdvancedSettingsEvent, AdvancedSettingsAction>(
    initialState = UiStateScreen(data = UiAdvancedSettingsScreen()),
) {

    override fun onEvent(event: AdvancedSettingsEvent) {
        when (event) {
            AdvancedSettingsEvent.ClearCache -> clearCache()
            AdvancedSettingsEvent.MessageShown -> onMessageShown()
        }
    }

    private fun clearCache() {
        viewModelScope.launch {
            val success = repository.clearCache()
            val message = if (success) {
                R.string.cache_cleared_success
            } else {
                R.string.cache_cleared_error
            }
            screenState.copyData { copy(cacheClearMessage = message) }
        }
    }

    private fun onMessageShown() {
        screenState.copyData { copy(cacheClearMessage = null) }
    }
}
