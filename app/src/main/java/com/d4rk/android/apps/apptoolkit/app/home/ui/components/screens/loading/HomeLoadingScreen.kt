package com.d4rk.android.apps.apptoolkit.app.home.ui.components.screens.loading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.WindowItemFit

@Composable
fun HomeLoadingScreen(paddingValues : PaddingValues , itemAspectRatio : Float = 1f) {
    val placeholderCount = WindowItemFit.count(itemHeight = 180.dp , itemSpacing = SizeConstants.LargeSize , paddingValues = paddingValues)
    val actualItemCount = if (placeholderCount % 2 == 0) placeholderCount else placeholderCount + 1

    LazyVerticalGrid(columns = GridCells.Fixed(count = 2) , contentPadding = paddingValues , horizontalArrangement = Arrangement.spacedBy(SizeConstants.LargeSize) , verticalArrangement = Arrangement.spacedBy(SizeConstants.LargeSize) , modifier = Modifier.padding(horizontal = SizeConstants.LargeSize)) {
        items(actualItemCount) {
            ShimmerPlaceholderAppCard(aspectRatio = itemAspectRatio)
        }
    }
}