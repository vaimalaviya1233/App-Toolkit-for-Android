package com.d4rk.android.apps.apptoolkit.app.apps.list.ui.components.screens.loading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ScreenHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.WindowItemFit

@Composable
fun HomeLoadingScreen(paddingValues: PaddingValues, itemAspectRatio: Float = 1f) {

    val context = LocalContext.current
    val isTabletOrLandscape: Boolean = remember(context) {
        ScreenHelper.isLandscapeOrTablet(context = context)
    }
    val numberOfColumns: Int by remember(isTabletOrLandscape) {
        derivedStateOf { if (isTabletOrLandscape) 4 else 2 }
    }

    val fittedRows: Int = WindowItemFit.count(
        itemHeight = 180.dp,
        itemSpacing = SizeConstants.LargeSize,
        paddingValues = paddingValues
    )

    val totalRowsToDisplay: Int by remember(fittedRows) {
        derivedStateOf { if (fittedRows == 0) 1 else fittedRows + 1 }
    }
    val actualItemCount: Int by remember(totalRowsToDisplay, numberOfColumns) {
        derivedStateOf { totalRowsToDisplay * numberOfColumns }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(count = numberOfColumns),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(space = SizeConstants.LargeSize),
        verticalArrangement = Arrangement.spacedBy(space = SizeConstants.LargeSize),
        modifier = Modifier.padding(horizontal = SizeConstants.LargeSize),
        userScrollEnabled = false
    ) {
        items(
            count = actualItemCount,
            key = { index: Int -> index }
        ) {
            ShimmerPlaceholderAppCard(aspectRatio = itemAspectRatio)
        }
    }
}
