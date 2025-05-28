package com.d4rk.android.apps.apptoolkit.app.apps.domain.model

sealed interface AppListItem {
    data class App(val appInfo : AppInfo) : AppListItem
    data object Ad : AppListItem
}