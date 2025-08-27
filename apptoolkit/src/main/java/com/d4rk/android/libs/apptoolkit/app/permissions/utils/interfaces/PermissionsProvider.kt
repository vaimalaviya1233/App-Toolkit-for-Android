package com.d4rk.android.libs.apptoolkit.app.permissions.utils.interfaces

import android.content.Context
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsConfig
import kotlinx.coroutines.flow.Flow

interface PermissionsProvider {
    fun providePermissionsConfig(context: Context): Flow<SettingsConfig>
}