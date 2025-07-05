package com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppInfo(
    @SerialName(value = "name") val name: String,
    @SerialName(value = "packageName") val packageName: String,
    @SerialName(value = "iconLogo") val iconUrl: String,
)