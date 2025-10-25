package com.d4rk.android.apps.apptoolkit.app.main.ui.components.navigation

import android.content.Context
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.d4rk.android.apps.apptoolkit.app.main.domain.model.ui.UiMainScreen
import com.d4rk.android.apps.apptoolkit.app.main.ui.MainScaffoldContent
import com.d4rk.android.libs.apptoolkit.app.main.ui.components.dialogs.ChangelogDialog
import com.d4rk.android.libs.apptoolkit.app.main.ui.components.navigation.NavigationDrawerItemContent
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.navigation.NavigationDrawerItem
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.hapticDrawerSwipe
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.LargeVerticalSpacer
import kotlinx.coroutines.CoroutineScope
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun NavigationDrawer(
    screenState: UiStateScreen<UiMainScreen>,
    windowWidthSizeClass: WindowWidthSizeClass,
) {
    val drawerState : DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope : CoroutineScope = rememberCoroutineScope()
    val context : Context = LocalContext.current
    val changelogUrl: String = koinInject(qualifier = named("github_changelog"))
    val buildInfoProvider: BuildInfoProvider = koinInject()
    val dispatchers: DispatcherProvider = koinInject()
    var showChangelog by rememberSaveable { mutableStateOf(false) }
    val uiState : UiMainScreen = screenState.data ?: UiMainScreen()

    ModalNavigationDrawer(
        modifier = Modifier.hapticDrawerSwipe(state = drawerState) , drawerState = drawerState , drawerContent = {
            ModalDrawerSheet {
                LargeVerticalSpacer()
                uiState.navigationDrawerItems.forEach { item : NavigationDrawerItem ->
                    NavigationDrawerItemContent(item = item , handleNavigationItemClick = {
                        handleNavigationItemClick(
                            context = context,
                            item = item,
                            drawerState = drawerState,
                            coroutineScope = coroutineScope,
                            onChangelogRequested = { showChangelog = true }
                        )
                    })
                }
            }
        }) {
        MainScaffoldContent(
            drawerState = drawerState,
            windowWidthSizeClass = windowWidthSizeClass,
        )
    }

    if (showChangelog) {
        ChangelogDialog(
            changelogUrl = changelogUrl,
            buildInfoProvider = buildInfoProvider,
            onDismiss = { showChangelog = false },
            dispatchers = dispatchers
        )
    }
}
