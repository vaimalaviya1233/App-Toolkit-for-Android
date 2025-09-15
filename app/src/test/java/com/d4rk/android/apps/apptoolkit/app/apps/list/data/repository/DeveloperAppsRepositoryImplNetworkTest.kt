package com.d4rk.android.apps.apptoolkit.app.apps.list.data.repository

import com.d4rk.android.apps.apptoolkit.app.apps.list.data.model.api.ApiResponse
import com.d4rk.android.apps.apptoolkit.app.apps.list.data.model.api.AppDataWrapper
import com.d4rk.android.apps.apptoolkit.app.apps.list.data.model.api.AppInfoDto
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.statement.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DeveloperAppsRepositoryImplNetworkTest {

    private val baseUrl = "https://example.com"

    @Test
    fun `fetchDeveloperApps returns mapped and sorted apps when request succeeds`() = runTest {
        val unsortedDtos = listOf(
            AppInfoDto(name = "zeta", packageName = "pkg.z", iconUrl = "icon_z"),
            AppInfoDto(name = "Alpha", packageName = "pkg.a", iconUrl = "icon_a"),
            AppInfoDto(name = "beta", packageName = "pkg.b", iconUrl = "icon_b"),
        )
        val apiResponse = ApiResponse(AppDataWrapper(unsortedDtos))
        val json = Json.encodeToString(apiResponse)

        val repository = createRepository { _ ->
            respond(
                content = json,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val result = repository.fetchDeveloperApps().first()
        val success = assertIs<DataState.Success<List<AppInfo>, Errors>>(result)
        assertEquals(listOf("Alpha", "beta", "zeta"), success.data.map(AppInfo::name))
        assertEquals(listOf("pkg.a", "pkg.b", "pkg.z"), success.data.map(AppInfo::packageName))
        assertEquals(listOf("icon_a", "icon_b", "icon_z"), success.data.map(AppInfo::iconUrl))
    }

    @Test
    fun `fetchDeveloperApps emits request timeout when http status is request timeout`() = runTest {
        val repository = createRepository(expectSuccess = false) { _ ->
            respond(
                content = "",
                status = HttpStatusCode.RequestTimeout,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val result = repository.fetchDeveloperApps().first()
        val error = assertIs<DataState.Error<List<AppInfo>, Errors>>(result)
        assertEquals(Errors.Network.REQUEST_TIMEOUT, error.error)
    }

    @Test
    fun `fetchDeveloperApps emits failed to load error when http status is server error`() = runTest {
        val repository = createRepository(expectSuccess = false) { _ ->
            respond(
                content = "",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val result = repository.fetchDeveloperApps().first()
        val error = assertIs<DataState.Error<List<AppInfo>, Errors>>(result)
        assertEquals(Errors.UseCase.FAILED_TO_LOAD_APPS, error.error)
    }

    @Test
    fun `fetchDeveloperApps emits request timeout when socket timeout exception occurs`() = runTest {
        val repository = createRepository { _ ->
            throw SocketTimeoutException("timeout")
        }

        val result = repository.fetchDeveloperApps().first()
        val error = assertIs<DataState.Error<List<AppInfo>, Errors>>(result)
        assertEquals(Errors.Network.REQUEST_TIMEOUT, error.error)
    }

    @Test
    fun `fetchDeveloperApps emits no internet error when io exception occurs`() = runTest {
        val repository = createRepository { _ ->
            throw IOException("no network")
        }

        val result = repository.fetchDeveloperApps().first()
        val error = assertIs<DataState.Error<List<AppInfo>, Errors>>(result)
        assertEquals(Errors.Network.NO_INTERNET, error.error)
    }

    @Test
    fun `fetchDeveloperApps emits failed to load error when body parsing fails`() = runTest {
        val repository = createRepository { _ ->
            respond(
                content = "{\"invalid\":true}",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val result = repository.fetchDeveloperApps().first()
        val error = assertIs<DataState.Error<List<AppInfo>, Errors>>(result)
        assertEquals(Errors.UseCase.FAILED_TO_LOAD_APPS, error.error)
    }

    @Test
    fun `fetchDeveloperApps emits failed to load error when client request exception is thrown`() = runTest {
        val repository = createRepository(expectSuccess = true) { _ ->
            respondError(HttpStatusCode.BadRequest)
        }

        val result = repository.fetchDeveloperApps().first()
        val error = assertIs<DataState.Error<List<AppInfo>, Errors>>(result)
        assertEquals(Errors.UseCase.FAILED_TO_LOAD_APPS, error.error)
    }

    @Test
    fun `fetchDeveloperApps emits request timeout error when client request exception is timeout`() = runTest {
        val repository = createRepository(expectSuccess = true) { _ ->
            respondError(HttpStatusCode.RequestTimeout)
        }

        val result = repository.fetchDeveloperApps().first()
        val error = assertIs<DataState.Error<List<AppInfo>, Errors>>(result)
        assertEquals(Errors.Network.REQUEST_TIMEOUT, error.error)
    }

    private fun createRepository(
        expectSuccess: Boolean = true,
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): DeveloperAppsRepositoryImpl {
        val client = HttpClient(MockEngine(handler)) {
            this.expectSuccess = expectSuccess
            install(ContentNegotiation) { json() }
        }
        return DeveloperAppsRepositoryImpl(client, baseUrl)
    }
}
