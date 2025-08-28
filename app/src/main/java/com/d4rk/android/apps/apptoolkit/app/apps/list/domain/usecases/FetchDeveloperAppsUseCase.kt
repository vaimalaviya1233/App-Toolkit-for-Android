package com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.repository.DeveloperAppsRepository
import com.d4rk.android.apps.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.apps.apptoolkit.core.utils.extensions.toError
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import com.d4rk.android.libs.apptoolkit.core.domain.usecases.RepositoryWithoutParam

class FetchDeveloperAppsUseCase(
    private val repository: DeveloperAppsRepository
) : RepositoryWithoutParam<DataState<List<AppInfo>, RootError>> {

    override suspend operator fun invoke(): DataState<List<AppInfo>, RootError> =
        runCatching { repository.fetchDeveloperApps() }
            .fold(
                onSuccess = { apps -> DataState.Success(data = apps) },
                onFailure = { throwable ->
                    DataState.Error(error = throwable.toError(default = Errors.UseCase.FAILED_TO_LOAD_APPS))
                }
            )
}

