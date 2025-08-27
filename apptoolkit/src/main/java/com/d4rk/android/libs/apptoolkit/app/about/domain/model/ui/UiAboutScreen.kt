package com.d4rk.android.libs.apptoolkit.app.about.domain.model.ui

/**
 * UI representation for the about screen.
 *
 * Values are loaded by [AboutViewModel] using the provided data sources and are
 * exposed as immutable properties to the UI layer.
 */
data class UiAboutScreen(
    val appVersion: String = "",
    val appVersionCode: Int = 0,
    val deviceInfo: String = "",
)
