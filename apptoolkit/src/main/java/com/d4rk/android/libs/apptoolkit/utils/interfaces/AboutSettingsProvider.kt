package com.d4rk.android.libs.apptoolkit.utils.interfaces

interface AboutSettingsProvider {
    val appName: String
    val appVersion: String
    val copyrightText: String
    val isDebug: Boolean
    val deviceInfo: String
}