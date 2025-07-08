package com.d4rk.android.libs.apptoolkit.app.issuereporter.ui

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import app.cash.turbine.test
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.actions.IssueReporterEvent
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.UnconfinedDispatcherExtension
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.google.common.truth.Truth.assertThat
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpHeaders
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class TestIssueReporterViewModel : TestIssueReporterViewModelBase() {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = UnconfinedDispatcherExtension()
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
        @Suppress("DEPRECATION") val packageInfo = PackageInfo().apply {
            versionCode = 1
            versionName = "1"
        }

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

            // Consume the emitted states for the three update events
            skipItems(3)
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

    @Test
    fun `send report unauthorized`() = runTest(dispatcherExtension.testDispatcher) {
        val engine = MockEngine { respond("fail", HttpStatusCode.Unauthorized) }
        setup(engine, githubToken = "bad", testDispatcher = dispatcherExtension.testDispatcher)
        @Suppress("DEPRECATION") val packageInfo = PackageInfo().apply {
            versionCode = 1
            versionName = "1"
        }
        val pm = mockk<PackageManager>()
        every { pm.getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns pm
        every { context.packageName } returns "pkg"

        viewModel.uiState.test {
            // Initial state
            awaitItem()

            viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))
            viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))

            // Consume emitted states for the update events
            skipItems(2)
            viewModel.onEvent(IssueReporterEvent.Send(context))

            val loading = awaitItem()
            assertThat(loading.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            val snackbar = state.snackbar!!
            assertThat(snackbar.isError).isTrue()
            val msg = snackbar.message as UiTextHelper.StringResource
            assertThat(msg.resourceId).isEqualTo(R.string.error_unauthorized)
            assertThat(state.screenState).isInstanceOf(ScreenState.Error::class.java)
        }
    }

    @Test
    fun `send report forbidden`() = runTest(dispatcherExtension.testDispatcher) {
        val engine = MockEngine { respond("fail", HttpStatusCode.Forbidden) }
        setup(engine, githubToken = "bad", testDispatcher = dispatcherExtension.testDispatcher)
        @Suppress("DEPRECATION") val packageInfo = PackageInfo().apply {
            versionCode = 1
            versionName = "1"
        }
        val pm = mockk<PackageManager>()
        every { pm.getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns pm
        every { context.packageName } returns "pkg"

        viewModel.uiState.test {
            // Initial state
            awaitItem()

            viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))
            viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))

            // Consume emitted states for the update events
            skipItems(2)
            viewModel.onEvent(IssueReporterEvent.Send(context))

            val loading = awaitItem()
            assertThat(loading.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            val snackbar = state.snackbar!!
            assertThat(snackbar.isError).isTrue()
            val msg = snackbar.message as UiTextHelper.StringResource
            assertThat(msg.resourceId).isEqualTo(R.string.error_forbidden)
            assertThat(state.screenState).isInstanceOf(ScreenState.Error::class.java)
        }
    }

    @Test
    fun `dismiss snackbar clears state`() = runTest(dispatcherExtension.testDispatcher) {
        val engine = MockEngine { respond("", HttpStatusCode.Created) }
        setup(engine, testDispatcher = dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(IssueReporterEvent.Send(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.snackbar).isNotNull()

        viewModel.onEvent(IssueReporterEvent.DismissSnackbar)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.snackbar).isNull()
    }

    @Test
    fun `send report gone`() = runTest(dispatcherExtension.testDispatcher) {
        val engine = MockEngine { respond("gone", HttpStatusCode.Gone) }
        setup(engine, githubToken = "token", testDispatcher = dispatcherExtension.testDispatcher)
        @Suppress("DEPRECATION") val packageInfo = PackageInfo().apply { versionCode = 1; versionName = "1" }
        val pm = mockk<PackageManager>()
        every { pm.getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns pm
        every { context.packageName } returns "pkg"

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))
            viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))
            skipItems(2)
            viewModel.onEvent(IssueReporterEvent.Send(context))

            awaitItem() // loading
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            val snackbar = state.snackbar!!
            assertThat(snackbar.isError).isTrue()
            val msg = snackbar.message as UiTextHelper.StringResource
            assertThat(msg.resourceId).isEqualTo(R.string.error_gone)
            assertThat(state.screenState).isInstanceOf(ScreenState.Error::class.java)
        }
    }

    @Test
    fun `send report unprocessable`() = runTest(dispatcherExtension.testDispatcher) {
        val engine = MockEngine { respond("bad", HttpStatusCode.UnprocessableEntity) }
        setup(engine, githubToken = "token", testDispatcher = dispatcherExtension.testDispatcher)
        @Suppress("DEPRECATION") val packageInfo = PackageInfo().apply { versionCode = 1; versionName = "1" }
        val pm = mockk<PackageManager>()
        every { pm.getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns pm
        every { context.packageName } returns "pkg"

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))
            viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))
            skipItems(2)
            viewModel.onEvent(IssueReporterEvent.Send(context))

            awaitItem()
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            val snackbar = state.snackbar!!
            assertThat(snackbar.isError).isTrue()
            val msg = snackbar.message as UiTextHelper.StringResource
            assertThat(msg.resourceId).isEqualTo(R.string.error_unprocessable)
            assertThat(state.screenState).isInstanceOf(ScreenState.Error::class.java)
        }
    }

    @Test
    fun `send report network exception`() = runTest(dispatcherExtension.testDispatcher) {
        val engine = MockEngine { throw java.net.SocketTimeoutException("timeout") }
        setup(engine, githubToken = "token", testDispatcher = dispatcherExtension.testDispatcher)
        @Suppress("DEPRECATION") val packageInfo = PackageInfo().apply { versionCode = 1; versionName = "1" }
        val pm = mockk<PackageManager>()
        every { pm.getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns pm
        every { context.packageName } returns "pkg"

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))
            viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))
            skipItems(2)
            viewModel.onEvent(IssueReporterEvent.Send(context))

            awaitItem() // loading
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            val snackbar = state.snackbar!!
            assertThat(snackbar.isError).isTrue()
            val msg = snackbar.message as UiTextHelper.StringResource
            assertThat(msg.resourceId).isEqualTo(R.string.snack_report_failed)
            assertThat(state.screenState).isInstanceOf(ScreenState.Error::class.java)
        }
    }

    @Test
    fun `send report without token and empty url`() = runTest(dispatcherExtension.testDispatcher) {
        val engine = MockEngine { respond("{}", HttpStatusCode.Created) }
        setup(engine, githubToken = "", testDispatcher = dispatcherExtension.testDispatcher)
        @Suppress("DEPRECATION") val packageInfo = PackageInfo().apply { versionCode = 1; versionName = "1" }
        val pm = mockk<PackageManager>()
        every { pm.getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns pm
        every { context.packageName } returns "pkg"

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))
            viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))
            skipItems(2)
            viewModel.onEvent(IssueReporterEvent.Send(context))

            awaitItem() // loading
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
            assertThat(state.data?.issueUrl).isEmpty()
            val snackbar = state.snackbar!!
            assertThat(snackbar.isError).isFalse()
        }
    }

    @Test
    fun `send report repository exception`() = runTest(dispatcherExtension.testDispatcher) {
        val engine = MockEngine { throw NullPointerException("boom") }
        setup(engine, githubToken = "tok", testDispatcher = dispatcherExtension.testDispatcher)
        @Suppress("DEPRECATION") val packageInfo = PackageInfo().apply { versionCode = 1; versionName = "1" }
        val pm = mockk<PackageManager>()
        every { pm.getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns pm
        every { context.packageName } returns "pkg"

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))
            viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))
            skipItems(2)
            viewModel.onEvent(IssueReporterEvent.Send(context))

            awaitItem() // loading
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            val snackbar = state.snackbar!!
            assertThat(snackbar.isError).isTrue()
            val msg = snackbar.message as UiTextHelper.StringResource
            assertThat(msg.resourceId).isEqualTo(R.string.snack_report_failed)
            assertThat(state.screenState).isInstanceOf(ScreenState.Error::class.java)
        }
    }

    @Test
    fun `multiple send attempts retain data`() = runTest(dispatcherExtension.testDispatcher) {
        var counter = 1
        val engine = MockEngine { respond("""{"html_url":"https://ex.com/${counter++}"}""", HttpStatusCode.Created) }
        setup(engine, githubToken = "tok", testDispatcher = dispatcherExtension.testDispatcher)
        @Suppress("DEPRECATION") val packageInfo = PackageInfo().apply { versionCode = 1; versionName = "1" }
        val pm = mockk<PackageManager>()
        every { pm.getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns pm
        every { context.packageName } returns "pkg"

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))
            viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))
            skipItems(2)

            viewModel.onEvent(IssueReporterEvent.Send(context))
            awaitItem() // loading
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            val first = awaitItem()
            assertThat(first.data?.issueUrl).isEqualTo("https://ex.com/1")

            viewModel.onEvent(IssueReporterEvent.Send(context))
            awaitItem() // loading
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            val second = awaitItem()
            assertThat(second.data?.issueUrl).isEqualTo("https://ex.com/2")
            assertThat(second.data?.title).isEqualTo("Bug")
            assertThat(second.data?.description).isEqualTo("Desc")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle anonymous back to true`() = runTest(dispatcherExtension.testDispatcher) {
        val engine = MockEngine { respond("", HttpStatusCode.Created) }
        setup(engine, testDispatcher = dispatcherExtension.testDispatcher)

        viewModel.onEvent(IssueReporterEvent.SetAnonymous(false))
        viewModel.onEvent(IssueReporterEvent.SetAnonymous(true))

        assertThat(viewModel.uiState.value.data?.anonymous).isTrue()
    }

    @Test
    fun `update email field variations`() = runTest(dispatcherExtension.testDispatcher) {
        val engine = MockEngine { respond("", HttpStatusCode.Created) }
        setup(engine, testDispatcher = dispatcherExtension.testDispatcher)

        viewModel.onEvent(IssueReporterEvent.UpdateEmail(""))
        assertThat(viewModel.uiState.value.data?.email).isEmpty()

        viewModel.onEvent(IssueReporterEvent.UpdateEmail("me@test.com"))
        assertThat(viewModel.uiState.value.data?.email).isEqualTo("me@test.com")

        viewModel.onEvent(IssueReporterEvent.UpdateEmail("invalid"))
        assertThat(viewModel.uiState.value.data?.email).isEqualTo("invalid")
    }

    @Test
    fun `send report while another in flight`() = runTest(dispatcherExtension.testDispatcher) {
        val engine = MockEngine {
            kotlinx.coroutines.delay(100)
            respond("""{"html_url":"https://ex.com/1"}""", HttpStatusCode.Created)
        }
        setup(engine, githubToken = "tok", testDispatcher = dispatcherExtension.testDispatcher)
        @Suppress("DEPRECATION") val packageInfo = PackageInfo().apply { versionCode = 1; versionName = "1" }
        val pm = mockk<PackageManager>()
        every { pm.getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns pm
        every { context.packageName } returns "pkg"

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))
            viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))
            skipItems(2)

            viewModel.onEvent(IssueReporterEvent.Send(context))
            viewModel.onEvent(IssueReporterEvent.Send(context))

            awaitItem() // loading
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            val final = awaitItem()
            assertThat(final.screenState).isInstanceOf(ScreenState.Success::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `send report when package manager throws`() = runTest(dispatcherExtension.testDispatcher) {
        val engine = MockEngine { respond("""{"html_url":"https://ex.com/1"}""", HttpStatusCode.Created) }
        setup(engine, githubToken = "tok", testDispatcher = dispatcherExtension.testDispatcher)

        val pm = mockk<PackageManager>()
        every { pm.getPackageInfo(any<String>(), any<Int>()) } throws PackageManager.NameNotFoundException("missing")
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns pm
        every { context.packageName } returns "pkg"

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))
            viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))
            skipItems(2)

            viewModel.onEvent(IssueReporterEvent.Send(context))

            awaitItem() // loading
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
            assertThat(state.data?.issueUrl).isEqualTo("https://ex.com/1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `send report with invalid package info`() = runTest(dispatcherExtension.testDispatcher) {
        val engine = MockEngine { respond("""{"html_url":"https://ex.com/1"}""", HttpStatusCode.Created) }
        setup(engine, githubToken = "tok", testDispatcher = dispatcherExtension.testDispatcher)

        @Suppress("DEPRECATION") val packageInfo = PackageInfo().apply { versionCode = -1; versionName = null }
        val pm = mockk<PackageManager>()
        every { pm.getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns pm
        every { context.packageName } returns "pkg"

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))
            viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))
            skipItems(2)

            viewModel.onEvent(IssueReporterEvent.Send(context))

            awaitItem() // loading
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
            assertThat(state.data?.issueUrl).isEqualTo("https://ex.com/1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `send report with only description`() = runTest(dispatcherExtension.testDispatcher) {
        val engine = MockEngine { respond("", HttpStatusCode.Created) }
        setup(engine, testDispatcher = dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(IssueReporterEvent.Send(context))
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            val snackbar = state.snackbar!!
            assertThat(snackbar.isError).isTrue()
            val msg = snackbar.message as UiTextHelper.StringResource
            assertThat(msg.resourceId).isEqualTo(R.string.error_invalid_report)
            assertThat(state.screenState).isNotInstanceOf(ScreenState.IsLoading::class.java)
        }
    }

    @Test
    fun `send report with only title`() = runTest(dispatcherExtension.testDispatcher) {
        val engine = MockEngine { respond("", HttpStatusCode.Created) }
        setup(engine, testDispatcher = dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(IssueReporterEvent.Send(context))
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            val snackbar = state.snackbar!!
            assertThat(snackbar.isError).isTrue()
            val msg = snackbar.message as UiTextHelper.StringResource
            assertThat(msg.resourceId).isEqualTo(R.string.error_invalid_report)
            assertThat(state.screenState).isNotInstanceOf(ScreenState.IsLoading::class.java)
        }
    }

    @Test
    fun `form can be reset after success`() = runTest(dispatcherExtension.testDispatcher) {
        var captured: HttpRequestData? = null
        val engine = MockEngine { request ->
            captured = request
            respond("""{"html_url":"https://ex.com/1"}""", HttpStatusCode.Created)
        }
        setup(engine, githubToken = "tok", testDispatcher = dispatcherExtension.testDispatcher)
        @Suppress("DEPRECATION") val packageInfo = PackageInfo().apply { versionCode = 1; versionName = "1" }
        val pm = mockk<PackageManager>()
        every { pm.getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns pm
        every { context.packageName } returns "pkg"

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))
            viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))
            skipItems(2)

            viewModel.onEvent(IssueReporterEvent.Send(context))
            awaitItem() // loading
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            awaitItem()

            viewModel.onEvent(IssueReporterEvent.UpdateTitle(""))
            viewModel.onEvent(IssueReporterEvent.UpdateDescription(""))
            viewModel.onEvent(IssueReporterEvent.UpdateEmail(""))

            val final = awaitItem()
            assertThat(final.data?.title).isEmpty()
            assertThat(final.data?.description).isEmpty()
            assertThat(final.data?.email).isEmpty()
        }
    }

    @Test
    fun `github token captured at init`() = runTest(dispatcherExtension.testDispatcher) {
        var captured: HttpRequestData? = null
        var token = "bad"
        val engine = MockEngine { request ->
            captured = request
            respond("""{"html_url":"https://ex.com/1"}""", HttpStatusCode.Created)
        }
        setup(engine, githubToken = token, testDispatcher = dispatcherExtension.testDispatcher)
        token = "good" // change after initialization
        @Suppress("DEPRECATION") val packageInfo = PackageInfo().apply { versionCode = 1; versionName = "1" }
        val pm = mockk<PackageManager>()
        every { pm.getPackageInfo(any<String>(), any<Int>()) } returns packageInfo
        val context = mockk<Context>(relaxed = true)
        every { context.packageManager } returns pm
        every { context.packageName } returns "pkg"

        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))
            viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))
            skipItems(2)

            viewModel.onEvent(IssueReporterEvent.Send(context))
            awaitItem() // loading
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            awaitItem()
            assertThat(captured?.headers?.get(HttpHeaders.Authorization)).isEqualTo("Bearer bad")
        }
    }
}
