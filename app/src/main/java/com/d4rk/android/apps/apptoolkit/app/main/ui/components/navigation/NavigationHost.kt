package com.d4rk.android.apps.apptoolkit.app.main.ui.components.navigation

import android.content.Context
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.d4rk.android.apps.apptoolkit.app.main.utils.constants.NavigationRoutes
import com.d4rk.android.libs.apptoolkit.app.help.ui.HelpActivity
import com.d4rk.android.libs.apptoolkit.app.settings.settings.ui.SettingsActivity
import com.d4rk.android.libs.apptoolkit.core.domain.model.navigation.NavigationDrawerItem
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NavigationHost(navController : NavHostController , snackbarHostState : SnackbarHostState , onFabVisibilityChanged : (Boolean) -> Unit , paddingValues : PaddingValues) {

    val mEnterAnimation : EnterTransition = remember {
        fadeIn() + scaleIn(animationSpec = tween(durationMillis = 200))
    }

    val mExitAnimation : ExitTransition = remember {
        fadeOut() + scaleOut(animationSpec = tween(durationMillis = 200))
    }

    NavHost(navController = navController , startDestination = NavigationRoutes.ROUTE_HOME , enterTransition = {
        mEnterAnimation
    } , exitTransition = {
        mExitAnimation
    }) {
        composable(route = NavigationRoutes.ROUTE_HOME) {
            Box(
                modifier = Modifier.fillMaxSize() , contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(text = "Home Screen")
            }
        }
    }
}

fun handleNavigationItemClick(
    context : Context , item : NavigationDrawerItem , drawerState : DrawerState , coroutineScope : CoroutineScope
) {
    when (item.title) {
        com.d4rk.android.libs.apptoolkit.R.string.settings -> {
              IntentsHelper.openActivity(
                context = context , activityClass = SettingsActivity::class.java
            )
        }

        com.d4rk.android.libs.apptoolkit.R.string.help_and_feedback -> {
            IntentsHelper.openActivity(
                context = context , activityClass = HelpActivity::class.java
            )
        }

        com.d4rk.android.libs.apptoolkit.R.string.updates -> {
            IntentsHelper.openUrl(
                context = context , url = "https://github.com/D4rK7355608/${context.packageName}/blob/master/CHANGELOG.md"
            )
        }

        com.d4rk.android.libs.apptoolkit.R.string.share -> {
            IntentsHelper.shareApp(
                context = context , shareMessageFormat = com.d4rk.android.libs.apptoolkit.R.string.summary_share_message
            )
        }
    }
    coroutineScope.launch { drawerState.close() }
}