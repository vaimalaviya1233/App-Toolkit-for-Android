package com.d4rk.android.apps.apptoolkit.app.home.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.d4rk.android.apps.apptoolkit.app.home.domain.model.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.app.home.ui.components.AppsList
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.LoadingScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.NoDataScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.ScreenStateHandler
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(paddingValues : PaddingValues) {
    val viewModel : HomeViewModel = koinViewModel()
    val screenState : UiStateScreen<UiHomeScreen> by viewModel.screenState.collectAsState()

    ScreenStateHandler(screenState = screenState , onLoading = { LoadingScreen() } , onEmpty = {
        NoDataScreen(showRetry = true , onRetry = {
            viewModel.fetchDeveloperApps()
        })
    } , onSuccess = { uiHomeScreen ->
        AppsList(uiHomeScreen = uiHomeScreen , paddingValues = paddingValues)
    })
}