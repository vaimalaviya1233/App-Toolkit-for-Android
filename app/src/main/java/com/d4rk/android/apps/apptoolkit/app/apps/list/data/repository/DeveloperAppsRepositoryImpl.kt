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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers

class DeveloperAppsRepositoryImpl(
    private val client: HttpClient
) : DeveloperAppsRepository {

    override fun fetchDeveloperApps(): Flow<DataState<List<AppInfo>, RootError>> = flow {
        try {
            val url = BuildConfig.DEBUG.let { isDebug ->
                val environment = if (isDebug) ApiEnvironments.ENV_DEBUG else ApiEnvironments.ENV_RELEASE
                "${ApiConstants.BASE_REPOSITORY_URL}/$environment${ApiPaths.DEVELOPER_APPS_API}"
            }
            val httpResponse: HttpResponse = client.get(url)
            val apiResponse: ApiResponse = httpResponse.body()

            val apps = apiResponse.data.apps
                .map { it.toDomain() }
                .sortedBy { it.name.lowercase() }

            emit(DataState.Success<List<AppInfo>, RootError>(data = apps))
        } catch (error: Throwable) {
            emit(
                DataState.Error<List<AppInfo>, RootError>(
                    error = error.toError(default = Errors.UseCase.FAILED_TO_LOAD_APPS)
                )
            )
        }
    }.flowOn(Dispatchers.IO)
}

