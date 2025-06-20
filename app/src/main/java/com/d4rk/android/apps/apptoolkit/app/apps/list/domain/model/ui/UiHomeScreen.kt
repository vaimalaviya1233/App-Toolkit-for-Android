package com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.ui

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo


data class UiHomeScreen(
    val apps : List<AppInfo> = emptyList()
)