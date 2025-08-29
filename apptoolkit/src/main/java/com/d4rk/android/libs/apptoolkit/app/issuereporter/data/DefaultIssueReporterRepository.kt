package com.d4rk.android.libs.apptoolkit.app.issuereporter.data

import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.CreateIssueRequest
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.IssueReportResult
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.Report
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.github.GithubTarget
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.repository.IssueReporterRepository
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.AppDispatchers
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.SerializationException

/**
 * Default implementation of [IssueReporterRepository] that posts issues to GitHub.
 */
class DefaultIssueReporterRepository(
    private val client: HttpClient,
    private val dispatchers: AppDispatchers,
    private val baseUrl: String = "https://api.github.com",
) : IssueReporterRepository {

    override suspend fun sendReport(
        report: Report,
        target: GithubTarget,
        token: String?,
    ): IssueReportResult = withContext(dispatchers.io) {
        val url = "$baseUrl/repos/${target.username}/${target.repository}/issues"
        try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                header("Accept", "application/vnd.github+json")
                token?.let { header("Authorization", "Bearer $it") }
                val issueRequest = CreateIssueRequest(
                    title = report.title,
                    body = report.getDescription(),
                    labels = listOf("bug", "from-mobile"),
                )
                setBody(Json.encodeToString(CreateIssueRequest.serializer(), issueRequest))
            }

            val responseBody = response.bodyAsText()
            if (response.status == HttpStatusCode.Created) {
                val json = Json.parseToJsonElement(responseBody).jsonObject
                val issueUrl = json["html_url"]?.jsonPrimitive?.content ?: ""
                IssueReportResult.Success(issueUrl)
            } else {
                mapError(response.status, responseBody)
            }
        } catch (e: IOException) {
            IssueReportResult.Error.Network(e.message ?: "")
        } catch (e: SerializationException) {
            IssueReportResult.Error.Serialization(e.message ?: "")
        }
    }

    private fun mapError(status: HttpStatusCode, body: String): IssueReportResult.Error = when (status) {
        HttpStatusCode.BadRequest -> IssueReportResult.Error.BadRequest(body)
        HttpStatusCode.Unauthorized -> IssueReportResult.Error.Unauthorized(body)
        HttpStatusCode.Forbidden -> IssueReportResult.Error.Forbidden(body)
        else -> IssueReportResult.Error.Unknown(status, body)
    }
}