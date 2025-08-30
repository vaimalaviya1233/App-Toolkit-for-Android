package com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.repository.DeveloperAppsRepository
import com.d4rk.android.apps.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.net.SocketTimeoutException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FetchDeveloperAppsUseCaseTest {

    @Test
    fun `invoke emits loading then success`() = runTest {
        val apps = listOf(AppInfo(name = "App", packageName = "pkg", iconUrl = "icon"))
        val repository = object : DeveloperAppsRepository {
            override fun fetchDeveloperApps(): Flow<List<AppInfo>> = flow { emit(apps) }
        }
        val useCase = FetchDeveloperAppsUseCase(repository)

        val emissions = useCase().toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is DataState.Loading)
        val success = emissions[1] as DataState.Success
        assertEquals(apps, success.data)
    }

    @Test
    fun `invoke emits error when repository fails`() = runTest {
        val repository = object : DeveloperAppsRepository {
            override fun fetchDeveloperApps(): Flow<List<AppInfo>> = flow { throw SocketTimeoutException("timeout") }
        }
        val useCase = FetchDeveloperAppsUseCase(repository)

        val emissions = useCase().toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is DataState.Loading)
        val errorState = emissions[1] as DataState.Error
        assertEquals(Errors.Network.REQUEST_TIMEOUT, errorState.error)
    }
}

