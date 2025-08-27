package com.d4rk.android.libs.apptoolkit.app.permissions.utils.interfaces

import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsConfig
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for providing the permissions configuration to the UI layer.
 *
 * Implementations should be free of Android framework dependencies so the
 * ViewModel can obtain the configuration without requiring a [Context].
 */
interface PermissionsProvider {
    fun providePermissionsConfig(): Flow<SettingsConfig>
}