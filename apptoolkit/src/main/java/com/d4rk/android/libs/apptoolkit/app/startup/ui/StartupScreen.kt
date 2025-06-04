package com.d4rk.android.libs.apptoolkit.app.startup.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImage
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.startup.domain.model.StartupUiData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.buttons.fab.AnimatedExtendedFloatingActionButton
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.LoadingScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.NoDataScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.ScreenStateHandler
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.sections.InfoMessageSection
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick
import com.d4rk.android.libs.apptoolkit.core.ui.components.navigation.TopAppBarScaffold

@Composable
fun StartupScreen(activity : StartupActivity , viewModel : StartupViewModel) {
    val uiStateScreen : UiStateScreen<StartupUiData> by viewModel.uiState.collectAsState()
    val consentFormLoaded : Boolean = uiStateScreen.data?.consentFormLoaded == true

    TopAppBarScaffold(title = stringResource(R.string.welcome) , content = { paddingValues ->
        ScreenStateHandler(screenState = uiStateScreen , onLoading = {
            LoadingScreen()
        } , onEmpty = {
            NoDataScreen()
        } , onSuccess = {
            StartupScreenContent(paddingValues = paddingValues)
        })
    } , floatingActionButton = {
        AnimatedExtendedFloatingActionButton(
            visible = consentFormLoaded , modifier = Modifier.bounceClick() , containerColor = if (consentFormLoaded) {
            FloatingActionButtonDefaults.containerColor
        }
        else {
            Gray
        } , text = { Text(text = stringResource(id = R.string.agree)) } , onClick = {
            activity.navigateToNext()
        } , icon = {
            Icon(
                imageVector = Icons.Outlined.CheckCircle , contentDescription = null
            )
        })
    })
}

@Composable
fun StartupScreenContent(paddingValues : PaddingValues) {
    Box(
        modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
                .padding(all = SizeConstants.MediumSize * 2)
                .safeDrawingPadding()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                AsyncImage(model = R.drawable.il_startup , contentDescription = null)
                InfoMessageSection(message = stringResource(R.string.summary_browse_terms_of_service_and_privacy_policy) , learnMoreText = stringResource(R.string.learn_more) , learnMoreUrl = "https://sites.google.com/view/d4rk7355608/more/apps/privacy-policy")
            }
        }
    }
}