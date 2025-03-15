package com.d4rk.android.libs.apptoolkit.core.utils.extensions

import android.database.sqlite.SQLiteException
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.sql.SQLException

fun Throwable.toError(default : Errors = Errors.UseCase.NO_DATA) : Errors {
    return when (this) {
        is UnknownHostException -> Errors.Network.NO_INTERNET
        is SocketTimeoutException -> Errors.Network.REQUEST_TIMEOUT
        is ConnectException -> Errors.Network.NO_INTERNET
        is SerializationException -> Errors.Network.SERIALIZATION
        is SQLException , is SQLiteException -> Errors.Database.DATABASE_OPERATION_FAILED
        is IllegalStateException -> when (this.message) {
            Errors.UseCase.FAILED_TO_IMPORT_CART.toString() -> Errors.UseCase.FAILED_TO_IMPORT_CART
            else -> Errors.UseCase.FAILED_TO_ENCRYPT_CART
        }

        is IllegalArgumentException -> Errors.UseCase.FAILED_TO_DECRYPT_CART
        else -> default
    }
}