package com.d4rk.android.libs.apptoolkit.core.domain.model.network

/**
 * A root error alias for handling errors in the data state.
 */
typealias RootError = Error

/**
 * Represents different states of data handling.
 *
 * @param D The type of data being processed.
 * @param E The type of error, extending from RootError.
 */
sealed interface DataState<out D , out E : RootError> {
    /** Represents a successful state with data. */
    data class Success<out D , out E : RootError>(val data : D) : DataState<D , E>

    /** Represents an error state, optionally containing previous data. */
    data class Error<out D , out E : RootError>(val data : D? = null , val error : E) : DataState<D , E>

    /** Represents a loading state, optionally containing previous data. */
    data class Loading<out D , out E : RootError>(val data : D? = null) : DataState<D , E>

    /** Represents an update state, optionally containing previous data. */
    data class Update<out D , out E : RootError>(val data : D? = null) : DataState<D , E>
}

/**
 * Executes an action when the state is `Success`.
 *
 * @param action The action to be executed with the success data.
 * @return The current DataState instance.
 */
inline fun <D , E : RootError> DataState<D , E>.onSuccess(action : (D) -> Unit) : DataState<D , E> {
    return when (this) {
        is DataState.Success -> {
            action(data)
            this
        }

        else -> this
    }
}

/**
 * Executes an action when the state is `Error`.
 *
 * @param action The action to be executed with the error.
 * @return The current DataState instance.
 */
inline fun <D , E : RootError> DataState<D , E>.onError(action : (E) -> Unit) : DataState<D , E> {
    return when (this) {
        is DataState.Error -> {
            action(error)
            this
        }

        else -> this
    }
}

/**
 * Executes an action when the state is `Loading`.
 *
 * @param action The action to be executed with the loading data.
 * @return The current DataState instance.
 */
inline fun <D , E : RootError> DataState<D , E>.onLoading(action : (D?) -> Unit) : DataState<D , E> {
    return when (this) {
        is DataState.Loading -> {
            action(data)
            this
        }

        else -> this
    }
}

/**
 * Executes an action when the state is `Update`.
 *
 * @param action The action to be executed with the update data.
 * @return The current DataState instance.
 */
inline fun <D , E : RootError> DataState<D , E>.onUpdate(action : (D?) -> Unit) : DataState<D , E> {
    return when (this) {
        is DataState.Update -> {
            action(data)
            this
        }

        else -> this
    }
}