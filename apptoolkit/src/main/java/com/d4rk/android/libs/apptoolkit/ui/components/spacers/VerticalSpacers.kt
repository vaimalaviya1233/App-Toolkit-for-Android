package com.d4rk.android.libs.apptoolkit.ui.components.spacers

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.utils.constants.ui.SizeConstants

/**
 * A composable function that creates a vertical spacer with a predefined height.
 *
 * This function provides a convenient way to add a consistent 16dp vertical space between
 * UI elements in your composable layouts. It helps improve readability and maintain a
 * uniform visual spacing throughout your application.
 *
 * @sample
 * ```kotlin
 * Column {
 *   Text("Item 1")
 *   LargeVerticalSpacer()
 *   Text("Item 2")
 * }
 * ```
 *
 * @see Spacer
 */
@Composable
fun LargeVerticalSpacer() {
    Spacer(modifier = Modifier.height(height = SizeConstants.LargeSize))
}

/**
 * A composable function that provides a vertical space with a fixed height of 12 dp.
 *
 * This composable is useful for creating consistent, medium-sized gaps between
 * elements within a vertical layout, enhancing the visual structure and
 * readability of your UI. It leverages the standard `Spacer` composable with a
 * pre-defined height, promoting code reusability and uniformity in spacing.
 *
 * @sample
 * ```kotlin
 * Column {
 *   Text("First Item")
 *   MediumVerticalSpacer()
 *   Text("Second Item")
 * }
 * ```
 *
 * @see Spacer
 * @see Modifier.height
 */
@Composable
fun MediumVerticalSpacer() {
    Spacer(modifier = Modifier.height(height = 12.dp))
}

/**
 * A composable function that creates a small vertical space of 8 dp.
 *
 * This is often used to add visual separation between elements in a layout.
 *
 * Example Usage:
 * ```
 * Column {
 *   Text("First Item")
 *   SmallVerticalSpacer()
 *   Text("Second Item")
 * }
 * ```
 */
@Composable
fun SmallVerticalSpacer() {
    Spacer(modifier = Modifier.height(height = 8.dp))
}