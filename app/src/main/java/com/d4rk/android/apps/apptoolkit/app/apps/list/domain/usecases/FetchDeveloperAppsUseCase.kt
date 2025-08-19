package com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.repository.DeveloperAppsRepository
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import com.d4rk.android.libs.apptoolkit.core.domain.usecases.RepositoryWithoutParam
import kotlinx.coroutines.flow.Flow

class FetchDeveloperAppsUseCase(
    private val repository: DeveloperAppsRepository
) : RepositoryWithoutParam<Flow<DataState<List<AppInfo>, RootError>>> {

    override suspend operator fun invoke(): Flow<DataState<List<AppInfo>, RootError>> =
        repository.fetchDeveloperApps()
}

