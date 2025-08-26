package com.d4rk.android.apps.apptoolkit.app.apps.list

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.repository.DeveloperAppsRepository
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import kotlinx.coroutines.flow.Flow

/**
 * Fake implementation of [DeveloperAppsRepository] that returns a predefined flow.
 * It can optionally throw an exception when [fetchDeveloperApps] is called.
 */
class FakeDeveloperAppsRepository(
    private val flow: Flow<DataState<List<AppInfo>, RootError>>,
    private val fetchThrows: Throwable? = null,
) : DeveloperAppsRepository {
    override fun fetchDeveloperApps(): Flow<DataState<List<AppInfo>, RootError>> {
        fetchThrows?.let { throw it }
        return flow
    }
}

