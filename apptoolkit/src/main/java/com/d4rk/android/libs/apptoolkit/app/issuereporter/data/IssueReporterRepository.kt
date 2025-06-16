package com.d4rk.android.libs.apptoolkit.app.issuereporter.data

import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.CreateIssueRequest
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.Report
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.github.GithubTarget
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

class IssueReporterRepository(private val client : HttpClient) {

    suspend fun sendReport(report : Report , target : GithubTarget , token : String? = null) : Boolean {
        val url = "https://api.github.com/repos/${target.username}/${target.repository}/issues"
        val response : HttpResponse = client.post(url) {
            contentType(ContentType.Application.Json)
            token?.let { header("Authorization" , "Bearer $it") }
            setBody(Json.encodeToString(CreateIssueRequest(title = report.title , body = report.getDescription())))
        }
        return response.status == HttpStatusCode.Created
    }
}
