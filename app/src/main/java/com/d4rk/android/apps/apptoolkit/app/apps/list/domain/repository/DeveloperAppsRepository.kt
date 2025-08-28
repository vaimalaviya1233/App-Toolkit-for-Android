package com.d4rk.android.apps.apptoolkit.app.apps.list.domain.repository

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface DeveloperAppsRepository {
    fun fetchDeveloperApps(): Flow<List<AppInfo>>
}

