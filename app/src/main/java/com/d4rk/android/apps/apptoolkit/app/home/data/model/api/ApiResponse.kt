package com.d4rk.android.apps.apptoolkit.app.home.data.model.api

import com.d4rk.android.apps.apptoolkit.app.home.domain.model.AppInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(
    @SerialName("data") val data: AppData
)

@Serializable
data class AppData(
    @SerialName("apps") val apps: List<AppInfo>
)