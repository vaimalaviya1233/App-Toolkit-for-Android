package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Utility helpers for navigation related logic.
 */
object NavigationHelper {
    /**
     * Returns the current route for the provided [navController].
     */
    @Composable
    fun currentRoute(navController: NavController): String? {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val route = backStackEntry?.destination?.route ?: navController.currentDestination?.route
        println("NavigationHelper.currentRoute -> $route")
        return route
    }
}
