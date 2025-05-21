package com.d4rk.android.apps.apptoolkit.app.home.domain.usecases

import com.d4rk.android.apps.apptoolkit.BuildConfig
import com.d4rk.android.apps.apptoolkit.app.home.data.model.api.ApiResponse
import com.d4rk.android.apps.apptoolkit.app.home.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.home.utils.constants.api.ApiConstants
import com.d4rk.android.apps.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.apps.apptoolkit.core.utils.extensions.toError
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import com.d4rk.android.libs.apptoolkit.core.domain.usecases.RepositoryWithoutParam
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FetchDeveloperAppsUseCase(private val client: HttpClient) :
    RepositoryWithoutParam<Flow<DataState<List<AppInfo>, RootError>>> {

    private val appsApiUrl: String = BuildConfig.DEBUG.let { isDebug ->
        val environment = if (isDebug) ApiConstants.ENV_DEBUG else ApiConstants.ENV_RELEASE
        "${ApiConstants.BASE_REPOSITORY_URL}/$environment/${ApiConstants.DEVELOPER_APPS_API_PATH}"
    }

    override suspend operator fun invoke(): Flow<DataState<List<AppInfo>, RootError>> = flow {
        runCatching {
            val response: ApiResponse = client.get(appsApiUrl).body()
            val sortedApps = response.data.apps.sortedBy { it.name.lowercase() }
            sortedApps
        }.onSuccess { foundApps ->
            if (foundApps.isEmpty()) {
                emit(DataState.Error(error = Errors.UseCase.FAILED_TO_LOAD_APPS))
            } else {
                emit(DataState.Success(data = foundApps))
            }
        }.onFailure { error ->
            emit(DataState.Error(error = error.toError(default = Errors.UseCase.FAILED_TO_LOAD_APPS)))
        }
    }
}