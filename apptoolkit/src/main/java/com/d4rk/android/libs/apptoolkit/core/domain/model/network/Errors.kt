package com.d4rk.android.libs.apptoolkit.core.domain.model.network

sealed interface Errors : Error {

    enum class Network : Errors {
        REQUEST_TIMEOUT , NO_INTERNET , SERIALIZATION
    }

    enum class UseCase : Errors {
        NO_DATA , FAILED_TO_LAUNCH_REVIEW , FAILED_TO_LOAD_FAQS , FAILED_TO_REQUEST_REVIEW , FAILED_TO_IMPORT_CART , FAILED_TO_DECRYPT_CART , FAILED_TO_ENCRYPT_CART ,FAILED_TO_LOAD_SKU_DETAILS
    }

    enum class Database : Errors {
        DATABASE_OPERATION_FAILED
    }
}
