package com.d4rk.android.apps.apptoolkit.core.utils.extensions

import android.database.sqlite.SQLiteException
import com.d4rk.android.apps.apptoolkit.R as AppR
import com.d4rk.android.apps.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.R as ToolkitR
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.sql.SQLException

fun Errors.asUiText() : UiTextHelper {
    return when (this) {
        Errors.Network.REQUEST_TIMEOUT ,
        Errors.Network.NO_INTERNET ,
        Errors.Database.DATABASE_OPERATION_FAILED -> UiTextHelper.StringResource(ToolkitR.string.io_error)

        Errors.Network.SERIALIZATION ,
        Errors.UseCase.NO_DATA -> UiTextHelper.StringResource(ToolkitR.string.initialization_error)

        Errors.UseCase.FAILED_TO_LOAD_APPS -> UiTextHelper.StringResource(AppR.string.error_failed_to_load_apps)

        Errors.UseCase.ILLEGAL_ARGUMENT -> UiTextHelper.StringResource(ToolkitR.string.illegal_argument_error)

        else -> UiTextHelper.StringResource(ToolkitR.string.unknown_error)

    }
}

fun Throwable.toError(default : Errors = Errors.UseCase.NO_DATA) : Errors {
    return when (this) {
        is UnknownHostException -> Errors.Network.NO_INTERNET
        is SocketTimeoutException -> Errors.Network.REQUEST_TIMEOUT
        is ConnectException -> Errors.Network.NO_INTERNET
        is SerializationException -> Errors.Network.SERIALIZATION
        is SQLException , is SQLiteException -> Errors.Database.DATABASE_OPERATION_FAILED
        is IllegalArgumentException -> Errors.UseCase.ILLEGAL_ARGUMENT
        else -> default
    }
}