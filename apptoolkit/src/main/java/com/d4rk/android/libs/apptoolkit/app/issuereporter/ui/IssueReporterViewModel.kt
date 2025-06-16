package com.d4rk.android.libs.apptoolkit.app.issuereporter.ui

import android.content.Context
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.issuereporter.data.IssueReporterRepository
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.actions.IssueReporterAction
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.actions.IssueReporterEvent
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.UiIssueReporterScreen
import com.d4rk.android.libs.apptoolkit.app.issuereporter.model.DeviceInfo
import com.d4rk.android.libs.apptoolkit.app.issuereporter.model.Report
import com.d4rk.android.libs.apptoolkit.app.issuereporter.model.github.ExtraInfo
import com.d4rk.android.libs.apptoolkit.app.issuereporter.model.github.GithubTarget
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.dismissSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.showSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateState
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.ScreenMessageType
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import io.ktor.client.HttpClient

class IssueReporterViewModel(
    private val dispatcherProvider: DispatcherProvider,
    httpClient: HttpClient,
    private val githubTarget: GithubTarget,
) : ScreenViewModel<UiIssueReporterScreen, IssueReporterEvent, IssueReporterAction>(
    initialState = UiStateScreen(data = UiIssueReporterScreen())
) {

    private val repository = IssueReporterRepository(httpClient)

    override fun onEvent(event: IssueReporterEvent) {
        when (event) {
            is IssueReporterEvent.UpdateTitle -> updateTitle(event.value)
            is IssueReporterEvent.UpdateDescription -> updateDescription(event.value)
            is IssueReporterEvent.UpdateEmail -> updateEmail(event.value)
            is IssueReporterEvent.Send -> sendReport(event.context)
            IssueReporterEvent.DismissSnackbar -> screenState.dismissSnackbar()
        }
    }

    private fun updateTitle(value: String) {
        screenState.updateData(newState = screenState.value.screenState) { current ->
            current.copy(title = value)
        }
    }

    private fun updateDescription(value: String) {
        screenState.updateData(newState = screenState.value.screenState) { current ->
            current.copy(description = value)
        }
    }

    private fun updateEmail(value: String) {
        screenState.updateData(newState = screenState.value.screenState) { current ->
            current.copy(email = value)
        }
    }

    private fun sendReport(context: Context) {
        val data = screenState.value.data ?: return
        if (data.title.isBlank() || data.description.isBlank()) {
            screenState.showSnackbar<UiIssueReporterScreen>(
                snackbar = UiSnackbar(
                    message = UiTextHelper.StringResource(R.string.error_invalid_report),
                    timeStamp = System.currentTimeMillis(),
                    isError = true,
                    type = ScreenMessageType.SNACKBAR
                )
            )
            return
        }
        launch(dispatcherProvider.io) {
            screenState.updateState(ScreenState.IsLoading())
            val deviceInfo = DeviceInfo(context)
            val extraInfo = ExtraInfo()
            val report = Report(
                title = data.title,
                description = data.description,
                deviceInfo = deviceInfo,
                extraInfo = extraInfo,
                email = data.email.ifBlank { null }
            )
            val success = runCatching { repository.sendReport(report, githubTarget) }.getOrDefault(false)
            if (success) {
                screenState.showSnackbar<UiIssueReporterScreen>(
                    snackbar = UiSnackbar(
                        message = UiTextHelper.StringResource(R.string.snack_report_success),
                        isError = false,
                        timeStamp = System.currentTimeMillis(),
                        type = ScreenMessageType.SNACKBAR
                    )
                )
                screenState.updateState(ScreenState.Success())
            } else {
                screenState.showSnackbar<UiIssueReporterScreen>(
                    snackbar = UiSnackbar(
                        message = UiTextHelper.StringResource(R.string.snack_report_failed),
                        isError = true,
                        timeStamp = System.currentTimeMillis(),
                        type = ScreenMessageType.SNACKBAR
                    )
                )
                screenState.updateState(ScreenState.Error())
            }
        }
    }
}
