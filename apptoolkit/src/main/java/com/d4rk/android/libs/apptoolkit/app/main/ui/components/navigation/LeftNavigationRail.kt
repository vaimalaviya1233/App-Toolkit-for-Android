package com.d4rk.android.libs.apptoolkit.app.main.ui.components.navigation

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.app.main.utils.interfaces.BottomNavigationItem
import com.d4rk.android.libs.apptoolkit.core.domain.model.navigation.NavigationDrawerItem
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick

@Composable
fun LeftNavigationRail(
    bottomItems : List<BottomNavigationItem> = emptyList() ,
    drawerItems : List<NavigationDrawerItem> = emptyList() ,
    currentRoute : String? ,
    isRailExpanded : Boolean = false ,
    paddingValues : PaddingValues ,
    onBottomItemClick : (BottomNavigationItem) -> Unit = {} ,
    onDrawerItemClick : (NavigationDrawerItem) -> Unit = {} ,
    content : @Composable () -> Unit ,
    modifier : Modifier = Modifier
) {
    val railWidth : Dp by animateDpAsState(
        targetValue = if (isRailExpanded) 200.dp else 72.dp , animationSpec = tween(durationMillis = 300)
    )

    Row(modifier = modifier.padding(top = paddingValues.calculateTopPadding())) {
        NavigationRail(modifier = Modifier.width(railWidth)) {
            bottomItems.forEach { item ->
                NavigationRailItem(modifier = Modifier.bounceClick() , selected = currentRoute == item.route , onClick = { onBottomItemClick(item) } , icon = {
                    Icon(imageVector = if (currentRoute == item.route) item.selectedIcon else item.icon , contentDescription = stringResource(id = item.title))
                } , label = if (isRailExpanded) {
                    { Text(text = stringResource(id = item.title) , maxLines = 1 , modifier = Modifier.animateContentSize() , overflow = TextOverflow.Ellipsis) }
                }
                else null)
            }

            Spacer(modifier = Modifier.weight(1f))

            drawerItems.forEach { item ->
                NavigationRailItem(selected = false , onClick = { onDrawerItemClick(item) } , icon = {
                    Icon(
                        imageVector = item.selectedIcon , contentDescription = stringResource(id = item.title)
                    )
                } , label = if (isRailExpanded) {
                    { Text(text = stringResource(id = item.title)) }
                }
                else null)
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}