package com.d4rk.android.apps.apptoolkit.app.apps.list.ui.components

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppListItem
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.ui.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig
import com.d4rk.android.libs.apptoolkit.core.ui.components.ads.AdBanner
import com.d4rk.android.libs.apptoolkit.core.ui.components.animations.rememberAnimatedVisibilityStateForGrids
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.animateVisibility
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ScreenHelper
import org.koin.compose.getKoin
import org.koin.core.qualifier.named

@Composable
fun AppsList(
    uiHomeScreen: UiHomeScreen,
    favorites: Set<String>,
    paddingValues: PaddingValues,
    onFavoriteToggle: (String) -> Unit
) {
    val apps: List<AppInfo> = uiHomeScreen.apps
    val context = LocalContext.current
    val isTabletOrLandscape: Boolean = remember(context) {
        ScreenHelper.isLandscapeOrTablet(context = context)
    }
    val columnCount: Int by remember(isTabletOrLandscape) {
        derivedStateOf { if (isTabletOrLandscape) 4 else 2 }
    }

    val bannerType: String by remember(isTabletOrLandscape) {
        derivedStateOf {
            if (isTabletOrLandscape) "full_banner" else "banner_medium_rectangle"
        }
    }
    val koin = getKoin()
    val adsConfig: AdsConfig = remember(bannerType) {
        koin.get(qualifier = named(bannerType))
    }
    val listState: LazyGridState = rememberLazyGridState()
    val adFrequency = 4
    val dataStore: DataStore = remember { koin.get() }
    val adsEnabled: Boolean by remember { dataStore.ads(default = true) }
        .collectAsStateWithLifecycle(initialValue = true)
    val items: List<AppListItem> by remember(apps, adsEnabled) {
        derivedStateOf {
            buildList {
                apps.forEachIndexed { index: Int, appInfo: AppInfo ->
                    add(element = AppListItem.App(appInfo = appInfo))
                    if (adsEnabled && (index + 1) % adFrequency == 0) {
                        add(element = AppListItem.Ad)
                    }
                }
                if (adsEnabled && apps.isNotEmpty() && apps.size % adFrequency != 0) {
                    add(element = AppListItem.Ad)
                }
            }
        }
    }

    val (visibilityStates: SnapshotStateList<Boolean>) = rememberAnimatedVisibilityStateForGrids(
        gridState = listState, itemCount = items.size
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(count = columnCount),
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(horizontal = SizeConstants.LargeSize)
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
            }
        ) { index: Int, item: AppListItem ->
            when (item) {
                is AppListItem.App -> {
                    val appInfo = item.appInfo
                    AppCard(
                        appInfo = appInfo,
                        isFavorite = favorites.contains(appInfo.packageName),
                        onFavoriteToggle = { onFavoriteToggle(appInfo.packageName) },
                        modifier = Modifier
                            .animateVisibility(
                                visible = visibilityStates.getOrElse(index = index) { false },
                                index = index
                            )
                            .padding(all = SizeConstants.SmallSize)
                    )
                }

                AppListItem.Ad -> {
                    AdBanner(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = SizeConstants.MediumSize),
                        adsConfig = adsConfig
                    )
                }
            }
        }
    }
}
