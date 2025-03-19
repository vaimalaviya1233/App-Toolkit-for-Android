package com.d4rk.android.libs.apptoolkit.app.settings.general.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.setErrors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.setLoading
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateState
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GeneralSettingsViewModel : ViewModel() {

    private val _screenState : MutableStateFlow<UiStateScreen<String>> = MutableStateFlow(UiStateScreen(screenState = ScreenState.IsLoading() , data = ""))
    val screenState : StateFlow<UiStateScreen<String>> = _screenState.asStateFlow()

    fun loadContent(contentKey : String?) {
        viewModelScope.launch {
            _screenState.setLoading()
            if (! contentKey.isNullOrBlank()) {
                _screenState.updateData(newDataState = ScreenState.Success()) { contentKey }
            }
            else {
                _screenState.setErrors(errors = listOf(UiSnackbar(message = UiTextHelper.DynamicString("Invalid content key!"))))
                _screenState.updateState(newValues = ScreenState.NoData())
            }
        }
    }
}