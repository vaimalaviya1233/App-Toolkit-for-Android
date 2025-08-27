package com.d4rk.android.apps.apptoolkit.app.apps.list.data.repository

import com.d4rk.android.apps.apptoolkit.app.apps.list.data.model.api.ApiResponse
import com.d4rk.android.apps.apptoolkit.app.apps.list.data.model.api.AppDataWrapper
import com.d4rk.android.apps.apptoolkit.app.apps.list.data.model.api.AppInfoDto
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeveloperAppsRepositoryImplTest {

    @Test
    fun `fetchDeveloperApps emits loading then success`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val apps = listOf(AppInfo("App", "pkg", "icon"))
        val response = ApiResponse(AppDataWrapper(apps.map { AppInfoDto(it.name, it.packageName, it.iconUrl) }))
        val json = Json.encodeToString(response)
        val client = HttpClient(MockEngine { request ->
            respond(
                content = json,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }) {
            install(ContentNegotiation) { json() }
        }
        val repository = DeveloperAppsRepositoryImpl(client, testDispatcher)

        val flow = repository.fetchDeveloperApps()
        val loading = flow.first()
        assertTrue(loading is DataState.Loading)

        val results = flow.drop(1).toList()
        assertEquals(1, results.size)
        val success = results[0]
        assertTrue(success is DataState.Success)
        assertEquals(apps, success.data)
    }

    @Test
    fun `fetchDeveloperApps emits loading then error`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val client = HttpClient(MockEngine { request ->
            respond(
                content = "",
                status = HttpStatusCode.RequestTimeout,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }) {
            install(ContentNegotiation) { json() }
        }
        val repository = DeveloperAppsRepositoryImpl(client, testDispatcher)

        val flow = repository.fetchDeveloperApps()
        val loading = flow.first()
        assertTrue(loading is DataState.Loading)

        val results = flow.drop(1).toList()
        assertEquals(1, results.size)
        val errorState = results[0]
        assertTrue(errorState is DataState.Error)
        assertEquals(Errors.Network.REQUEST_TIMEOUT, errorState.error)
    }
}

