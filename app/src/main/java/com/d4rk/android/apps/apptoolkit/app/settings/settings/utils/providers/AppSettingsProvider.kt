package com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsCategory
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsConfig
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsPreference
import com.d4rk.android.libs.apptoolkit.app.settings.settings.utils.interfaces.SettingsProvider
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper

class AppSettingsProvider : SettingsProvider {
    override fun provideSettingsConfig(context: Context): SettingsConfig {
        return SettingsConfig(
            title = context.getString(R.string.settings),
            categories = listOf(
                SettingsCategory(
                    title = "context.getString(R.string.general)",
                    preferences = listOf(
                        SettingsPreference(
                            key = "notifications",
                            icon = Icons.Outlined.Notifications,
                            title = context.getString(R.string.notifications),
                            summary = context.getString(R.string.summary_preference_settings_notifications),
                            action = { IntentsHelper.openAppNotificationSettings(context) }
                        ),
                        SettingsPreference(
                            key = "display",
                            icon = Icons.Outlined.Palette,
                            title = context.getString(R.string.display),
                            summary = context.getString(R.string.summary_preference_settings_display),
                            action = {
                            /*    GeneralSettingsActivity.start(
                                    context, title = context.getString(R.string.display), content = SettingsContent.DISPLAY
                                )*/
                            }
                        )
                    )
                ),
                SettingsCategory(
                    title = "context.getString(R.string.general)",
                    preferences = listOf(
                        SettingsPreference(
                            key = "notifications",
                            icon = Icons.Outlined.Notifications,
                            title = context.getString(R.string.notifications),
                            summary = context.getString(R.string.summary_preference_settings_notifications),
                            action = { IntentsHelper.openAppNotificationSettings(context) }
                        ),
                        SettingsPreference(
                            key = "display",
                            icon = Icons.Outlined.Palette,
                            title = context.getString(R.string.display),
                            summary = context.getString(R.string.summary_preference_settings_display),
                            action = {
                                /*    GeneralSettingsActivity.start(
                                        context, title = context.getString(R.string.display), content = SettingsContent.DISPLAY
                                    )*/
                            }
                        ),
                        SettingsPreference(
                            key = "display",
                            icon = Icons.Outlined.Palette,
                            title = context.getString(R.string.display),
                            summary = context.getString(R.string.summary_preference_settings_display),
                            action = {
                                /*    GeneralSettingsActivity.start(
                                        context, title = context.getString(R.string.display), content = SettingsContent.DISPLAY
                                    )*/
                            }
                        )
                    )
                )
            )
        )
    }
}