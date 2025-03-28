package com.d4rk.android.libs.apptoolkit.core.domain.model.ui

import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.ScreenDataStatus
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.ScreenMessageType
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Encapsulates the UI state for a screen, including status, data, errors, and snackbar messages.
 *
 * @param T The type of the screen's data.
 * @property screenState The current [ScreenState] (e.g., loading, success, error). Defaults to [ScreenState.IsLoading].
 * @property errors List of [UiSnackbar] errors. Defaults to empty list.
 * @property snackbar Optional single [UiSnackbar] for non-error messages. Defaults to null.
 * @property data The screen-specific data of type [T]. Defaults to null.
 */
data class UiStateScreen<T>(
    val screenState : ScreenState = ScreenState.IsLoading() , var errors : List<UiSnackbar> = emptyList() , val snackbar : UiSnackbar? = null , val data : T? = null
)

/**
 * Represents a snackbar message to be shown in the UI.
 *
 * @property type Message category string, see [ScreenMessageType]. Defaults to [ScreenMessageType.NONE].
 * @property message The text content using [UiTextHelper]. Defaults to empty.
 * @property isError True if this is an error message. Defaults to `true`.
 * @property timeStamp Creation timestamp (milliseconds). Defaults to 0.
 */
data class UiSnackbar(
    var type : String = ScreenMessageType.NONE ,
    val message : UiTextHelper = UiTextHelper.DynamicString(content = "") ,
    val isError : Boolean = true ,
    val timeStamp : Long = 0 ,
)

/**
 * Updates the [UiStateScreen.data] and [UiStateScreen.screenState] within the flow.
 *
 * @param newDataState The new [ScreenState].
 * @param newValues Lambda to transform the existing data `(T) -> T`.
 */
fun <T> MutableStateFlow<UiStateScreen<T>>.updateData(newDataState : ScreenState , newValues : (T) -> T) {
    update { current ->
        current.copy(screenState = newDataState , data = current.data?.let(newValues))
    }
}

/**
 * Updates the [UiStateScreen.screenState] within the flow.
 *
 * @param newValues The new [ScreenState].
 */
fun <T> MutableStateFlow<UiStateScreen<T>>.updateState(newValues : ScreenState) {
    update { current ->
        current.copy(screenState = newValues)
    }
}

/**
 * Updates the [UiStateScreen.errors] list within the flow.
 *
 * @param errors The new list of error [UiSnackbar]s.
 */
fun <T> MutableStateFlow<UiStateScreen<T>>.setErrors(errors : List<UiSnackbar>) {
    update { current ->
        current.copy(errors = errors)
    }
}

/**
 * Sets the [UiStateScreen.snackbar] within the flow to display a message.
 *
 * @param snackbar The [UiSnackbar] to show.
 */
fun <T> MutableStateFlow<UiStateScreen<T>>.showSnackbar(snackbar : UiSnackbar) {
    update { current ->
        current.copy(snackbar = snackbar)
    }
}

/** Sets the [UiStateScreen.snackbar] to null within the flow, dismissing it. */
fun <T> MutableStateFlow<UiStateScreen<T>>.dismissSnackbar() {
    update { current ->
        current.copy(snackbar = null)
    }
}

/**
 * Sets [UiStateScreen.errors] and updates [UiStateScreen.screenState] to [ScreenState.Error] within the flow.
 *
 * @param errors The list of error [UiSnackbar]s. Null becomes an empty list.
 */
fun <T> MutableStateFlow<UiStateScreen<T>>.setErrors(errors : ArrayList<UiSnackbar>?) {
    update { current ->
        current.copy(screenState = ScreenState.Error() , errors = errors ?: ArrayList())
    }
}

/** Sets the [UiStateScreen.screenState] to [ScreenState.IsLoading] within the flow. */
fun <T> MutableStateFlow<UiStateScreen<T>>.setLoading() {
    update { current ->
        current.copy(screenState = ScreenState.IsLoading())
    }
}

/**
 * Gets the current [UiStateScreen.data] from the flow's value.
 *
 * @return The data of type [T].
 * @throws IllegalStateException if data is null.
 */
fun <T> MutableStateFlow<UiStateScreen<T>>.getData() : T {
    return value.data ?: throw IllegalStateException("Data is not available or null.") // Slightly modified exception message
}

/**
 * Gets the current [UiStateScreen.errors] list from the flow's value.
 *
 * @return The list of error [UiSnackbar]s.
 */
fun <T> MutableStateFlow<UiStateScreen<T>>.getErrors() : List<UiSnackbar> {
    return value.errors
}

/** Sealed class representing distinct UI screen states. */
sealed class ScreenState {
    /** State: No data available. [data] defaults to [ScreenDataStatus.NO_DATA]. */
    data class NoData(val data : String = ScreenDataStatus.NO_DATA) : ScreenState()

    /** State: Currently loading data. [data] defaults to [ScreenDataStatus.LOADING]. */
    data class IsLoading(val data : String = ScreenDataStatus.LOADING) : ScreenState()

    /** State: Data loaded successfully. [data] defaults to [ScreenDataStatus.HAS_DATA]. */
    data class Success(val data : String = ScreenDataStatus.HAS_DATA) : ScreenState()

    /** State: An error occurred. [data] defaults to [ScreenDataStatus.ERROR]. */
    data class Error(val data : String = ScreenDataStatus.ERROR) : ScreenState()
}