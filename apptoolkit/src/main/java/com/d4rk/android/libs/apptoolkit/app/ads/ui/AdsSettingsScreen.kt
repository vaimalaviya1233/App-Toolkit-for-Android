package com.d4rk.android.libs.apptoolkit.app.ads.ui

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.ads.domain.actions.AdsSettingsEvent
import com.d4rk.android.libs.apptoolkit.app.ads.domain.model.AdsSettingsData
import com.d4rk.android.libs.apptoolkit.app.ads.ui.components.AdsSnackbarHandler
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.LoadingScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.NoDataScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.ScreenStateHandler
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.sections.InfoMessageSection
import com.d4rk.android.libs.apptoolkit.core.ui.components.navigation.LargeTopAppBarWithScaffold
import com.d4rk.android.libs.apptoolkit.core.ui.components.preferences.PreferenceItem
import com.d4rk.android.libs.apptoolkit.core.ui.components.preferences.SwitchCardItem
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AdsSettingsScreen(activity : Activity , viewModel : AdsSettingsViewModel , buildInfoProvider : BuildInfoProvider) {

    val uiState : UiStateScreen<AdsSettingsData> by viewModel.uiState.collectAsState()
    val snackbarHostState : SnackbarHostState = remember { SnackbarHostState() }

    ScreenStateHandler(screenState = uiState , onLoading = {
        LoadingScreen()
    } , onEmpty = {
        NoDataScreen(showRetry = true , onRetry = {
            viewModel.onEvent(event = AdsSettingsEvent.LoadAdsSettings)
        })
    } , onSuccess = { data : AdsSettingsData ->
        AdSettingsScreenContent(
            data = data , viewModel = viewModel , activity = activity , snackbarHostState = snackbarHostState , buildInfoProvider = buildInfoProvider
        )
    })

    AdsSnackbarHandler(uiState = uiState , viewModel = viewModel , snackbarHostState = snackbarHostState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdSettingsScreenContent(data : AdsSettingsData , viewModel : AdsSettingsViewModel , activity : Activity , snackbarHostState : SnackbarHostState , buildInfoProvider : BuildInfoProvider) {
    val context : Context = LocalContext.current
    val dataStore : CommonDataStore = CommonDataStore.getInstance(context = context)
    val switchState : Boolean by dataStore.ads.collectAsState(initial = ! buildInfoProvider.isDebugBuild)
    val coroutineScope : CoroutineScope = rememberCoroutineScope()
    val adsEnabledState : MutableState<Boolean> = remember { mutableStateOf(value = switchState) }

    LargeTopAppBarWithScaffold(
        snackbarHostState = snackbarHostState , title = stringResource(id = R.string.ads) , onBackClicked = { (context as? Activity)?.finish() }) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues = paddingValues)
            ) {
                item {
                    SwitchCardItem(
                        title = stringResource(id = R.string.display_ads) , switchState = adsEnabledState
                    ) { isChecked : Boolean ->
                        adsEnabledState.value = isChecked
                        coroutineScope.launch {
                            dataStore.saveAds(isChecked = isChecked)
                        }
                        viewModel.onEvent(event = AdsSettingsEvent.AdsSettingChanged(isEnabled = isChecked))
                    }
                }
                item {
                    Box(modifier = Modifier.padding(horizontal = SizeConstants.SmallSize)) {
                        PreferenceItem(
                            title = stringResource(id = R.string.personalized_ads) , enabled = data.adsEnabled , summary = stringResource(id = R.string.summary_ads_personalized_ads) , onClick = {
                                viewModel.onEvent(event = AdsSettingsEvent.OpenConsentForm(activity = activity as AdsSettingsActivity))
                            })
                    }
                }

                item {
                    InfoMessageSection(
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 24.dp) , message = stringResource(id = R.string.summary_ads) , learnMoreText = stringResource(id = R.string.learn_more) , learnMoreUrl = "https://sites.google.com/view/d4rk7355608/more/apps/ads-help-center"
                    )
                }
            }
        }
    }
}