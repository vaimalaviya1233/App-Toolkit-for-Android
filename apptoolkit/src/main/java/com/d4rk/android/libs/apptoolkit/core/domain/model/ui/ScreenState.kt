package com.d4rk.android.libs.apptoolkit.core.domain.model.ui

import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.ScreenDataStatus
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.ScreenMessageType
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Represents the UI state of a screen.
 *
 * This class encapsulates the different states a screen can be in, including loading, success, error, and no data,
 * along with any associated data, snackbar messages, and a list of errors. It is designed to simplify state management
 * for UI screens in a reactive and predictable way.
 *
 * @param T The type of the data associated with the screen.
 * @property screenState The current state of the screen, represented by the [ScreenState] enum. Defaults to [ScreenState.NoData].
 * @property errors A list of [UiSnackbar] representing any errors that occurred.  This allows presenting multiple error messages
 * simultaneously or in sequence. Defaults to an empty list.
 * @property snackbar A single [UiSnackbar] to be displayed. Useful for showing informational or success messages.
 * Can be used instead of `errors` or alongside it. Defaults to null.
 * @property data The data associated with the screen. This is only populated when the screen is in a success state.
 * Defaults to null.
 */
data class UiStateScreen<T>(
    val screenState : ScreenState = ScreenState.NoData() , var errors : List<UiSnackbar> = emptyList() , val snackbar : UiSnackbar? = null , val data : T? = null
)

/**
 * Represents a snackbar to be displayed in the UI.
 *
 * This data class encapsulates the information needed to display a snackbar, including its type,
 * message content, whether it represents an error, and a timestamp for tracking.
 *
 * @property type The type of the snackbar. Defaults to [ScreenMessageType.NONE].
 *                  This can be used to differentiate between different categories of messages.
 *                  See [ScreenMessageType] for possible values.
 * @property message The message content to be displayed in the snackbar.
 *                   It uses [UiTextHelper] to handle both dynamic strings and string resources.
 *                   Defaults to an empty dynamic string.
 * @property isError A boolean indicating whether the snackbar represents an error condition.
 *                   Defaults to `true`. This can be used for visual styling (e.g., red background).
 * @property timeStamp A timestamp (in milliseconds) representing when the snackbar was created.
 *                     Defaults to 0. This can be used for features like automatic dismissal after a certain time, or to identify unique messages.
 */
data class UiSnackbar(
    var type : String = ScreenMessageType.NONE ,
    val message : UiTextHelper = UiTextHelper.DynamicString(content = "") ,
    val isError : Boolean = true ,
    val timeStamp : Long = 0 ,
)

/**
 * Updates the data and state of a [MutableStateFlow] of [UiStateScreen].
 *
 * This function allows modifying the data within the [UiStateScreen] and updating its state simultaneously.
 *
 * @param newDataState The new [ScreenState] to set.
 * @param newValues A lambda function that takes the current data of type [T] and returns a modified version of it.
 */
fun <T> MutableStateFlow<UiStateScreen<T>>.updateData(newDataState : ScreenState , newValues : (T) -> T) {
    update { current ->
        current.copy(
            screenState = newDataState , data = current.data?.let(newValues)
        )
    }
}

/**
 * Updates the state of a [MutableStateFlow] of [UiStateScreen].
 *
 * This function updates only the screenState property of the [UiStateScreen].
 *
 * @param newValues The new [ScreenState] to set.
 */
fun <T> MutableStateFlow<UiStateScreen<T>>.updateState(newValues : ScreenState) {
    update { current ->
        current.copy(screenState = newValues)
    }
}

/**
 * Sets a list of errors in a [MutableStateFlow] of [UiStateScreen].
 *
 * This function updates the errors property of the [UiStateScreen].
 *
 * @param errors The list of [UiSnackbar] to set as errors.
 */
fun <T> MutableStateFlow<UiStateScreen<T>>.setErrors(errors : List<UiSnackbar>) {
    update { current ->
        current.copy(errors = errors)
    }
}

/**
 * Shows a snackbar in a [MutableStateFlow] of [UiStateScreen].
 *
 * This function updates the snackbar property of the [UiStateScreen].
 *
 * @param snackbar The [UiSnackbar] to display.
 */
fun <T> MutableStateFlow<UiStateScreen<T>>.showSnackbar(snackbar : UiSnackbar) {
    update { current ->
        current.copy(snackbar = snackbar)
    }
}

/**
 * Dismisses the currently displayed snackbar in a [MutableStateFlow] of [UiStateScreen].
 *
 * This function sets the snackbar property of the [UiStateScreen] to null.
 */
fun <T> MutableStateFlow<UiStateScreen<T>>.dismissSnackbar() {
    update { current ->
        current.copy(snackbar = null)
    }
}

/**
 * Sets a list of errors and updates the state to [ScreenState.Error] in a [MutableStateFlow] of [UiStateScreen].
 *
 * This function updates both the state and the errors property of the [UiStateScreen].
 *
 * @param errors The list of [UiSnackbar] to set as errors. If null, an empty list is used.
 */
fun <T> MutableStateFlow<UiStateScreen<T>>.setErrors(errors : ArrayList<UiSnackbar>?) {
    update { current ->
        current.copy(
            screenState = ScreenState.Error(), errors = errors ?: ArrayList()
        )
    }
}

/**
 * Sets the state to [ScreenState.IsLoading] in a [MutableStateFlow] of [UiStateScreen].
 *
 * This function updates the state to indicate that the screen is loading.
 */
fun <T> MutableStateFlow<UiStateScreen<T>>.setLoading() {
    update { current ->
        current.copy(
            screenState = ScreenState.IsLoading()
        )
    }
}

/**
 * Retrieves the data from a [MutableStateFlow] of [UiStateScreen].
 *
 * This function returns the data property of the [UiStateScreen].
 *
 * @return The data of type [T].
 * @throws IllegalStateException if the data is null.
 */
fun <T> MutableStateFlow<UiStateScreen<T>>.getData() : T {
    return value.data ?: throw IllegalStateException("Data is not of expected type.")
}

/**
 * Retrieves the list of errors from a [MutableStateFlow] of [UiStateScreen].
 *
 * This function returns the errors property of the [UiStateScreen].
 *
 * @return The list of [UiSnackbar] representing the errors.
 */
fun <T> MutableStateFlow<UiStateScreen<T>>.getErrors() : List<UiSnackbar> {
    return value.errors
}

/**
 * Represents the different states a screen can be in.
 *
 * This sealed class provides a type-safe way to represent the various states of a screen,
 * such as no data, loading, success, or error.
 */
sealed class ScreenState {
    /** Represents the state when there is no data to display. */
    data class NoData(val data : String = ScreenDataStatus.NO_DATA) : ScreenState()

    /** Represents the state when the screen is loading data. */
    data class IsLoading(val data : String = ScreenDataStatus.LOADING) : ScreenState()

    /** Represents the state when data has been successfully loaded. */
    data class Success(val data : String = ScreenDataStatus.HAS_DATA) : ScreenState()

    /** Represents the state when an error has occurred. */
    data class Error(val data : String = ScreenDataStatus.ERROR) : ScreenState()
}