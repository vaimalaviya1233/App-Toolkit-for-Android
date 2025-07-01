package com.d4rk.android.libs.apptoolkit.app.issuereporter.ui

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import app.cash.turbine.test // <-- ADDED THIS IMPORT
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.MainDispatcherExtension
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.actions.IssueReporterEvent
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.google.common.truth.Truth.assertThat
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
        println("🚀 [TEST] update fields")
        val engine = MockEngine { respond("", HttpStatusCode.Created) }
        setup(engine, testDispatcher = dispatcherExtension.testDispatcher)

        viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))
        viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))
        viewModel.onEvent(IssueReporterEvent.UpdateEmail("me@test.com"))
        viewModel.onEvent(IssueReporterEvent.SetAnonymous(false))

        val data = viewModel.uiState.value.data!!
        assertThat(data.title).isEqualTo("Bug")
        assertThat(data.description).isEqualTo("Desc")
        assertThat(data.email).isEqualTo("me@test.com")
        assertThat(data.anonymous).isFalse()
        println("🏁 [TEST DONE] update fields")
    }

    @Test
    fun `send report invalid`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] send report invalid")
        val engine = MockEngine { respond("", HttpStatusCode.Created) }
        setup(engine, testDispatcher = dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)

        viewModel.uiState.test {
            // Initial state
            awaitItem()

            // Trigger event
            viewModel.onEvent(IssueReporterEvent.Send(context))
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

            // Get final state
            val finalState = awaitItem()
            val snackbar = finalState.snackbar!!

            // Assertions
            assertThat(snackbar.isError).isTrue()
            val msg = snackbar.message as UiTextHelper.StringResource
            assertThat(msg.resourceId).isEqualTo(R.string.error_invalid_report)
            assertThat(finalState.screenState).isNotInstanceOf(ScreenState.IsLoading::class.java)
        }
        println("🏁 [TEST DONE] send report invalid")
    }

    @Test
    fun `send report success`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] send report success")
        val engine = MockEngine { respond("""{"html_url":"https://ex.com/1"}""", HttpStatusCode.Created) }
        setup(engine, githubToken = "token", testDispatcher = dispatcherExtension.testDispatcher)
        val packageInfo = PackageInfo().apply { versionCode = 1; versionName = "1" }
        val pm = mockk<PackageManager>()
        every { pm.getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns pm
        every { context.packageName } returns "pkg"

        viewModel.uiState.test {
            // 1. Await and consume the flow's initial state
            val initialState = awaitItem()
            assertThat(initialState.screenState).isInstanceOf(ScreenState.Success::class.java)

            // 2. Populate the form and trigger sending
            viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))
            viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))
            viewModel.onEvent(IssueReporterEvent.UpdateEmail("me@test.com"))
            viewModel.onEvent(IssueReporterEvent.Send(context))

            // 3. Loading state should be emitted next
            val loadingState = awaitItem()
            assertThat(loadingState.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            // 4. Advance dispatcher to perform the network call
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

            // 5. Final success state
            val successState = awaitItem()
            val snackbar = successState.snackbar!!

            // Assertions
            assertThat(successState.screenState).isInstanceOf(ScreenState.Success::class.java)
            assertThat(successState.data?.issueUrl).isEqualTo("https://ex.com/1")
            assertThat(snackbar.isError).isFalse()
            assertThat((snackbar.message as UiTextHelper.StringResource).resourceId)
                .isEqualTo(R.string.snack_report_success)
        }

        println("🏁 [TEST DONE] send report success")
    }
}