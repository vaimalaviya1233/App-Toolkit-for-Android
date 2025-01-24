package com.d4rk.android.libs.apptoolkit.utils.interfaces.providers

interface AboutSettingsProvider {
    val appName : String
    val packageName : String
    val appVersion : String
    val appVersionCode : Int
    val copyrightText : String
    val deviceInfo : String
}