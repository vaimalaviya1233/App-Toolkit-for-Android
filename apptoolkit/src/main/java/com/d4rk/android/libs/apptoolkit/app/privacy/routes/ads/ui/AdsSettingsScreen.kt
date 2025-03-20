package com.d4rk.android.libs.apptoolkit.app.privacy.routes.ads.ui

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.app.privacy.routes.ads.domain.model.AdsSettingsData
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.LoadingScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.ScreenStateHandler
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.sections.InfoMessageSection
import com.d4rk.android.libs.apptoolkit.core.ui.components.navigation.LargeTopAppBarWithScaffold
import com.d4rk.android.libs.apptoolkit.core.ui.components.preferences.PreferenceItem
import com.d4rk.android.libs.apptoolkit.core.ui.components.preferences.SwitchCardItem

@Composable
fun AdsSettingsScreen(viewModel: AdsSettingsViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.screenState.collectAsState()

    ScreenStateHandler(
        screenState = uiState,
        onLoading = {
          LoadingScreen()
        },
        onEmpty = {
           // todo no data
        },
        onSuccess = { data ->
            AdSettingsScreenContent(context = context , data = data , viewModel = viewModel)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdSettingsScreenContent(context : Context, data: AdsSettingsData, viewModel : AdsSettingsViewModel) {
    LargeTopAppBarWithScaffold(
        title = stringResource(id = com.d4rk.android.libs.apptoolkit.R.string.ads),
        onBackClicked = { (context as? Activity)?.finish() }) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
            ) {
                item(key = "display_ads") {
                    SwitchCardItem(
                        title = stringResource(id = com.d4rk.android.libs.apptoolkit.R.string.display_ads),
                        switchState = remember { mutableStateOf(data.adsEnabled) }
                    ) { isChecked ->
                        viewModel.toggleAds(isChecked)
                    }
                }
                item {
                    Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                        PreferenceItem(
                            title = stringResource(id = com.d4rk.android.libs.apptoolkit.R.string.personalized_ads),
                            enabled = data.adsEnabled,
                            summary = stringResource(id = com.d4rk.android.libs.apptoolkit.R.string.summary_ads_personalized_ads),
                            onClick = {
                                viewModel.openConsentForm(context as AdsSettingsActivity)
                            })
                    }
                }

                item {
                    InfoMessageSection(
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 24.dp),
                        message = stringResource(id = com.d4rk.android.libs.apptoolkit.R.string.summary_ads),
                        learnMoreText = stringResource(id = com.d4rk.android.libs.apptoolkit.R.string.learn_more),
                        learnMoreUrl = "https://sites.google.com/view/d4rk7355608/more/apps/ads-help-center"
                    )
                }
            }
        }
    }
}