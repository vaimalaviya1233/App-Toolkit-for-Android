package com.d4rk.android.apps.apptoolkit.core.domain.model.network

import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Error

sealed interface Errors : Error {

    enum class Network : Errors {
        REQUEST_TIMEOUT,
        NO_INTERNET,
        SERIALIZATION,
    }

    enum class UseCase : Errors {
        NO_DATA,
        FAILED_TO_LOAD_APPS,
        FAILED_TO_LAUNCH_REVIEW,
        FAILED_TO_LOAD_FAQS,
        FAILED_TO_REQUEST_REVIEW,
        FAILED_TO_UPDATE_APP,
        FAILED_TO_LOAD_SKU_DETAILS,
        FAILED_TO_LOAD_CONSENT_INFO,
        ILLEGAL_ARGUMENT,
    }

    enum class Database : Errors {
        DATABASE_OPERATION_FAILED,
    }
}
