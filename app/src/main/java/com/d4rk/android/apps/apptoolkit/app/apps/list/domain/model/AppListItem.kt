package com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface AppListItem {
    @Immutable
    data class App(val appInfo: AppInfo) : AppListItem
    data object Ad : AppListItem
}