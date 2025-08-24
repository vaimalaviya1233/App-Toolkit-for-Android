package com.d4rk.android.libs.apptoolkit.app.issuereporter.ui

import android.content.Context
import app.cash.turbine.test
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.actions.IssueReporterEvent
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.github.GithubTarget
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.google.common.truth.Truth.assertThat
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class IssueReporterViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(engine: MockEngine): IssueReporterViewModel {
        val client = HttpClient(engine)
        return IssueReporterViewModel(client, GithubTarget("user", "repo"), githubToken = "")
    }

    @Test
    fun `update fields`() = runTest(dispatcher) {
        val viewModel = createViewModel(MockEngine { respond("", HttpStatusCode.Created) })

        viewModel.onEvent(IssueReporterEvent.UpdateTitle("Bug"))
        viewModel.onEvent(IssueReporterEvent.UpdateDescription("Desc"))
        viewModel.onEvent(IssueReporterEvent.UpdateEmail("me@test.com"))
        viewModel.onEvent(IssueReporterEvent.SetAnonymous(false))

        val data = viewModel.screenState.value.data!!
        assertThat(data.title).isEqualTo("Bug")
        assertThat(data.description).isEqualTo("Desc")
        assertThat(data.email).isEqualTo("me@test.com")
        assertThat(data.anonymous).isFalse()
    }

    @Test
    fun `send report invalid shows snackbar`() = runTest(dispatcher) {
        val viewModel = createViewModel(MockEngine { respond("", HttpStatusCode.Created) })
        val context = mockk<Context>(relaxed = true)

        viewModel.screenState.test {
            awaitItem() // initial state
            viewModel.onEvent(IssueReporterEvent.Send(context))
            dispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            val snackbar = state.snackbar!!
            assertThat(snackbar.isError).isTrue()
            val message = snackbar.message as UiTextHelper.StringResource
            assertThat(message.resourceId).isEqualTo(R.string.error_invalid_report)
            assertThat(state.screenState).isNotInstanceOf(ScreenState.IsLoading::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
