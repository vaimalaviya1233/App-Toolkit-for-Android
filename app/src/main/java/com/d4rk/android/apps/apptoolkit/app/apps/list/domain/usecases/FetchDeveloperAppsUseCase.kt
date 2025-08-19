package com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.repository.DeveloperAppsRepository
import com.d4rk.android.apps.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.apps.apptoolkit.core.utils.extensions.toError
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import com.d4rk.android.libs.apptoolkit.core.domain.usecases.RepositoryWithoutParam
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FetchDeveloperAppsUseCase(private val repository: DeveloperAppsRepository) : RepositoryWithoutParam<Flow<DataState<List<AppInfo>, RootError>>> {

    override suspend operator fun invoke(): Flow<DataState<List<AppInfo>, RootError>> = flow {
        runCatching {
            repository.fetchDeveloperApps()
        }.onSuccess { foundApps ->
            emit(DataState.Success(data = foundApps))
        }.onFailure { error ->
            emit(DataState.Error(error = error.toError(default = Errors.UseCase.FAILED_TO_LOAD_APPS)))
        }
    }
}

