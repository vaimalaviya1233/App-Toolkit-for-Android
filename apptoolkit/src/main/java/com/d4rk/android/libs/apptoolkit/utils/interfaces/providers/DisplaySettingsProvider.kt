package com.d4rk.android.libs.apptoolkit.utils.interfaces.providers

import android.content.Context
import androidx.compose.runtime.Composable

interface DisplaySettingsProvider {

    val supportsStartupPage: Boolean
        get() = false

    @Composable
    fun StartupPageDialog(
        onDismiss: () -> Unit,
        onStartupSelected: (String) -> Unit
    )

    @Composable
    fun LanguageSelectionDialog(
        onDismiss: () -> Unit,
        onLanguageSelected: (String) -> Unit
    )

    fun openThemeSettings()
}