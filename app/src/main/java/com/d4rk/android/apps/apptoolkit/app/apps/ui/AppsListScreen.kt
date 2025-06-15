package com.d4rk.android.apps.apptoolkit.app.apps.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.d4rk.android.apps.apptoolkit.app.apps.domain.actions.HomeEvent
import com.d4rk.android.apps.apptoolkit.app.apps.domain.model.ui.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.app.apps.ui.components.AppsList
import com.d4rk.android.apps.apptoolkit.app.apps.ui.components.screens.loading.HomeLoadingScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.NoDataScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.ScreenStateHandler
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppsListScreen(paddingValues : PaddingValues) {
    val viewModel : AppsListViewModel = koinViewModel()
    val screenState : UiStateScreen<UiHomeScreen> by viewModel.uiState.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    // Content does not trigger in-app review directly; handled in MainActivity

    ScreenStateHandler(screenState = screenState , onLoading = {
        HomeLoadingScreen(paddingValues = paddingValues)
    } , onEmpty = {
        NoDataScreen(showRetry = true , onRetry = {
            viewModel.onEvent(HomeEvent.FetchApps)
        })
    } , onSuccess = { uiHomeScreen ->
        Column {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SizeConstants.MediumSize, vertical = SizeConstants.SmallSize),
                value = query,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text(text = "Search") }
            )
            Row(modifier = Modifier.padding(horizontal = SizeConstants.MediumSize)) {
                IconButton(onClick = { viewModel.toggleSortOrder() }) {
                    Icon(imageVector = Icons.Outlined.Sort, contentDescription = "Sort")
                }
            }
            AppsList(uiHomeScreen = uiHomeScreen , paddingValues = paddingValues)
        }
    })
}