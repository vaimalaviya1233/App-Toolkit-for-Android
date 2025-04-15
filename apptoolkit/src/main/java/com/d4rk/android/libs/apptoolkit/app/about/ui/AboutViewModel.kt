package com.d4rk.android.libs.apptoolkit.app.about.ui

import android.content.Context
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.about.domain.model.actions.AboutEvents
import com.d4rk.android.libs.apptoolkit.app.about.domain.model.events.AboutActions
import com.d4rk.android.libs.apptoolkit.app.about.domain.model.ui.UiAboutScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.showSnackbar
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.ScreenMessageType
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.dismissSnackbar
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.AboutLibrariesHelper
import kotlinx.coroutines.flow.MutableStateFlow

open class AboutViewModel : ScreenViewModel<UiAboutScreen , AboutEvents , AboutActions>(
    initialState = UiStateScreen(data = UiAboutScreen())
) {

    override fun onEvent(event: AboutEvents) {
        when (event) {
            is AboutEvents.CopyDeviceInfo -> copyDeviceInfo()
            is AboutEvents.DismissSnackbar -> screenState.dismissSnackbar()
            is AboutEvents.LoadHtml -> loadHtmlData(
                context = event.context,
                packageName = event.packageName,
                versionName = event.versionName
            )
        }
    }

    protected open fun loadHtmlData(context: Context, packageName: String, versionName: String) {
        launch {
            val (changelog, eula) = AboutLibrariesHelper.loadHtmlData(
                packageName = packageName,
                currentVersionName = versionName,
                context = context
            )
            updateUi {
                copy(changelogHtml = changelog, eulaHtml = eula)
            }
        }
    }

    private fun copyDeviceInfo() {
        updateUi { copy(showDeviceInfoCopiedSnackbar = true) }
        screenState.showSnackbar(
            snackbar = UiSnackbar(
                message = UiTextHelper.StringResource(resourceId = R.string.snack_device_info_copied),
                isError = false,
                timeStamp = System.currentTimeMillis(),
                type = ScreenMessageType.SNACKBAR
            )
        )
    }

    protected inline fun updateUi(crossinline transform: UiAboutScreen.() -> UiAboutScreen) {
        launch {
            screenState.updateData(newState = screenState.value.screenState) { current -> transform(current) }
        }
    }
}
