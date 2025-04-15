package com.d4rk.android.apps.apptoolkit.app.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
fun AppsList(
    uiHomeScreen: UiHomeScreen,
    paddingValues: PaddingValues,
    adsConfig: AdsConfig = koinInject(qualifier = named("banner_medium_rectangle"))
) {
    val listState: LazyListState = rememberLazyListState()
    val apps = uiHomeScreen.apps

    val (visibilityStates: SnapshotStateList<Boolean>) =
            rememberAnimatedVisibilityState(listState = listState, itemCount = apps.size)

    LazyVerticalStaggeredGrid(
        modifier = Modifier.padding(horizontal = SizeConstants.LargeSize),
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(SizeConstants.LargeSize),
        verticalItemSpacing = SizeConstants.LargeSize,
    ) {
        apps.forEachIndexed { index, app ->
            // Insert banner every 4 apps
            if (index > 0 && index % 4 == 0) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    AdBanner(
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = SizeConstants.MediumSize),
                        adsConfig = adsConfig
                    )
                }
            }

            item {
                AppCard(
                    appInfo = app,
                    modifier = Modifier
                            .animateItem()
                            .animateVisibility(
                                visible = visibilityStates.getOrElse(index) { false },
                                index = index
                            )
                )
            }
        }
    }
}
