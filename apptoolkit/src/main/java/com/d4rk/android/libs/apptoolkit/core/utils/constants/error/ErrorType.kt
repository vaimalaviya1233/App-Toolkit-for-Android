package com.d4rk.android.libs.apptoolkit.core.utils.constants.error

import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Error

/**
 * Enum class representing different categories of recoverable failures within the toolkit.
 *
 * The values defined here act as lightweight identifiers that can be surfaced to the UI layer or
 * logged for analytics. Declaring the enum as a subtype of [Error] ensures every constant is
 * compatible with APIs that expect the toolkit's marker error interface.
 */
enum class ErrorType : Error {
    SECURITY_EXCEPTION,
    IO_EXCEPTION,
    ACTIVITY_NOT_FOUND,
    ILLEGAL_ARGUMENT,
    SQLITE_EXCEPTION,
    FILE_NOT_FOUND,
    UNKNOWN_ERROR,
}
