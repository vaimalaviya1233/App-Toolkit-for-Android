package com.d4rk.android.apps.apptoolkit.app.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.d4rk.android.apps.apptoolkit.app.home.domain.model.UiHomeScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.animations.rememberAnimatedVisibilityState
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.animateVisibility
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants

@Composable
fun AppsList(uiHomeScreen : UiHomeScreen , paddingValues : PaddingValues) {
    val listState : LazyListState = rememberLazyListState()

    val (visibilityStates : SnapshotStateList<Boolean>) = rememberAnimatedVisibilityState(listState = listState , itemCount = uiHomeScreen.apps.size)

    LazyVerticalStaggeredGrid(modifier = Modifier.padding(horizontal = SizeConstants.LargeSize) , columns = StaggeredGridCells.Fixed(2) , contentPadding = paddingValues , horizontalArrangement = Arrangement.spacedBy(space = SizeConstants.LargeSize) , verticalItemSpacing = SizeConstants.LargeSize) {
        itemsIndexed(uiHomeScreen.apps) { index , app ->
            AppCard(appInfo = app , modifier = Modifier
                    .animateItem()
                    .animateVisibility(visible = visibilityStates.getOrElse(index) { false } , index = index))
        }
    }
}