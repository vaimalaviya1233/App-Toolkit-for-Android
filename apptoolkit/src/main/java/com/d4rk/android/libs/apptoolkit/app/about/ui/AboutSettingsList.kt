package com.d4rk.android.libs.apptoolkit.app.about.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.about.domain.actions.AboutEvent
import com.d4rk.android.libs.apptoolkit.app.about.domain.model.ui.UiAboutScreen
import com.d4rk.android.libs.apptoolkit.app.licenses.LicensesActivity
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.LoadingScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.NoDataScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.ScreenStateHandler
import com.d4rk.android.libs.apptoolkit.core.ui.components.preferences.PreferenceCategoryItem
import com.d4rk.android.libs.apptoolkit.core.ui.components.preferences.SettingsPreferenceItem
import com.d4rk.android.libs.apptoolkit.core.ui.components.snackbar.DefaultSnackbarHandler
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.ExtraTinyVerticalSpacer
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.SmallVerticalSpacer
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ClipboardHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AboutSettingsList(paddingValues: PaddingValues = PaddingValues(), snackbarHostState: SnackbarHostState) {
    val context: Context = LocalContext.current
    val viewModel: AboutViewModel = koinViewModel()
    val screenState: UiStateScreen<UiAboutScreen> by viewModel.uiState.collectAsStateWithLifecycle()
    val deviceInfo: String = stringResource(id = R.string.device_info)

    ScreenStateHandler(screenState = screenState, onLoading = { LoadingScreen() }, onEmpty = { NoDataScreen() }, onSuccess = { data: UiAboutScreen ->
        LazyColumn(modifier = Modifier.fillMaxHeight() , contentPadding = paddingValues) {
            item {
                PreferenceCategoryItem(title = stringResource(id = R.string.app_info))
                SmallVerticalSpacer()
                Column(
                    modifier = Modifier
                            .padding(horizontal = SizeConstants.LargeSize)
                            .clip(shape = RoundedCornerShape(size = SizeConstants.LargeSize))
                ) {
                    SettingsPreferenceItem(title = stringResource(id = R.string.app_full_name), summary = stringResource(id = R.string.copyright))
                    ExtraTinyVerticalSpacer()
                    SettingsPreferenceItem(
                        title = stringResource(id = R.string.app_build_version),
                        summary = "${data.appVersion} (${data.appVersionCode})",
                    )
                    ExtraTinyVerticalSpacer()
                    SettingsPreferenceItem(title = stringResource(id = R.string.oss_license_title), summary = stringResource(id = R.string.summary_preference_settings_oss)) {
                        IntentsHelper.openActivity(context = context, activityClass = LicensesActivity::class.java)
                    }
                }
            }

            item {
                PreferenceCategoryItem(title = deviceInfo)
                SmallVerticalSpacer()
                Column(
                    modifier = Modifier
                            .padding(horizontal = SizeConstants.LargeSize)
                            .clip(shape = RoundedCornerShape(size = SizeConstants.LargeSize))
                ) {
                    SettingsPreferenceItem(title = deviceInfo, summary = data.deviceInfo) {
                        ClipboardHelper.copyTextToClipboard(
                            context = context,
                            label = deviceInfo,
                            text = data.deviceInfo,
                            onShowSnackbar = { viewModel.onEvent(event = AboutEvent.CopyDeviceInfo) },
                        )
                    }
                }
            }
        }
    })

    DefaultSnackbarHandler(screenState = screenState, snackbarHostState = snackbarHostState, getDismissEvent = { AboutEvent.DismissSnackbar }, onEvent = { viewModel.onEvent(it) })
}