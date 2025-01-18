package com.d4rk.android.libs.apptoolkit.utils.error

import android.app.Activity
import android.content.Context
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.utils.constants.error.ErrorType
import com.d4rk.android.libs.apptoolkit.utils.interfaces.ErrorReporter
import com.google.android.material.snackbar.Snackbar

object ErrorHandler {

    private var errorReporter: ErrorReporter? = null

    /**
     * Initialize the ErrorHandler with an [ErrorReporter] implementation
     * from your app. (E.g., Crashlytics-based reporter.)
     */
    fun init(reporter: ErrorReporter) {
        errorReporter = reporter
    }

    /**
     * Handles a general error in the application, by:
     * 1) Determining a user-facing message from [errorType].
     * 2) Reporting the exception via [errorReporter].
     * 3) Optionally showing a Snackbar (when [applicationContext] is an Activity).
     */
    fun handleError(
        applicationContext: Context,
        errorType: ErrorType,
        exception: Throwable?
    ) {
        val message: String = applicationContext.getString(
            when (errorType) {
                ErrorType.SECURITY_EXCEPTION -> R.string.security_error
                ErrorType.IO_EXCEPTION -> R.string.io_error
                ErrorType.ACTIVITY_NOT_FOUND -> R.string.activity_not_found
                ErrorType.ILLEGAL_ARGUMENT -> R.string.illegal_argument_error
                else -> R.string.unknown_error
            }
        )

        // Report it
        reportException(exception = exception, displayMessage = message)

        // Show a user-facing Snackbar
        showSnackbar(context = applicationContext, message = message)
    }

    /**
     * Handles failures during initialization steps (e.g. DB init, Ads init, etc.).
     * 1) Uses a caller-provided [message] or a fallback resource string.
     * 2) Reports the exception (if not null).
     * 3) Shows a Snackbar.
     */
    fun handleInitializationFailure(
        applicationContext: Context,
        message: String,
        exception: Exception? = null
    ) {
        val displayMessage: String = message.ifEmpty {
            applicationContext.getString(R.string.initialization_error)
        }

        reportException(exception = exception, displayMessage = displayMessage)
        showSnackbar(context = applicationContext, message = displayMessage)
    }

    /**
     * Internal helper that delegates to our [errorReporter].
     * We set some custom keys, then record the exception itself.
     */
    private fun reportException(exception: Throwable?, displayMessage: String) {
        val throwableToReport = exception ?: Exception(displayMessage)
        errorReporter?.setCustomKey("error_type", "ERROR_HANDLER_REPORT")
        errorReporter?.setCustomKey("error_message", displayMessage)
        errorReporter?.recordException(throwableToReport)
    }

    /**
     * Shows a Snackbar if [context] is an Activity. Otherwise, no-op.
     */
    private fun showSnackbar(context: Context, message: String) {
        (context as? Activity)?.let { activity ->
            activity.runOnUiThread {
                Snackbar.make(
                    activity.findViewById(android.R.id.content),
                    message,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
}
