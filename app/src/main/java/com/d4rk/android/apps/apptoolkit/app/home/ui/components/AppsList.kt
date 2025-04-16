package com.d4rk.android.apps.apptoolkit.app.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.d4rk.android.apps.apptoolkit.app.home.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.home.domain.model.UiHomeScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig
import com.d4rk.android.libs.apptoolkit.core.ui.components.ads.AdBanner
import com.d4rk.android.libs.apptoolkit.core.ui.components.animations.rememberAnimatedVisibilityState
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.animateVisibility
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun AppsList(uiHomeScreen : UiHomeScreen , paddingValues : PaddingValues , adsConfig : AdsConfig = koinInject(qualifier = named("banner_medium_rectangle"))) {
    val apps : List<AppInfo> = uiHomeScreen.apps
    val listState : LazyListState = rememberLazyListState()
    val (visibilityStates : SnapshotStateList<Boolean>) = rememberAnimatedVisibilityState(listState = listState , itemCount = apps.size)

    LazyVerticalGrid(
        columns = GridCells.Fixed(count = 2) ,
        contentPadding = paddingValues ,
        horizontalArrangement = Arrangement.spacedBy(space = SizeConstants.LargeSize) ,
        verticalArrangement = Arrangement.spacedBy(space = SizeConstants.LargeSize) ,
        modifier = Modifier.padding(horizontal = SizeConstants.LargeSize)
    ) {
        val adFrequency = 4
        val totalItems : Int = apps.size + (apps.size / adFrequency)
        items(count = totalItems , key = { index : Int -> "item_$index" } , span = { index : Int ->
            val isAdIndex : Boolean = (index + 1) % (adFrequency + 1) == 0
            if (isAdIndex) {
                GridItemSpan(currentLineSpan = maxLineSpan)
            }
            else {
                GridItemSpan(currentLineSpan = 1)
            }
        }) { index : Int ->
            val isAdIndex : Boolean = (index + 1) % (adFrequency + 1) == 0
            val appIndex : Int = index - (index / (adFrequency + 1))

            if (isAdIndex) {
                AdBanner(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = SizeConstants.MediumSize) , adsConfig = adsConfig)
            }
            else if (appIndex in apps.indices) {
                AppCard(appInfo = apps[appIndex] , modifier = Modifier
                        .animateItem()
                        .animateVisibility(visible = visibilityStates.getOrElse(index = appIndex) { false } , index = appIndex))
            }
        }
    }
}