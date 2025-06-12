package com.d4rk.android.apps.apptoolkit.app.apps.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.d4rk.android.apps.apptoolkit.app.apps.domain.actions.HomeEvent
import com.d4rk.android.apps.apptoolkit.app.apps.domain.model.ui.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.app.apps.ui.components.AppsList
import com.d4rk.android.apps.apptoolkit.app.apps.ui.components.screens.loading.HomeLoadingScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.NoDataScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.ScreenStateHandler
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AppsListScreen(paddingValues : PaddingValues) {
    val viewModel : AppsListViewModel = koinViewModel()
    val screenState : UiStateScreen<UiHomeScreen> by viewModel.uiState.collectAsState()
    // Content does not trigger in-app review directly; handled in MainActivity

    ScreenStateHandler(screenState = screenState , onLoading = {
        HomeLoadingScreen(paddingValues = paddingValues)
    } , onEmpty = {
        NoDataScreen(showRetry = true , onRetry = {
            viewModel.onEvent(HomeEvent.FetchApps)
        })
    } , onSuccess = { uiHomeScreen ->
        AppsList(uiHomeScreen = uiHomeScreen , paddingValues = paddingValues)
    })
}