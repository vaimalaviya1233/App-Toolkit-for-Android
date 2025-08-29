package com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model

import io.ktor.http.HttpStatusCode

sealed interface IssueReportResult {
    data class Success(val url: String) : IssueReportResult

    /** Represents different error cases that can occur when reporting an issue. */
    sealed interface Error : IssueReportResult {
        val message: String

        /** Generic network connectivity problems. */
        data class Network(override val message: String) : Error

        /** Failures when serializing or deserializing payloads. */
        data class Serialization(override val message: String) : Error

        /** HTTP 400 Bad Request */
        data class BadRequest(override val message: String) : Error

        /** HTTP 401 Unauthorized */
        data class Unauthorized(override val message: String) : Error

        /** HTTP 403 Forbidden */
        data class Forbidden(override val message: String) : Error

        /** Any other HTTP status code not explicitly handled. */
        data class Unknown(val status: HttpStatusCode, override val message: String) : Error
    }
}