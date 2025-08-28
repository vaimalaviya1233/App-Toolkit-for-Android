package com.d4rk.android.apps.apptoolkit.app.apps.list.data.repository

import com.d4rk.android.apps.apptoolkit.BuildConfig
import com.d4rk.android.apps.apptoolkit.app.apps.list.data.model.api.ApiResponse
import com.d4rk.android.apps.apptoolkit.app.apps.list.data.model.api.toDomain
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.repository.DeveloperAppsRepository
import com.d4rk.android.apps.apptoolkit.app.apps.list.utils.constants.api.ApiConstants
import com.d4rk.android.apps.apptoolkit.app.apps.list.utils.constants.api.ApiEnvironments
import com.d4rk.android.apps.apptoolkit.app.apps.list.utils.constants.api.ApiPaths
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException

class DeveloperAppsRepositoryImpl(
    private val client: HttpClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : DeveloperAppsRepository {

    override suspend fun fetchDeveloperApps(): List<AppInfo> = withContext(ioDispatcher) {
        val url = BuildConfig.DEBUG.let { isDebug ->
            val environment = if (isDebug) ApiEnvironments.ENV_DEBUG else ApiEnvironments.ENV_RELEASE
            "${ApiConstants.BASE_REPOSITORY_URL}/$environment${ApiPaths.DEVELOPER_APPS_API}"
        }

        val httpResponse: HttpResponse = client.get(url)
        if (!httpResponse.status.isSuccess()) {
            if (httpResponse.status == HttpStatusCode.RequestTimeout) {
                throw SocketTimeoutException("Request timeout")
            } else {
                throw IllegalStateException("Failed to load apps")
            }
        }

        val apiResponse: ApiResponse = httpResponse.body()
        apiResponse.data.apps
            .map { it.toDomain() }
            .sortedBy { it.name.lowercase() }
    }
}

