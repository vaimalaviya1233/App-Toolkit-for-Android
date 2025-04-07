package com.d4rk.android.libs.apptoolkit.app.ads.ui.components

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.d4rk.android.libs.apptoolkit.app.ads.domain.model.AdsSettingsData
import com.d4rk.android.libs.apptoolkit.app.ads.ui.AdsSettingsViewModel
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.dismissSnackbar

@Composable
fun AdsSnackbarHandler(uiState : UiStateScreen<AdsSettingsData> , viewModel : AdsSettingsViewModel , snackbarHostState : SnackbarHostState) {
    val context : Context = LocalContext.current

    LaunchedEffect(key1 = uiState.snackbar) {
        uiState.snackbar?.let {
            snackbarHostState.showSnackbar(message = it.message.asString(context))
            viewModel.screenState.dismissSnackbar()
        }
    }
}