package com.d4rk.android.apps.apptoolkit.app.main.ui.components.navigation

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.DrawerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.d4rk.android.apps.apptoolkit.app.apps.ui.AppsListScreen
import com.d4rk.android.apps.apptoolkit.app.compass.ui.CompassScreen
import com.d4rk.android.apps.apptoolkit.app.compass.ui.rememberCompassSensorState
import com.d4rk.android.apps.apptoolkit.app.main.utils.constants.NavigationRoutes
import com.d4rk.android.apps.apptoolkit.app.tools.domain.data.model.ToolActionType
import com.d4rk.android.apps.apptoolkit.app.tools.domain.data.model.ui.ToolItem
import com.d4rk.android.apps.apptoolkit.app.tools.ui.ToolsList
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
    val context = LocalContext.current
    var isFlashlightOn by remember { mutableStateOf(false) }

    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraId = try {
        cameraManager.cameraIdList[0]
    } catch (e : Exception) {
        null
    }

    fun toggleFlashlight() {
        if (true && cameraId != null) {
            try {
                cameraManager.setTorchMode(cameraId , ! isFlashlightOn)
                isFlashlightOn = ! isFlashlightOn
                val status = if (isFlashlightOn) "ON" else "OFF"
                Toast.makeText(context , "Flashlight $status" , Toast.LENGTH_SHORT).show()
            } catch (e : Exception) {
                Toast.makeText(context , "Error toggling flashlight" , Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
        else {
            Toast.makeText(context , "Flashlight not available" , Toast.LENGTH_SHORT).show()
        }
    }

    NavigationHost(
        navController = navController , startDestination = NavigationRoutes.ROUTE_TOOLS_LIST
    ) {
        composable(route = NavigationRoutes.ROUTE_TOOLS_LIST) {
            val toolItemsList = listOf(
                ToolItem(
                    id = "apps" , icon = Icons.Filled.Apps , iconBackgroundColor = Color(0xFF4285F4) , title = "Useful Apps" , subtitle = "Manage your installed apps" , actionType = ToolActionType.NAVIGATE , destinationRoute = NavigationRoutes.ROUTE_APPS_LIST
                ) ,
                ToolItem(
                    id = "flashlight" , icon = Icons.Filled.FlashlightOn , iconBackgroundColor = Color(0xFF03A9F4) , title = "Flashlight" , subtitle = if (isFlashlightOn) "Turn Off" else "Turn On" , actionType = ToolActionType.TOGGLE_FLASHLIGHT
                ) ,
                ToolItem(
                    id = "compass" , icon = Icons.Filled.Explore , iconBackgroundColor = Color(0xFFE53935) , title = "Compass" , subtitle = "Find your direction" , actionType = ToolActionType.NAVIGATE , destinationRoute = NavigationRoutes.ROUTE_COMPASS
                ) ,
            )
            ToolsList(
                toolItems = toolItemsList , paddingValues = paddingValues , onToolClick = { clickedTool ->
                    when (clickedTool.actionType) {
                        ToolActionType.NAVIGATE -> {
                            clickedTool.destinationRoute?.let { route ->
                                navController.navigate(route)
                            }
                        }

                        ToolActionType.TOGGLE_FLASHLIGHT -> {
                            toggleFlashlight()
                        }

                    }
                })
        }
        composable(route = NavigationRoutes.ROUTE_APPS_LIST) {
            AppsListScreen(paddingValues = paddingValues)
        }
        composable(route = NavigationRoutes.ROUTE_COMPASS) {
            CompassScreen(
                paddingValues = paddingValues,
                sensorState   = rememberCompassSensorState()
            )
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