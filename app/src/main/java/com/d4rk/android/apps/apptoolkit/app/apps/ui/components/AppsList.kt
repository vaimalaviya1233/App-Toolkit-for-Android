package com.d4rk.android.apps.apptoolkit.app.apps.ui.components

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.d4rk.android.apps.apptoolkit.app.apps.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.domain.model.AppListItem
import com.d4rk.android.apps.apptoolkit.app.apps.domain.model.ui.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig
import com.d4rk.android.libs.apptoolkit.core.ui.components.ads.AdBanner
import com.d4rk.android.libs.apptoolkit.core.ui.components.animations.rememberAnimatedVisibilityStateForGrids
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.animateVisibility
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ScreenHelper
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun AppsList(uiHomeScreen : UiHomeScreen , paddingValues : PaddingValues , adsConfig : AdsConfig = koinInject(qualifier = named("banner_medium_rectangle"))) {
    val apps : List<AppInfo> = uiHomeScreen.apps
    val listState : LazyGridState = rememberLazyGridState()
    val adFrequency = 4
    val dataStore : DataStore = koinInject()
    val adsEnabled : Boolean by remember { dataStore.ads(default = true) }.collectAsState(initial = true)
    val items : List<AppListItem> = remember(key1 = apps , key2 = adsEnabled) {
        buildList {
            apps.forEachIndexed { index : Int , appInfo : AppInfo ->
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
    val context = LocalContext.current
    val isTabletOrLandscape : Boolean = ScreenHelper.isLandscapeOrTablet(context = context)
    val (visibilityStates : SnapshotStateList<Boolean>) = rememberAnimatedVisibilityStateForGrids(gridState = listState , itemCount = items.size)
    LazyVerticalGrid(
        columns = GridCells.Fixed(count = if(isTabletOrLandscape) 6 else 2) ,
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