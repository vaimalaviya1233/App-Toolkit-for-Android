package com.d4rk.android.libs.apptoolkit.app.main.ui.components.navigation

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.support.ui.SupportActivity
import com.d4rk.android.libs.apptoolkit.core.ui.components.buttons.AnimatedIconButtonDirection
import com.d4rk.android.libs.apptoolkit.core.ui.components.dropdown.CommonDropdownMenuItem
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    navigationIcon: ImageVector,
    onNavigationIconClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val context: Context = LocalContext.current
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        navigationIcon = {
            AnimatedIconButtonDirection(
                icon = navigationIcon,
                contentDescription = stringResource(id = R.string.go_back),
                onClick = { onNavigationIconClick() },
                vibrate = false
            )
        },
        actions = {
            var expandedMenu by remember { mutableStateOf(value = false) }

            AnimatedIconButtonDirection(
                fromRight = true,
                icon = Icons.Outlined.MoreVert,
                contentDescription = stringResource(id = R.string.content_description_more_options),
                onClick = { expandedMenu = true },
            )

            DropdownMenu(expanded = expandedMenu, onDismissRequest = { expandedMenu = false }) {
                CommonDropdownMenuItem(
                    textResId = R.string.support_us,
                    icon = Icons.Outlined.VolunteerActivism,
                    onClick = {
                        expandedMenu = false
                        IntentsHelper.openActivity(context, SupportActivity::class.java)
                    }
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}
