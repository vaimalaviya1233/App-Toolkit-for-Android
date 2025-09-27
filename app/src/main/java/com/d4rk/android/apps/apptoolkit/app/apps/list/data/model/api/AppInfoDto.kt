package com.d4rk.android.apps.apptoolkit.app.apps.list.data.model.api

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppInfoDto(
    @SerialName("name") val name: String,
    @SerialName("packageName") val packageName: String,
    @SerialName("iconLogo") val iconUrl: String,
    @SerialName("description") val description: String? = null,
    @SerialName("screenshots") val screenshots: List<String>? = null
)

fun AppInfoDto.toDomain(): AppInfo = AppInfo(
    name = name,
    packageName = packageName,
    iconUrl = iconUrl,
    description = description ?: "",
    screenshots = screenshots ?: emptyList()
)
