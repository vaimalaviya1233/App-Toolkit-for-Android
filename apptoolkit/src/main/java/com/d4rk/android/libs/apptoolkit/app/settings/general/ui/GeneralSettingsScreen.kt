package com.d4rk.android.libs.apptoolkit.app.settings.general.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.GeneralSettingsContentProvider
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.LoadingScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.NoDataScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.ScreenStateHandler
import com.d4rk.android.libs.apptoolkit.core.ui.components.navigation.LargeTopAppBarWithScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen(
    title : String , viewModel : GeneralSettingsViewModel , contentProvider : GeneralSettingsContentProvider , onBackClicked : () -> Unit
) {
    val screenState by viewModel.screenState.collectAsState()

    LargeTopAppBarWithScaffold(title = title , onBackClicked = onBackClicked) { paddingValues ->
        ScreenStateHandler(screenState = screenState , onLoading = { LoadingScreen() } , onEmpty = { NoDataScreen() } , onSuccess = { contentKey ->
            contentProvider.ProvideContent(contentKey , paddingValues)
        })
    }
}
