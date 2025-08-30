package com.d4rk.android.apps.apptoolkit.app.apps.favorites.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d4rk.android.apps.apptoolkit.R
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.actions.FavoriteAppsEvent
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.ui.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.app.apps.list.ui.components.AppsList
import com.d4rk.android.apps.apptoolkit.app.apps.list.ui.components.rememberAdsConfig
import com.d4rk.android.apps.apptoolkit.app.apps.list.ui.components.rememberAdsEnabled
import com.d4rk.android.apps.apptoolkit.app.apps.list.ui.components.screens.loading.HomeLoadingScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.NoDataScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.ScreenStateHandler
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.AppInfoHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ScreenHelper
import org.koin.compose.getKoin
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FavoriteAppsRoute(paddingValues: PaddingValues) {
    val viewModel: FavoriteAppsViewModel = koinViewModel()
    val screenState: UiStateScreen<UiHomeScreen> by viewModel.uiState.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isTabletOrLandscape = remember(context) { ScreenHelper.isLandscapeOrTablet(context) }
    val koin = getKoin()
    val adsConfig = rememberAdsConfig(koin, isTabletOrLandscape)
    val adsEnabled = rememberAdsEnabled(koin)
    val onFavoriteToggle: (String) -> Unit = remember(viewModel) { { pkg -> viewModel.toggleFavorite(pkg) } }
    val onRetry: () -> Unit = remember(viewModel) { { viewModel.onEvent(FavoriteAppsEvent.LoadFavorites) } }
    val appInfoHelper = remember { AppInfoHelper() }
    val onAppClick: (AppInfo) -> Unit = remember(context) {
        {
            if (it.packageName.isNotEmpty()) {
                if (appInfoHelper.isAppInstalled(context, it.packageName)) {
                    if (!appInfoHelper.openApp(context, it.packageName)) {
                        IntentsHelper.openPlayStoreForApp(context, it.packageName)
                    }
                } else {
                    IntentsHelper.openPlayStoreForApp(context, it.packageName)
                }
            }
        }
    }
    val onShareClick: (AppInfo) -> Unit = remember(context) {
        {
            IntentsHelper.shareApp(
                context = context,
                shareMessageFormat = com.d4rk.android.libs.apptoolkit.R.string.summary_share_message,
                packageName = it.packageName
            )
        }
    }

    FavoriteAppsScreen(
        screenState = screenState,
        favorites = favorites,
        paddingValues = paddingValues,
        adsConfig = adsConfig,
        adsEnabled = adsEnabled,
        onFavoriteToggle = onFavoriteToggle,
        onAppClick = onAppClick,
        onShareClick = onShareClick,
        onRetry = onRetry
    )
}

@Composable
fun FavoriteAppsScreen(
    screenState: UiStateScreen<UiHomeScreen>,
    favorites: Set<String>,
    paddingValues: PaddingValues,
    adsConfig: AdsConfig,
    adsEnabled: Boolean,
    onFavoriteToggle: (String) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onShareClick: (AppInfo) -> Unit,
    onRetry: () -> Unit,
) {
    ScreenStateHandler(
        screenState = screenState,
        onLoading = { HomeLoadingScreen(paddingValues = paddingValues) },
        onEmpty = {
            NoDataScreen(
                textMessage = R.string.no_apps_added_to_favorites,
                icon = Icons.Outlined.Android
            )
        },
        onSuccess = { uiHomeScreen ->
            AppsList(
                uiHomeScreen = uiHomeScreen,
                favorites = favorites,
                paddingValues = paddingValues,
                adsConfig = adsConfig,
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