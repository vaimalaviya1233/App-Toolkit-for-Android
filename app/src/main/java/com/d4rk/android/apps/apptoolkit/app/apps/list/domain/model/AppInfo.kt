package com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppInfo(
    @SerialName("name") val name: String ,
    @SerialName("packageName") val packageName: String ,
    @SerialName("iconLogo") val iconUrl: String
)