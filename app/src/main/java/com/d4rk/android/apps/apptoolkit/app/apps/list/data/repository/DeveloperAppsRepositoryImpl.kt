package com.d4rk.android.apps.apptoolkit.app.apps.list.data.repository

import com.d4rk.android.apps.apptoolkit.BuildConfig
import com.d4rk.android.apps.apptoolkit.app.apps.list.data.model.api.ApiResponse
import com.d4rk.android.apps.apptoolkit.app.apps.list.data.model.api.toDomain
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.repository.DeveloperAppsRepository
import com.d4rk.android.apps.apptoolkit.app.apps.list.utils.constants.api.ApiConstants
import com.d4rk.android.apps.apptoolkit.app.apps.list.utils.constants.api.ApiEnvironments
import com.d4rk.android.apps.apptoolkit.app.apps.list.utils.constants.api.ApiPaths
import com.d4rk.android.apps.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.apps.apptoolkit.core.utils.extensions.toError
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class DeveloperAppsRepositoryImpl(
    private val client: HttpClient,
    private val dispatcher: CoroutineDispatcher,
) : DeveloperAppsRepository {

    override fun fetchDeveloperApps(): Flow<DataState<List<AppInfo>, RootError>> = flow {
        emit(DataState.Loading<List<AppInfo>, RootError>())
        runCatching {
            val url = BuildConfig.DEBUG.let { isDebug ->
                val environment = if (isDebug) ApiEnvironments.ENV_DEBUG else ApiEnvironments.ENV_RELEASE
                "${ApiConstants.BASE_REPOSITORY_URL}/$environment${ApiPaths.DEVELOPER_APPS_API}"
            }
            val httpResponse: HttpResponse = client.get(url)
            if (!httpResponse.status.isSuccess()) {
                val rootError = when (httpResponse.status) {
                    HttpStatusCode.RequestTimeout -> Errors.Network.REQUEST_TIMEOUT
                    else -> Errors.UseCase.FAILED_TO_LOAD_APPS
                }
                emit(DataState.Error<List<AppInfo>, RootError>(error = rootError))
                return@flow
            }

            val apiResponse: ApiResponse = httpResponse.body<ApiResponse>()

            apiResponse.data.apps
                .map { it.toDomain() }
                .sortedBy { it.name.lowercase() }
        }.onSuccess { apps ->
            emit(DataState.Success(data = apps))
        }.onFailure { error ->
            emit(
                DataState.Error<List<AppInfo>, RootError>(
                    error = error.toError(default = Errors.UseCase.FAILED_TO_LOAD_APPS)
                )
            )
        }
    }.flowOn(dispatcher)
}

