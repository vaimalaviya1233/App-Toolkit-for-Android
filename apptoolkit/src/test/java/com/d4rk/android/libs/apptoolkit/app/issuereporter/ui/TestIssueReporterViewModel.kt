package com.d4rk.android.libs.apptoolkit.app.issuereporter.ui

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.MainDispatcherExtension
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.actions.IssueReporterEvent
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class TestIssueReporterViewModel : TestIssueReporterViewModelBase() {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = MainDispatcherExtension()
    }

    @Test
    fun `update fields`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] update fields")
        val engine = MockEngine { respond("", HttpStatusCode.Created) }
        setup(engine, testDispatcher = dispatcherExtension.testDispatcher)

        viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))
        viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))
        viewModel.onEvent(IssueReporterEvent.UpdateEmail("me@test.com"))
        viewModel.onEvent(IssueReporterEvent.SetAnonymous(false))

        val data = viewModel.uiState.value.data!!
        assert(data.title == "Bug")
        assert(data.description == "Desc")
        assert(data.email == "me@test.com")
        assert(!data.anonymous)
        println("\uD83C\uDFC1 [TEST DONE] update fields")
    }

    @Test
    fun `send report invalid`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] send report invalid")
        val engine = MockEngine { respond("", HttpStatusCode.Created) }
        setup(engine, testDispatcher = dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(IssueReporterEvent.Send(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        val snackbar = state.snackbar!!
        assert(snackbar.isError)
        val msg = snackbar.message as UiTextHelper.StringResource
        assert(msg.resourceId == R.string.error_invalid_report)
        assert(state.screenState is ScreenState.IsLoading == false)
        println("\uD83C\uDFC1 [TEST DONE] send report invalid")
    }

    @Test
    fun `send report success`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] send report success")
        val engine = MockEngine { respond("""{\"html_url\":\"https://ex.com/1\"}""", HttpStatusCode.Created) }
        setup(engine, githubToken = "token", testDispatcher = dispatcherExtension.testDispatcher)
        val packageInfo = PackageInfo().apply { versionCode = 1; versionName = "1" }
        val pm = mockk<PackageManager>()
        every { pm.getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns pm
        every { context.packageName } returns "pkg"

        viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))
        viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))
        viewModel.onEvent(IssueReporterEvent.UpdateEmail("me@test.com"))
        viewModel.onEvent(IssueReporterEvent.Send(context))

        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        val snackbar = state.snackbar!!
        assert(!snackbar.isError)
        assert((snackbar.message as UiTextHelper.StringResource).resourceId == R.string.snack_report_success)
        assert(state.data?.issueUrl == "https://ex.com/1")
        assert(state.screenState is ScreenState.Success)
        println("\uD83C\uDFC1 [TEST DONE] send report success")
    }
}

