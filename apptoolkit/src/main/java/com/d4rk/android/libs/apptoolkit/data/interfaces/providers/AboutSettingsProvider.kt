package com.d4rk.android.libs.apptoolkit.data.interfaces.providers

/**
 * Interface for providing settings related to the "About" section of an application.
 * This interface defines properties that describe the application itself,
 * such as its name, version, and copyright information. Implement this interface
 * to provide the necessary data for displaying an "About" screen or similar
 * informational views within the application.
 */
interface AboutSettingsProvider {
    val appName : String
    val packageName : String
    val appVersion : String
    val appVersionCode : Int
    val copyrightText : String
    val deviceInfo : String
}