package com.d4rk.android.libs.apptoolkit.app.issuereporter.data

import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.DeviceInfo
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.Report
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.github.ExtraInfo
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.github.GithubTarget
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.IssueReportResult
import com.google.common.truth.Truth.assertThat
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class IssueReporterRepositoryTest {

    @Test
    fun `send report success`() = runTest {
        val engine = MockEngine { respond("""{"html_url":"https://ex.com/1"}""", HttpStatusCode.Created) }
        val client = HttpClient(engine)
        val repository = IssueReporterRepository(client)
        val deviceInfo = mockk<DeviceInfo> { every { toMarkdown() } returns "info" }
        val report = Report("Bug", "Desc", deviceInfo, ExtraInfo(), null)

        val result = repository.sendReport(report, GithubTarget("user", "repo"), token = null)
        assertThat(result).isInstanceOf(IssueReportResult.Success::class.java)
    }

    @Test
    fun `send report error`() = runTest {
        val engine = MockEngine { respond("fail", HttpStatusCode.Unauthorized) }
        val client = HttpClient(engine)
        val repository = IssueReporterRepository(client)
        val deviceInfo = mockk<DeviceInfo> { every { toMarkdown() } returns "info" }
        val report = Report("Bug", "Desc", deviceInfo, ExtraInfo(), null)

        val result = repository.sendReport(report, GithubTarget("user", "repo"), token = null)
        assertThat(result).isInstanceOf(IssueReportResult.Error::class.java)
    }
}
