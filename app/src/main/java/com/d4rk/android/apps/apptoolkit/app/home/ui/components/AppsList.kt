package com.d4rk.android.apps.apptoolkit.app.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.d4rk.android.apps.apptoolkit.app.home.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.home.domain.model.AppListItem
import com.d4rk.android.apps.apptoolkit.app.home.domain.model.UiHomeScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig
import com.d4rk.android.libs.apptoolkit.core.ui.components.ads.AdBanner
import com.d4rk.android.libs.apptoolkit.core.ui.components.animations.rememberAnimatedVisibilityStateForGrids
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.animateVisibility
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun AppsList(uiHomeScreen : UiHomeScreen , paddingValues : PaddingValues , adsConfig : AdsConfig = koinInject(qualifier = named("banner_medium_rectangle"))) {
    val apps : List<AppInfo> = uiHomeScreen.apps
    val listState : LazyGridState = rememberLazyGridState()
    val adFrequency = 4
    val items : List<AppListItem> = remember(key1 = apps) {
        buildList {
            apps.forEachIndexed { index : Int , appInfo : AppInfo ->
                add(element = AppListItem.App(appInfo = appInfo))
                if ((index + 1) % adFrequency == 0) {
                    add(element = AppListItem.Ad)
                }
            }
        }
    }

    val (visibilityStates : SnapshotStateList<Boolean>) = rememberAnimatedVisibilityStateForGrids(gridState = listState , itemCount = items.size)

    LazyVerticalGrid(
        columns = GridCells.Fixed(count = 2) ,
        contentPadding = paddingValues ,
        state = listState ,
        horizontalArrangement = Arrangement.spacedBy(SizeConstants.LargeSize) ,
        verticalArrangement = Arrangement.spacedBy(SizeConstants.LargeSize) ,
        modifier = Modifier.padding(horizontal = SizeConstants.LargeSize)
    ) {
        itemsIndexed(items , span = { _ : Int , item : AppListItem ->
            when (item) {
                is AppListItem.Ad -> GridItemSpan(currentLineSpan = maxLineSpan)
                is AppListItem.App -> GridItemSpan(currentLineSpan = 1)
            }
        }) { index : Int , item : AppListItem ->
            when (item) {
                is AppListItem.Ad -> {
                    AdBanner(
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = SizeConstants.MediumSize) , adsConfig = adsConfig
                    )
                }

                is AppListItem.App -> {
                    AppCard(appInfo = item.appInfo , modifier = Modifier
                            .animateItem()
                            .animateVisibility(visible = visibilityStates.getOrElse(index = index) { false } , index = index))
                }
            }
        }
    }
}