package com.d4rk.android.apps.apptoolkit.app.apps.list.domain.repository

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo

interface DeveloperAppsRepository {
    suspend fun fetchDeveloperApps(): List<AppInfo>
}

