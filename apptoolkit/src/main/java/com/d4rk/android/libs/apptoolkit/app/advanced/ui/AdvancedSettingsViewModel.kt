package com.d4rk.android.libs.apptoolkit.app.advanced.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.advanced.data.CacheRepository
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.copyData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdvancedSettingsViewModel(
    private val repository: CacheRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        UiStateScreen(
            screenState = ScreenState.Success(),
            data = AdvancedSettingsUiState(),
        ),
    )
    val uiState: StateFlow<UiStateScreen<AdvancedSettingsUiState>> = _uiState.asStateFlow()

    fun onClearCache() {
        viewModelScope.launch {
            val success = repository.clearCache()
            val message = if (success) {
                R.string.cache_cleared_success
            } else {
                R.string.cache_cleared_error
            }
            _uiState.copyData { copy(cacheClearMessage = message) }
        }
    }

    fun onMessageShown() {
        _uiState.copyData { copy(cacheClearMessage = null) }
    }
}
