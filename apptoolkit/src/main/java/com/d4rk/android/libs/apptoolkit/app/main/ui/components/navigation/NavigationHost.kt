package com.d4rk.android.libs.apptoolkit.app.main.ui.components.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun NavigationHost(navController : NavHostController , startDestination : String , navGraphBuilder : NavGraphBuilder.() -> Unit) {
    val enterAnimation : EnterTransition = remember {
        fadeIn() + scaleIn(animationSpec = tween(durationMillis = 200))
    }
    val exitAnimation : ExitTransition = remember {
        fadeOut() + scaleOut(animationSpec = tween(durationMillis = 200))
    }

    NavHost(navController = navController , startDestination = startDestination , enterTransition = { enterAnimation } , exitTransition = { exitAnimation } , builder = navGraphBuilder)
}