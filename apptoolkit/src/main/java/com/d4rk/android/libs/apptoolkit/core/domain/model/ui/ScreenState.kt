package com.d4rk.android.libs.apptoolkit.core.domain.model.ui

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import com.d4rk.android.libs.apptoolkit.core.ui.base.handling.UiEvent
import com.d4rk.android.libs.apptoolkit.core.ui.base.handling.UiState
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.ScreenDataStatus
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.ScreenMessageType
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

data class UiStateScreen<T>(
    val screenState : ScreenState = ScreenState.IsLoading() , var errors : List<UiSnackbar> = emptyList() , val snackbar : UiSnackbar? = null , val data : T? = null

) : UiState

data class UiSnackbar(
    var type : String = ScreenMessageType.NONE ,
    val message : UiTextHelper = UiTextHelper.DynamicString(content = "") ,
    val isError : Boolean = true ,
    val timeStamp : Long = 0 ,
)


inline fun <T> MutableStateFlow<UiStateScreen<T>>.updateData(
    newState : ScreenState , crossinline transform : (T) -> T
) {
    update { current ->
        val updatedData = current.data?.let { transform(it) }
        current.copy(screenState = newState , data = updatedData)
    }
}

inline fun <T> MutableStateFlow<UiStateScreen<T>>.copyData(crossinline transform : T.() -> T) { // FIXME: Function "copyData" is never used
    update { current ->
        val updatedData = current.data?.transform()
        current.copy(data = updatedData)
    }
}

inline fun <T> MutableStateFlow<UiStateScreen<T>>.successData(crossinline transform : T.() -> T) {
    update { current ->
        val updatedData = current.data?.transform()
        current.copy(screenState = ScreenState.Success() , data = updatedData)
    }
}

inline fun <D , T , E : RootError> MutableStateFlow<UiStateScreen<T>>.applyResult(
    result : DataState<D , E> ,   errorMessage: UiTextHelper = UiTextHelper.DynamicString("Something went wrong") , crossinline transform : (D , T) -> T
) {
    when (result) {
        is DataState.Success -> {
            successData {
                transform(result.data , this)
            }
        }

        is DataState.Error -> {
            setErrors(errors = listOf(element = UiSnackbar(message = errorMessage)))
            updateState(newValues = ScreenState.Error())
        }

        is DataState.Loading -> {
            setLoading()
        }
    }
}

fun <T> MutableStateFlow<UiStateScreen<T>>.updateState(newValues : ScreenState) {
    update { current : UiStateScreen<T> ->
        current.copy(screenState = newValues)
    }
}

fun <T> MutableStateFlow<UiStateScreen<T>>.setErrors(errors : List<UiSnackbar>) {
    update { current : UiStateScreen<T> ->
        current.copy(errors = errors)
    }
}

@Composable
fun <T, E : UiEvent> DefaultSnackbarHandler(
    screenState: UiStateScreen<T>,
    snackbarHostState: SnackbarHostState,
    getDismissEvent: (() -> E)? = null,
    onEvent: ((E) -> Unit)? = null
) {
    val context : Context = LocalContext.current

    LaunchedEffect(key1 = screenState.snackbar?.timeStamp) {
        screenState.snackbar?.let { snackbar : UiSnackbar ->
            if (snackbarHostState.currentSnackbarData != null) {
                snackbarHostState.currentSnackbarData?.dismiss()
            }

            val result : SnackbarResult = snackbarHostState.showSnackbar(
                message = snackbar.message.asString(context = context) ,
                withDismissAction = true ,
                duration = if (snackbar.isError) SnackbarDuration.Long else SnackbarDuration.Short
            )
            if ((result == SnackbarResult.Dismissed || result == SnackbarResult.ActionPerformed) &&
                getDismissEvent != null && onEvent != null
            ) {
                onEvent(getDismissEvent())
            }
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.fillMaxWidth(),
        snackbar = { snackbarData : SnackbarData ->
            Snackbar(
                containerColor = if (screenState.snackbar?.isError == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface,
                contentColor = if (screenState.snackbar?.isError == true) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurface,
                action = {
                    TextButton(onClick = { snackbarData.dismiss() }) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                }
            ) {
                Text(text = snackbarData.visuals.message)
            }
        }
    )
}

fun <T> MutableStateFlow<UiStateScreen<T>>.showSnackbar(snackbar : UiSnackbar) {
    update { current : UiStateScreen<T> ->
        current.copy(snackbar = snackbar)
    }
}

fun <T> MutableStateFlow<UiStateScreen<T>>.dismissSnackbar() {
    update { current : UiStateScreen<T> ->
        current.copy(snackbar = null)
    }
}

fun <T> MutableStateFlow<UiStateScreen<T>>.setLoading() {
    update { current ->
        current.copy(screenState = ScreenState.IsLoading())
    }
}

fun <T> MutableStateFlow<UiStateScreen<T>>.getData() : T { // FIXME: Function "getData" is never used
    return value.data ?: throw IllegalStateException("Data is not available or null.")
}

fun <T> MutableStateFlow<UiStateScreen<T>>.getErrors() : List<UiSnackbar> { // FIXME: Function "getErrors" is never used
    return value.errors
}

sealed class ScreenState {
    data class NoData(val data : String = ScreenDataStatus.NO_DATA) : ScreenState()
    data class IsLoading(val data : String = ScreenDataStatus.LOADING) : ScreenState()
    data class Success(val data : String = ScreenDataStatus.HAS_DATA) : ScreenState()
    data class Error(val data : String = ScreenDataStatus.ERROR) : ScreenState()
}