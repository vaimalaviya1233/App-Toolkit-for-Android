package com.d4rk.android.libs.apptoolkit.core.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.core.ui.base.handling.ActionEvent
import com.d4rk.android.libs.apptoolkit.core.ui.base.handling.UiEvent
import com.d4rk.android.libs.apptoolkit.core.ui.base.handling.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<S : UiState , E : UiEvent , A : ActionEvent>(initialState : S) : ViewModel() {

    protected val _uiState : MutableStateFlow<S> = MutableStateFlow(value = initialState)
    val uiState : StateFlow<S> = _uiState.asStateFlow()

    private val _actionEvent = MutableSharedFlow<A>(extraBufferCapacity = 1)
    val actionEvent: SharedFlow<A> = _actionEvent.asSharedFlow()

    protected val currentState : S
        get() = uiState.value

    abstract fun onEvent(event : E)

    protected fun sendAction(action : A) {
        viewModelScope.launch {
            _actionEvent.emit(action)
        }
    }
}