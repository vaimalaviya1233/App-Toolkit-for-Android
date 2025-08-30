package com.d4rk.android.apps.apptoolkit.app.apps.list.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.actions.HomeEvent
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.ui.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.app.apps.list.ui.components.AppsList
import com.d4rk.android.apps.apptoolkit.app.apps.list.ui.components.rememberAdsConfig
import com.d4rk.android.apps.apptoolkit.app.apps.list.ui.components.rememberAdsEnabled
import com.d4rk.android.apps.apptoolkit.app.apps.list.ui.components.screens.loading.HomeLoadingScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.NoDataScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.ScreenStateHandler
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ScreenHelper
import org.koin.compose.getKoin
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppsListRoute(paddingValues: PaddingValues) {
    val viewModel: AppsListViewModel = koinViewModel()
    val screenState: UiStateScreen<UiHomeScreen> by viewModel.uiState.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isTabletOrLandscape = remember(context) { ScreenHelper.isLandscapeOrTablet(context) }
    val koin = getKoin()
    val adsConfig = rememberAdsConfig(koin, isTabletOrLandscape)
    val adsEnabled = rememberAdsEnabled(koin)
    val onFavoriteToggle: (String) -> Unit = remember(viewModel) { { pkg -> viewModel.toggleFavorite(pkg) } }
    val onRetry: () -> Unit = remember(viewModel) { { viewModel.onEvent(HomeEvent.FetchApps) } }

    AppsListScreen(
        screenState = screenState,
        favorites = favorites,
        paddingValues = paddingValues,
        adsConfig = adsConfig,
        adsEnabled = adsEnabled,
        onFavoriteToggle = onFavoriteToggle,
        onRetry = onRetry
    )
}

@Composable
fun AppsListScreen(
    screenState: UiStateScreen<UiHomeScreen>,
    favorites: Set<String>,
    paddingValues: PaddingValues,
    adsConfig: AdsConfig,
    adsEnabled: Boolean,
    onFavoriteToggle: (String) -> Unit,
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
                adsConfig = adsConfig,
                adsEnabled = adsEnabled,
                onFavoriteToggle = onFavoriteToggle
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