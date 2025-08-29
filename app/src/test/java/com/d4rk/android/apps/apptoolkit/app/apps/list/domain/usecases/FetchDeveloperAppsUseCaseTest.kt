package com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases

import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.app.apps.list.FakeDeveloperAppsRepository
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import com.google.common.truth.Truth.assertThat
import kotlin.test.assertTrue

class FetchDeveloperAppsUseCaseTest {

    @Test
    fun `invoke emits Loading then Success`() = runTest {
        val apps = listOf(AppInfo("App", "pkg", "url"))
        val useCase = FetchDeveloperAppsUseCase(FakeDeveloperAppsRepository(apps))

        useCase().test {
            assertTrue(awaitItem() is DataState.Loading)
            val success = awaitItem()
            assertTrue(success is DataState.Success)
            assertThat((success as DataState.Success).data).isEqualTo(apps)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke emits Loading then Error when repository throws`() = runTest {
        val useCase = FetchDeveloperAppsUseCase(
            FakeDeveloperAppsRepository(emptyList(), RuntimeException("fail"))
        )

        useCase().test {
            assertTrue(awaitItem() is DataState.Loading)
            val error = awaitItem()
            assertTrue(error is DataState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
