package com.d4rk.android.apps.apptoolkit.app.main.ui.components.navigation

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DrawerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.d4rk.android.apps.apptoolkit.app.apps.ui.AppsListScreen
import com.d4rk.android.apps.apptoolkit.app.apps.ui.FavoriteAppsScreen
import com.d4rk.android.apps.apptoolkit.app.main.utils.constants.NavigationRoutes
import com.d4rk.android.libs.apptoolkit.app.help.ui.HelpActivity
import com.d4rk.android.libs.apptoolkit.app.main.ui.components.navigation.NavigationHost
import com.d4rk.android.libs.apptoolkit.app.settings.settings.ui.SettingsActivity
import com.d4rk.android.libs.apptoolkit.core.domain.model.navigation.NavigationDrawerItem
import com.d4rk.android.libs.apptoolkit.core.utils.constants.links.AppLinks
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppNavigationHost(
    navController : NavHostController , snackbarHostState : SnackbarHostState , onFabVisibilityChanged : (Boolean) -> Unit , paddingValues : PaddingValues
) {
    NavigationHost(
        navController = navController , startDestination = NavigationRoutes.ROUTE_APPS_LIST
    ) {
        composable(route = NavigationRoutes.ROUTE_APPS_LIST) {
            AppsListScreen(paddingValues = paddingValues)
        }
        composable(route = NavigationRoutes.ROUTE_FAVORITE_APPS) {
            FavoriteAppsScreen(paddingValues = paddingValues)
        }
    }
}

fun handleNavigationItemClick(context : Context , item : NavigationDrawerItem , drawerState : DrawerState? = null , coroutineScope : CoroutineScope? = null) {
    when (item.title) {
        com.d4rk.android.libs.apptoolkit.R.string.settings -> IntentsHelper.openActivity(context = context , activityClass = SettingsActivity::class.java)
        com.d4rk.android.libs.apptoolkit.R.string.help_and_feedback -> IntentsHelper.openActivity(context = context , activityClass = HelpActivity::class.java)
        com.d4rk.android.libs.apptoolkit.R.string.updates -> IntentsHelper.openUrl(context = context , url = AppLinks.githubChangelog(context.packageName))
        com.d4rk.android.libs.apptoolkit.R.string.share -> IntentsHelper.shareApp(context = context , shareMessageFormat = com.d4rk.android.libs.apptoolkit.R.string.summary_share_message)
    }
    if (drawerState != null && coroutineScope != null) {
        coroutineScope.launch { drawerState.close() }
    }
}