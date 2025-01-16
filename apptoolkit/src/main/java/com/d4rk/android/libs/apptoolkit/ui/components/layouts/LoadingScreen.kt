package com.d4rk.android.libs.apptoolkit.ui.components.layouts

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

/**
 * A composable function that displays a loading screen with a circular progress indicator.
 *
 * The loading screen covers the entire screen and its opacity is controlled by the [progressAlpha] parameter.
 * It uses [CircularProgressIndicator] to visually indicate the loading process.
 *
 * @param progressAlpha A float value between 0 and 1 that controls the opacity of the loading screen.
 *                      0 means fully transparent, 1 means fully opaque.
 */
@Composable
fun LoadingScreen(progressAlpha : Float) {
    Box(
        modifier = Modifier
                .fillMaxSize()
                .animateContentSize()
                .alpha(alpha = progressAlpha) ,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}