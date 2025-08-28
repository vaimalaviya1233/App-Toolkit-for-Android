package com.d4rk.android.libs.apptoolkit.app.advanced.domain.model.ui

/**
 * Represents the state for [AdvancedSettingsViewModel].
 *
 * @param cacheClearMessage optional string resource id to show after clearing cache
 */
data class UiAdvancedSettingsScreen(
    val cacheClearMessage: Int? = null,
)
