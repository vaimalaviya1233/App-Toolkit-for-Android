package com.d4rk.android.apps.apptoolkit.app.home.domain.model

data class UiHomeScreen(
    val apps : List<AppInfo> = emptyList()
)

sealed interface AppListItem {
    data class App(val appInfo : AppInfo) : AppListItem
    data object Ad : AppListItem
}