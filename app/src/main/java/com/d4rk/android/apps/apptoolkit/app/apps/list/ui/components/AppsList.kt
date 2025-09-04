package com.d4rk.android.apps.apptoolkit.app.apps.list.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppListItem
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.ui.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.core.ads.ui.NativeAdBanner
import com.d4rk.android.libs.apptoolkit.core.ui.components.animations.rememberAnimatedVisibilityStateForGrids
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.animateVisibility
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ScreenHelper

@Composable
fun AppsList(
    uiHomeScreen: UiHomeScreen,
    favorites: Set<String>,
    paddingValues: PaddingValues,
    adsEnabled: Boolean,
    onFavoriteToggle: (String) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onShareClick: (AppInfo) -> Unit,
) {
    val apps: List<AppInfo> = uiHomeScreen.apps
    val context = LocalContext.current
    val isTabletOrLandscape = remember(context) {
        ScreenHelper.isLandscapeOrTablet(context = context)
    }
    val columnCount by remember(isTabletOrLandscape) {
        derivedStateOf { if (isTabletOrLandscape) 4 else 2 }
    }
    val listState = rememberLazyGridState()
    val adFrequency = 4
    val items by remember(apps, adsEnabled) {
        derivedStateOf { buildAppListItems(apps, adsEnabled, adFrequency) }
    }

    AppsGrid(
        items = items,
        favorites = favorites,
        paddingValues = paddingValues,
        columnCount = columnCount,
        listState = listState,
        onFavoriteToggle = onFavoriteToggle,
        onAppClick = onAppClick,
        onShareClick = onShareClick
    )
}

@Composable
private fun AppsGrid(
    items: List<AppListItem>,
    favorites: Set<String>,
    paddingValues: PaddingValues,
    columnCount: Int,
    listState: LazyGridState,
    onFavoriteToggle: (String) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onShareClick: (AppInfo) -> Unit
) {
    val (visibilityStates: SnapshotStateList<Boolean>) = rememberAnimatedVisibilityStateForGrids(
        gridState = listState,
        itemCount = items.size
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(count = columnCount),
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(SizeConstants.LargeSize),
        verticalArrangement = Arrangement.spacedBy(space = SizeConstants.LargeSize),
        horizontalArrangement = Arrangement.spacedBy(space = SizeConstants.LargeSize)
    ) {
        itemsIndexed(
            items = items,
            key = { index, item ->
                when (item) {
                    is AppListItem.App -> item.appInfo.packageName
                    AppListItem.Ad -> "ad_$index"
                }
            },
            span = { _, item ->
                if (item is AppListItem.Ad) GridItemSpan(columnCount) else GridItemSpan(1)
            },
            contentType = { _, item ->
                when (item) {
                    is AppListItem.App -> "app"
                    AppListItem.Ad -> "ad"
                }
            }
        ) { index: Int, item: AppListItem ->
            when (item) {
                is AppListItem.App -> {
                    val packageName = item.appInfo.packageName
                    val isFavorite by remember(favorites, packageName) {
                        derivedStateOf { favorites.contains(packageName) }
                    }
                    AppCardItem(
                        item = item,
                        isFavorite = isFavorite,
                        visibilityStates = visibilityStates,
                        index = index,
                        onFavoriteToggle = onFavoriteToggle,
                        onAppClick = onAppClick,
                        onShareClick = onShareClick
                    )
                }

                AppListItem.Ad -> AdListItem()
            }
        }
    }
}

@Composable
private fun AppCardItem(
    item: AppListItem.App,
    isFavorite: Boolean,
    visibilityStates: SnapshotStateList<Boolean>,
    index: Int,
    onFavoriteToggle: (String) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onShareClick: (AppInfo) -> Unit
) {
    val appInfo = item.appInfo
    AppCard(
        appInfo = appInfo,
        isFavorite = isFavorite,
        onFavoriteToggle = { onFavoriteToggle(appInfo.packageName) },
        onAppClick = onAppClick,
        onShareClick = onShareClick,
        modifier = Modifier.animateVisibility(
            visible = visibilityStates.getOrElse(index = index) { false },
            index = index
        )
    )
}

@Composable
private fun AdListItem() {
    NativeAdBanner(
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun buildAppListItems(
    apps: List<AppInfo>,
    adsEnabled: Boolean,
    adFrequency: Int
): List<AppListItem> {
    return buildList {
        apps.forEachIndexed { index, appInfo ->
            add(AppListItem.App(appInfo))
            if (adsEnabled && (index + 1) % adFrequency == 0) {
                add(AppListItem.Ad)
            }
        }
        if (adsEnabled && apps.isNotEmpty() && apps.size % adFrequency != 0) {
            add(AppListItem.Ad)
        }
    }
}

