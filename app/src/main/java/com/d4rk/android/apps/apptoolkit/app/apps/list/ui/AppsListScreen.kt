package com.d4rk.android.apps.apptoolkit.app.apps.list.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d4rk.android.apps.apptoolkit.app.apps.common.AppDetailsBottomSheet
import com.d4rk.android.apps.apptoolkit.app.apps.common.buildOnAppClick
import com.d4rk.android.apps.apptoolkit.app.apps.common.buildOnShareClick
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.actions.HomeEvent
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.ui.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.app.apps.list.ui.components.AppsList
import com.d4rk.android.apps.apptoolkit.app.apps.list.ui.components.screens.loading.HomeLoadingScreen
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.ads.rememberAdsEnabled
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.NoDataScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.ScreenStateHandler
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsListRoute(paddingValues: PaddingValues) {
    val viewModel: AppsListViewModel = koinViewModel()
    val screenState: UiStateScreen<UiHomeScreen> by viewModel.uiState.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val adsEnabled = rememberAdsEnabled()
    val onFavoriteToggle: (String) -> Unit = remember(viewModel) { { pkg -> viewModel.toggleFavorite(pkg) } }
    val onRetry: () -> Unit = remember(viewModel) { { viewModel.onEvent(HomeEvent.FetchApps) } }
    val dispatchers: DispatcherProvider = koinInject()
    val onOpenInPlayStore: (AppInfo) -> Unit = buildOnAppClick(dispatchers, context)
    val onShareClick: (AppInfo) -> Unit = buildOnShareClick(context)
    var selectedApp: AppInfo? by remember { mutableStateOf(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    selectedApp?.let { app ->
        ModalBottomSheet(
            modifier = Modifier.fillMaxHeight(),
            sheetState = sheetState,
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    selectedApp = null
                }
            }
        ) {
            AppDetailsBottomSheet(
                appInfo = app,
                onShareClick = { onShareClick(app) },
                onOpenInPlayStoreClick = {
                    coroutineScope.launch {
                        selectedApp = null
                        onOpenInPlayStore(app)
                    }
                },
                onFavoriteClick = {
                    coroutineScope.launch {
                        selectedApp = null
                        onFavoriteToggle(app.packageName)
                    }
                }
            )
        }
    }

    AppsListScreen(
        screenState = screenState,
        favorites = favorites,
        paddingValues = paddingValues,
        adsEnabled = adsEnabled,
        onFavoriteToggle = onFavoriteToggle,
        onAppClick = { app -> selectedApp = app },
        onShareClick = onShareClick,
        onRetry = onRetry
    )
}

@Composable
fun AppsListScreen(
    screenState: UiStateScreen<UiHomeScreen>,
    favorites: Set<String>,
    paddingValues: PaddingValues,
    adsEnabled: Boolean,
    onFavoriteToggle: (String) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onShareClick: (AppInfo) -> Unit,
    onRetry: () -> Unit,
) {
    ScreenStateHandler(
        screenState = screenState,
        onLoading = { HomeLoadingScreen(paddingValues = paddingValues) },
        onEmpty = { NoDataScreen() },
        onSuccess = { uiHomeScreen ->
            AppsList(
                uiHomeScreen = uiHomeScreen,
                favorites = favorites,
                paddingValues = paddingValues,
                adsEnabled = adsEnabled,
                onFavoriteToggle = onFavoriteToggle,
                onAppClick = onAppClick,
                onShareClick = onShareClick
            )
        },
        onError = {
            NoDataScreen(
                showRetry = true,
                onRetry = onRetry,
                isError = true
            )
        }
    )
}