package com.d4rk.android.libs.apptoolkit.app.ads.ui

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.ads.domain.events.AdsSettingsEvents
import com.d4rk.android.libs.apptoolkit.app.ads.domain.model.AdsSettingsData
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.LoadingScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.NoDataScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.ScreenStateHandler
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.sections.InfoMessageSection
import com.d4rk.android.libs.apptoolkit.core.ui.components.navigation.LargeTopAppBarWithScaffold
import com.d4rk.android.libs.apptoolkit.core.ui.components.preferences.PreferenceItem
import com.d4rk.android.libs.apptoolkit.core.ui.components.preferences.SwitchCardItem
import com.d4rk.android.libs.apptoolkit.core.utils.constants.links.AppLinks
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AdsSettingsScreen(activity : Activity , viewModel : AdsSettingsViewModel , buildInfoProvider : BuildInfoProvider) {
    val uiState : UiStateScreen<AdsSettingsData> by viewModel.uiState.collectAsState()
    ScreenStateHandler(screenState = uiState , onLoading = {
        LoadingScreen()
    } , onEmpty = {
        NoDataScreen(showRetry = true , onRetry = {
            viewModel.onEvent(event = AdsSettingsEvents.LoadAdsSettings)
        })
    } , onSuccess = {
        AdSettingsScreenContent(
            viewModel = viewModel , activity = activity , buildInfoProvider = buildInfoProvider
        )
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdSettingsScreenContent(viewModel : AdsSettingsViewModel , activity : Activity , buildInfoProvider : BuildInfoProvider) {
    val context : Context = LocalContext.current
    val coroutineScope : CoroutineScope = rememberCoroutineScope()
    val dataStore : CommonDataStore = CommonDataStore.getInstance(context)
    val adsEnabled : Boolean by dataStore.ads(default = ! buildInfoProvider.isDebugBuild).collectAsState(initial = ! buildInfoProvider.isDebugBuild)

    LargeTopAppBarWithScaffold(
        title = stringResource(id = R.string.ads) , onBackClicked = { (context as? Activity)?.finish() }) { paddingValues : PaddingValues ->
        LazyColumn(
            modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = paddingValues)
        ) {
            item {
                SwitchCardItem(
                    title = stringResource(id = R.string.display_ads) , switchState = rememberUpdatedState(newValue = adsEnabled)
                ) { isChecked : Boolean ->
                    coroutineScope.launch {
                        dataStore.saveAds(isChecked = isChecked)
                    }
                }
            }

            item {
                Box(modifier = Modifier.padding(horizontal = SizeConstants.SmallSize)) {
                    PreferenceItem(
                        title = stringResource(id = R.string.personalized_ads) , enabled = adsEnabled , summary = stringResource(id = R.string.summary_ads_personalized_ads) , onClick = {
                            viewModel.onEvent(event = AdsSettingsEvents.OpenConsentForm(activity = activity as AdsSettingsActivity))
                        })
                }
            }

            item {
                InfoMessageSection(modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 24.dp) , message = stringResource(id = R.string.summary_ads) , learnMoreText = stringResource(id = R.string.learn_more) , learnMoreUrl = AppLinks.ADS_HELP_CENTER)
            }
        }
    }
}