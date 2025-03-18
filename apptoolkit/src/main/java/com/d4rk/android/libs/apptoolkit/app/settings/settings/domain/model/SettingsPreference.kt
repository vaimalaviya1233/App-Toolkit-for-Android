package com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class SettingsPreference(
    val key: String ,
    val icon: ImageVector ,
    val title: String ,
    val summary: String? = null ,
    val action: () -> Unit
)