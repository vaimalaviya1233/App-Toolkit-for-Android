package com.d4rk.android.apps.apptoolkit.app.apps.list.data.repository

import com.d4rk.android.apps.apptoolkit.app.apps.list.data.model.api.ApiResponse
import com.d4rk.android.apps.apptoolkit.app.apps.list.data.model.api.AppDataWrapper
import com.d4rk.android.apps.apptoolkit.app.apps.list.data.model.api.AppInfoDto
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import java.net.SocketTimeoutException

class DeveloperAppsRepositoryImplTest {

    @Test
    fun `fetchDeveloperApps returns apps list`() = runTest {
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

        val result = repository.fetchDeveloperApps()
        assertEquals(apps, result)
    }

    @Test
    fun `fetchDeveloperApps throws on error`() = runTest {
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

        val result = runCatching { repository.fetchDeveloperApps() }
        val exception = result.exceptionOrNull()
        assertIs<SocketTimeoutException>(exception)
    }
}

