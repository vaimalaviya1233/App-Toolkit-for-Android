package com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases

import com.d4rk.android.apps.apptoolkit.app.apps.list.FakeDeveloperAppsRepository
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.StandardDispatcherExtension
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class FetchDeveloperAppsUseCaseTest {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = StandardDispatcherExtension()
    }

    @Test
    fun `emits loading then success`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "url"))
        val repository = FakeDeveloperAppsRepository(apps)
        val useCase = FetchDeveloperAppsUseCase(repository)

        val emissions = useCase().toList()
        assertThat(emissions[0]).isInstanceOf(DataState.Loading::class.java)
        assertThat(emissions[1]).isEqualTo(DataState.Success(apps))
    }

    @Test
    fun `emits loading then error`() = runTest(dispatcherExtension.testDispatcher) {
        val repository = FakeDeveloperAppsRepository(emptyList(), fetchThrows = RuntimeException("fail"))
        val useCase = FetchDeveloperAppsUseCase(repository)

        val emissions = useCase().toList()
        assertThat(emissions[0]).isInstanceOf(DataState.Loading::class.java)
        assertThat(emissions[1]).isInstanceOf(DataState.Error::class.java)
    }
}

