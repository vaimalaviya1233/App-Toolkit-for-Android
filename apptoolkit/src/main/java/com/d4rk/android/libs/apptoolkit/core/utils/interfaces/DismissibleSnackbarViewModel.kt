package com.d4rk.android.libs.apptoolkit.core.utils.interfaces

import com.d4rk.android.libs.apptoolkit.core.ui.base.handling.UiEvent

/**
 * Contract for ViewModels that can instruct the UI to dismiss a snackbar.
 *
 * @param E type of [UiEvent] used to trigger the dismissal
 */
interface DismissibleSnackbarViewModel<E : UiEvent> {
    /** Event emitted when the current snackbar should be cleared. */
    fun getDismissSnackbarEvent() : E
}