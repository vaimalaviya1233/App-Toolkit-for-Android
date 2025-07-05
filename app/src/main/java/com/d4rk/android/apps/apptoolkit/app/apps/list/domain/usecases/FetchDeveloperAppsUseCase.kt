package com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases

import com.d4rk.android.apps.apptoolkit.BuildConfig
import com.d4rk.android.apps.apptoolkit.app.apps.list.data.model.api.ApiResponse
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.utils.constants.api.ApiConstants
import com.d4rk.android.apps.apptoolkit.app.apps.list.utils.constants.api.ApiEnvironments
import com.d4rk.android.apps.apptoolkit.app.apps.list.utils.constants.api.ApiPaths
import com.d4rk.android.apps.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.apps.apptoolkit.core.utils.extensions.toError
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import com.d4rk.android.libs.apptoolkit.core.domain.usecases.RepositoryWithoutParam
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FetchDeveloperAppsUseCase(private val client: HttpClient) : RepositoryWithoutParam<Flow<DataState<List<AppInfo> , RootError>>> {

    override suspend operator fun invoke(): Flow<DataState<List<AppInfo> , RootError>> = flow {
        runCatching {
            val url = BuildConfig.DEBUG.let { isDebug ->
                val environment = if (isDebug) ApiEnvironments.ENV_DEBUG else ApiEnvironments.ENV_RELEASE
                "${ApiConstants.BASE_REPOSITORY_URL}/$environment${ApiPaths.DEVELOPER_APPS_API}"
            }
            val httpResponse: HttpResponse = client.get(url)
            val apiResponse: ApiResponse = httpResponse.body()

            val sortedApps = apiResponse.data.apps.sortedBy { it.name.lowercase() }
            sortedApps
        }.onSuccess { foundApps ->
            emit(DataState.Success(data = foundApps))
        }.onFailure { error ->
            emit(DataState.Error(error = error.toError(default = Errors.UseCase.FAILED_TO_LOAD_APPS)))
        }
    }
}