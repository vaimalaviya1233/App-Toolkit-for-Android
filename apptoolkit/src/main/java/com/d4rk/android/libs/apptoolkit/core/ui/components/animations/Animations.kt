package com.d4rk.android.libs.apptoolkit.core.ui.components.animations

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.delay

/**
 * Remembers and manages the visibility states of items in a lazy list along with the visibility state of a floating action button (FAB).
 *
 * This composable function orchestrates the animation of items appearing in a `LazyList` and the visibility of a FAB.
 * It uses a `SnapshotStateList` to track the individual visibility of each item in the list, and a `MutableState` to control the FAB's visibility.
 *
 * **Functionality:**
 * 1. **Initialization:**
 *    - Creates a `SnapshotStateList` (`visibilityStates`) to store the visibility state (true/false) for each item in the list. Initially, all items are set to invisible (false).
 *    - Creates a `MutableState` (`isFabVisible`) to control the FAB's visibility, initially set to invisible (false).
 *    - Initializes a boolean flag `initialAnimationPlayed` to `false`, tracking if the initial animation has completed.
 * 2. **Item Count Change Handling:**
 *    - When the `itemCount` changes, the `LaunchedEffect` clears the `visibilityStates` and recreates it with the new size, setting all items to invisible (false).
 *    - If `itemCount` is 0, it immediately sets the FAB to visible.
 * 3. **Initial Animation:**
 *    - A `LaunchedEffect` with `Unit` as the key triggers the initial animation sequence once when the composable is first composed.
 *    - It iterates through the currently visible items in the `LazyList` (from `firstVisibleItemIndex` to the last visible item).
 *    - For each visible item, it adds a small delay (increasing with index) and then sets the corresponding `visibilityStates` to `true`, effectively revealing the item.
 */
@Composable
fun rememberAnimatedVisibilityState(
    listState : LazyListState , itemCount : Int
) : Pair<SnapshotStateList<Boolean> , MutableState<Boolean>> {
    val visibilityStates : SnapshotStateList<Boolean> = remember { mutableStateListOf() }
    val isFabVisible : MutableState<Boolean> = remember { mutableStateOf(value = false) }
    var initialAnimationPlayed : Boolean by remember { mutableStateOf(value = false) }

    LaunchedEffect(key1 = itemCount) {
        visibilityStates.clear()
        visibilityStates.addAll(List(size = itemCount) { false })
        if (itemCount == 0) {
            isFabVisible.value = true
        }
    }

    LaunchedEffect(key1 = Unit) {
        val firstVisible : Int = listState.firstVisibleItemIndex
        val lastVisible : Int = (firstVisible + listState.layoutInfo.visibleItemsInfo.size - 1).coerceAtMost(maximumValue = itemCount - 1)
        for (index in firstVisible..lastVisible) {
            delay(timeMillis = index * 8L)
            visibilityStates[index] = true
        }
        initialAnimationPlayed = true
        delay(timeMillis = 50L)
        isFabVisible.value = true
    }

    if (initialAnimationPlayed) {
        for (index in 0 until itemCount) {
            if (index < visibilityStates.size && ! visibilityStates[index]) {
                visibilityStates[index] = true
            }
        }
    }

    return visibilityStates to isFabVisible
}