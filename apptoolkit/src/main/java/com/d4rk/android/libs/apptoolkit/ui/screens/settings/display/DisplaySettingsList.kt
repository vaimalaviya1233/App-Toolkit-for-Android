package com.d4rk.android.libs.apptoolkit.ui.screens.settings.display


import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.d4rk.android.libs.apptoolkit.data.interfaces.providers.DisplaySettingsProvider
import com.d4rk.android.libs.apptoolkit.ui.components.preferences.PreferenceCategoryItem
import com.d4rk.android.libs.apptoolkit.ui.components.preferences.PreferenceItem
import com.d4rk.android.libs.apptoolkit.ui.components.preferences.SwitchPreferenceItem
import com.d4rk.android.libs.apptoolkit.ui.components.preferences.SwitchPreferenceItemWithDivider
import kotlinx.coroutines.launch

@Composable
fun DisplaySettingsList(
    paddingValues: PaddingValues = PaddingValues(),
    provider: DisplaySettingsProvider
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val dataStore: CommonDataStore = CommonDataStore.getInstance(context)
    var showLanguageDialog: Boolean by remember { mutableStateOf(value = false) }
    var showStartupDialog: Boolean by remember { mutableStateOf(value = false) }

    val themeMode: String = dataStore.themeMode.collectAsState(initial = "follow_system").value
    val darkModeString: String = stringResource(id = R.string.dark_mode)
    val lightModeString: String = stringResource(id = R.string.light_mode)
    val isSystemDarkTheme: Boolean = isSystemInDarkTheme()
    val isDarkTheme: Boolean = when (themeMode) {
        darkModeString -> true
        lightModeString -> false
        else -> isSystemDarkTheme
    }

    val themeSummary: String = when (themeMode) {
        darkModeString, lightModeString -> stringResource(id = R.string.will_never_turn_on_automatically)
        else -> stringResource(id = R.string.will_turn_on_automatically_by_system)
    }

    val isDynamicColors: State<Boolean> = dataStore.dynamicColors.collectAsState(initial = true)
    val bouncyButtons: Boolean by dataStore.bouncyButtons.collectAsState(initial = true)

    LazyColumn(
        contentPadding = paddingValues,
        modifier = Modifier
                .fillMaxHeight()
    ) {
        item {
            PreferenceCategoryItem(title = stringResource(id = R.string.appearance))
            SwitchPreferenceItemWithDivider(
                title = stringResource(id = R.string.dark_theme),
                summary = themeSummary,
                checked = isDarkTheme,
                onCheckedChange = { isChecked ->
                    coroutineScope.launch {
                        if (isChecked) {
                            dataStore.saveThemeMode(mode = darkModeString)
                            dataStore.themeModeState.value = darkModeString
                        } else {
                            dataStore.saveThemeMode(mode = lightModeString)
                            dataStore.themeModeState.value = lightModeString
                        }
                    }
                },
                onSwitchClick = { isChecked ->
                    coroutineScope.launch {
                        if (isChecked) {
                            dataStore.saveThemeMode(mode = darkModeString)
                            dataStore.themeModeState.value = darkModeString
                        } else {
                            dataStore.saveThemeMode(mode = lightModeString)
                            dataStore.themeModeState.value = lightModeString
                        }
                    }
                },
                onClick = {
                    provider.openThemeSettings()
                }
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SwitchPreferenceItem(
                    title = stringResource(id = R.string.dynamic_colors) ,
                    summary = stringResource(id = R.string.summary_preference_settings_dynamic_colors) ,
                    checked = isDynamicColors.value ,
                ) { isChecked ->
                    coroutineScope.launch {
                        dataStore.saveDynamicColors(isChecked = isChecked)
                    }
                }
            }
        }
        item {
            PreferenceCategoryItem(title = stringResource(id = R.string.app_behavior))
            SwitchPreferenceItem(
                title = stringResource(id = R.string.bounce_buttons) ,
                summary = stringResource(id = R.string.summary_preference_settings_bounce_buttons) ,
                checked = bouncyButtons ,
            ) { isChecked ->
                coroutineScope.launch {
                    dataStore.saveBouncyButtons(isChecked = isChecked)
                }
            }
        }

        if (provider.supportsStartupPage) {
            item {
                PreferenceCategoryItem(title = stringResource(id = R.string.navigation))
                PreferenceItem(title = stringResource(id = R.string.startup_page) , summary = stringResource(id = R.string.summary_preference_settings_startup_page) , onClick = { showStartupDialog = true })
                if (showStartupDialog) {
                    provider.StartupPageDialog(onDismiss = {
                        showStartupDialog = false
                    }) { }
                }
            }
        }

        item {
            PreferenceCategoryItem(title = stringResource(id = R.string.language))
            PreferenceItem(title = stringResource(id = R.string.language) , summary = stringResource(id = R.string.summary_preference_settings_language) , onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val localeIntent : Intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS).setData(
                        Uri.fromParts(
                            "package" , context.packageName , null
                        )
                    )
                    val detailsIntent : Intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(
                        Uri.fromParts(
                            "package" , context.packageName , null
                        )
                    )
                    when {
                        context.packageManager.resolveActivity(
                            localeIntent , 0
                        ) != null -> context.startActivity(localeIntent)

                        context.packageManager.resolveActivity(
                            detailsIntent , 0
                        ) != null -> context.startActivity(detailsIntent)

                        else -> {
                            showLanguageDialog = true
                        }
                    }
                }
                else {
                    showLanguageDialog = true
                }
            })

            if (showLanguageDialog) {
                provider.LanguageSelectionDialog(onDismiss = { showLanguageDialog = false }) { }
            }
        }
    }
}