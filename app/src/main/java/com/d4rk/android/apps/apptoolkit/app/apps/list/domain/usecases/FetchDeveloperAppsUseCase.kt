package com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.repository.DeveloperAppsRepository
import com.d4rk.android.apps.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.apps.apptoolkit.core.utils.extensions.toError
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class FetchDeveloperAppsUseCase(
    private val repository: DeveloperAppsRepository
) {

    operator fun invoke(): Flow<DataState<List<AppInfo>, RootError>> =
        repository.fetchDeveloperApps()
            .map<List<AppInfo>, DataState<List<AppInfo>, RootError>> { apps ->
                DataState.Success(data = apps)
            }
            .onStart { emit(DataState.Loading()) }
            .catch { throwable ->
                emit(DataState.Error(error = throwable.toError(default = Errors.UseCase.FAILED_TO_LOAD_APPS)))
            }
}

