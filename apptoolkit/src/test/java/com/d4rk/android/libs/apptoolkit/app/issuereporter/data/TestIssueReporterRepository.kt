package com.d4rk.android.libs.apptoolkit.app.issuereporter.data

import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.IssueReportResult
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.Report
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.github.ExtraInfo
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.github.GithubTarget
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class TestIssueReporterRepository {

    @Test
    fun `sendReport returns success`() = runTest {
        println("\uD83D\uDE80 [TEST] repository success")
        var capturedRequest: HttpRequestData? = null
        val engine = MockEngine { request ->
            capturedRequest = request
            val body = """{"html_url":"https://example.com/issue/1"}"""
            respond(content = body, status = HttpStatusCode.Created)
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json() }
        }
        val repository = IssueReporterRepository(client)
        val report = Report("title", "desc", com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.DeviceInfo(android.app.Application()), ExtraInfo(), "me@test.com")
        val target = GithubTarget("user", "repo")
        val result = repository.sendReport(report, target, token = "token123")

        assertThat(result).isInstanceOf(IssueReportResult.Success::class.java)
        assertThat((result as IssueReportResult.Success).url).isEqualTo("https://example.com/issue/1")
        assertThat(capturedRequest?.headers?.get(HttpHeaders.Authorization)).isEqualTo("Bearer token123")
        println("\uD83C\uDFC1 [TEST DONE] repository success")
    }

    @Test
    fun `sendReport returns error`() = runTest {
        println("\uD83D\uDE80 [TEST] repository error")
        val engine = MockEngine { respond("fail", HttpStatusCode.BadRequest) }
        val client = HttpClient(engine) { install(ContentNegotiation) { json() } }
        val repository = IssueReporterRepository(client)
        val report = Report("t", "d", com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.DeviceInfo(android.app.Application()), ExtraInfo(), null)
        val target = GithubTarget("user", "repo")
        val result = repository.sendReport(report, target)

        assertThat(result).isInstanceOf(IssueReportResult.Error::class.java)
        val error = result as IssueReportResult.Error
        assertThat(error.status).isEqualTo(HttpStatusCode.BadRequest)
        assertThat(error.message).isEqualTo("fail")
        println("\uD83C\uDFC1 [TEST DONE] repository error")
    }
}

