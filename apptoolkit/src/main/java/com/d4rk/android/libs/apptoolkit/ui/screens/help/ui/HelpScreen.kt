package com.d4rk.android.libs.apptoolkit.ui.screens.help.ui

import android.app.Activity
import android.content.Context
import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import com.d4rk.android.libs.apptoolkit.ui.components.buttons.AnimatedExtendedFloatingActionButton
import com.d4rk.android.libs.apptoolkit.ui.components.layouts.LoadingScreen
import com.d4rk.android.libs.apptoolkit.ui.components.layouts.NoDataScreen
import com.d4rk.android.libs.apptoolkit.ui.components.layouts.ScreenStateHandler
import com.d4rk.android.libs.apptoolkit.ui.components.navigation.LargeTopAppBarWithScaffold
import com.d4rk.android.libs.apptoolkit.ui.components.network.rememberHtmlData
import com.d4rk.android.libs.apptoolkit.ui.components.spacers.MediumVerticalSpacer
import com.d4rk.android.libs.apptoolkit.ui.screens.help.domain.actions.HelpAction
import com.d4rk.android.libs.apptoolkit.ui.screens.help.domain.model.ui.HelpScreenConfig
import com.d4rk.android.libs.apptoolkit.ui.screens.help.domain.model.ui.UiHelpScreen
import com.d4rk.android.libs.apptoolkit.ui.screens.help.ui.components.ContactUsCard
import com.d4rk.android.libs.apptoolkit.ui.screens.help.ui.components.HelpQuestionsList
import com.d4rk.android.libs.apptoolkit.ui.screens.help.ui.components.dropdown.HelpScreenMenuActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(activity : Activity , viewModel : HelpViewModel , config : HelpScreenConfig) {
    val screenState : UiStateScreen<UiHelpScreen> by viewModel.screenState.collectAsState()
    val context : Context = LocalContext.current
    val view : View = LocalView.current
    val topAppBarState : TopAppBarState = rememberTopAppBarState()
    val scrollBehavior : TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state = topAppBarState)

    val htmlData : State<Pair<String? , String?>> = rememberHtmlData(context = context , currentVersionName = config.versionName , packageName = activity.packageName)
    val changelogHtmlString : String? = htmlData.value.first
    val eulaHtmlString : String? = htmlData.value.second

    val isFabExtended : MutableState<Boolean> = remember { mutableStateOf(value = true) }
    LaunchedEffect(key1 = scrollBehavior.state.contentOffset) {
        isFabExtended.value = scrollBehavior.state.contentOffset >= 0f
    }

    LargeTopAppBarWithScaffold(title = stringResource(id = R.string.help) , onBackClicked = { activity.finish() } , actions = {
        HelpScreenMenuActions(context = context , activity = activity , showDialog = remember { mutableStateOf(value = false) } , eulaHtmlString = eulaHtmlString , changelogHtmlString = changelogHtmlString , view = view , config = config)
    } , scrollBehavior = scrollBehavior , floatingActionButton = {
        AnimatedExtendedFloatingActionButton(visible = screenState.data?.reviewInfo != null , expanded = isFabExtended.value , onClick = {
            screenState.data?.reviewInfo?.let { reviewInfo -> viewModel.sendEvent(HelpAction.LaunchReviewFlow(activity , reviewInfo)) }
        } , text = { Text(text = stringResource(id = R.string.feedback)) } , icon = { Icon(Icons.Outlined.RateReview , contentDescription = null) })
    }) { paddingValues ->
        ScreenStateHandler(screenState = screenState , onLoading = {
            LoadingScreen()
        } , onEmpty = {
            NoDataScreen()
        } , onSuccess = { helpData ->
            HelpScreenContent(helpData = helpData , paddingValues = paddingValues , activity = activity , view = view)
        })
    }
}

@Composable
fun HelpScreenContent(helpData : UiHelpScreen , paddingValues : PaddingValues , activity : Activity , view : View) {
    LazyColumn(
        modifier = Modifier.fillMaxSize() , contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() , bottom = paddingValues.calculateBottomPadding() , start = SizeConstants.LargeSize , end = SizeConstants.LargeSize
        )
    ) {
        item {
            Text(text = stringResource(id = R.string.popular_help_resources))
            MediumVerticalSpacer()
            Card(modifier = Modifier.fillMaxWidth()) {
                HelpQuestionsList(questions = helpData.questions)
            }
            MediumVerticalSpacer()
            ContactUsCard(onClick = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                IntentsHelper.sendEmailToDeveloper(context = activity , applicationNameRes = R.string.app_name)
            })
            Spacer(modifier = Modifier.height(height = 96.dp))
        }
    }
}