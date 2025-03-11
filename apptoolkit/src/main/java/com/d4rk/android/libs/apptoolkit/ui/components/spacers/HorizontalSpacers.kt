package com.d4rk.android.libs.apptoolkit.ui.components.spacers

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants

/**
 *  A composable function that creates a horizontal space with a width of 16 dp.
 *
 *  This can be used to provide consistent spacing between elements in a layout.
 *
 * @sample
 * ```kotlin
 * Row {
 *   Text("Item 1")
 *   LargeHorizontalSpacer()
 *   Text("Item 2")
 * }
 * ```
 */
@Composable
fun LargeHorizontalSpacer() {
    Spacer(modifier = Modifier.width(width = SizeConstants.LargeSize))
}

/**
 * A composable function that provides a horizontal space of medium size (12 dp).
 *
 * This function creates a [Spacer] with a fixed width of 12 density-independent pixels
 * (dp). It is useful for creating consistent spacing between elements in a horizontal layout.
 *
 * @see Spacer
 * @see Modifier.width
 *
 * Example usage:
 * ```
 * Row {
 *   Text("Item 1")
 *   MediumHorizontalSpacer()
 *   Text("Item 2")
 * }
 * ```
 */
@Composable
fun MediumHorizontalSpacer() {
    Spacer(modifier = Modifier.width(width = SizeConstants.MediumSize))
}

/**
 * A small horizontal spacer with a width of 8dp.
 *
 * This composable can be used to add a small gap between elements arranged horizontally.
 *
 * @sample
 *  Row {
 *      Text("Item 1")
 *      SmallHorizontalSpacer()
 *      Text("Item 2")
 *  }
 */
@Composable
fun SmallHorizontalSpacer() {
    Spacer(modifier = Modifier.width(width = SizeConstants.SmallSize))
}

@Composable
fun ExtraSmallHorizontalSpacer() {
    Spacer(modifier = Modifier.width(width = SizeConstants.ExtraSmallSize))
}