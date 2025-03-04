package com.d4rk.android.libs.apptoolkit.ui.components.layouts

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * A composable function that displays a full-screen loading indicator.
 *
 * This function creates a `Box` that fills the entire available space and centers
 * a `CircularProgressIndicator` within it. The `animateContentSize` modifier is
 * used to smoothly animate changes in the size of the `Box`, although in this
 * particular case with the full screen and circular progress, the animation won't
 * be visually noticeable.
 *
 * The loading screen is often used to indicate that the application is performing
 * some background task, such as fetching data from a network, and that the user
 * should wait until the task is completed.
 *
 * @param modifier Modifier to be applied to the layout. Defaults to fillMaxSize() and animateContentSize().
 *
 * Example Usage:
 *
 * ```
 *  if (isLoading) {
 *      LoadingScreen()
 *  } else {
 *      // Display the main content here
 *      MainContent()
 *  }
 * ```
 */
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
                .fillMaxSize()
                .animateContentSize() , contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}