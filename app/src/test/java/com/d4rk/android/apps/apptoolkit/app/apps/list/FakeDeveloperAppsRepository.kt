package com.d4rk.android.apps.apptoolkit.app.apps.list

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.repository.DeveloperAppsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Fake implementation of [DeveloperAppsRepository] that returns a predefined list.
 * It can optionally throw an exception when [fetchDeveloperApps] is called.
 */
class FakeDeveloperAppsRepository(
    private val apps: List<AppInfo>,
    private val fetchThrows: Throwable? = null,
) : DeveloperAppsRepository {
    override fun fetchDeveloperApps(): Flow<List<AppInfo>> = flow {
        fetchThrows?.let { throw it }
        emit(apps)
    }
}

