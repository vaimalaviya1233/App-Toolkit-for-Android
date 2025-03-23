package com.d4rk.android.libs.apptoolkit.app.startup.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.startup.domain.model.StartupUiData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.LoadingScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.NoDataScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.ScreenStateHandler
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick
import com.d4rk.android.libs.apptoolkit.core.ui.components.navigation.TopAppBarScaffold
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper

@Composable
fun StartupScreen(activity : StartupActivity , viewModel : StartupViewModel) {
    val uiStateScreen : UiStateScreen<StartupUiData> by viewModel.screenState.collectAsState()

    TopAppBarScaffold(title = stringResource(R.string.welcome)) { paddingValues ->
        ScreenStateHandler(screenState = uiStateScreen , onLoading = {
            LoadingScreen()
        } , onEmpty = {
            NoDataScreen()
        } , onSuccess = { data ->
            StartupScreenContent(paddingValues = paddingValues , activity = activity , data = data)
        })
    }
}

@Composable
fun StartupScreenContent(paddingValues : PaddingValues , activity : StartupActivity , data : StartupUiData) {
    val context = LocalContext.current
    val fabEnabled = data.consentFormLoaded

    Box(
        modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .safeDrawingPadding()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                AsyncImage(model = R.drawable.il_startup , contentDescription = null)
                Icon(Icons.Outlined.Info , contentDescription = null)
            }
            item {
                Text(
                    text = stringResource(R.string.summary_browse_terms_of_service_and_privacy_policy) , modifier = Modifier.padding(vertical = 24.dp)
                )
                val annotatedString = buildAnnotatedString {
                    val start = length
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append(stringResource(R.string.learn_more))
                    }
                    addStringAnnotation(
                        tag = "URL" , annotation = "https://sites.google.com/view/d4rk7355608/more/apps/privacy-policy" , start = start , end = length
                    )
                }
                Text(
                    text = annotatedString , modifier = Modifier
                            .bounceClick()
                            .clickable {
                                annotatedString.getStringAnnotations("URL" , 0 , annotatedString.length).firstOrNull()?.let { annotation ->
                                    IntentsHelper.openUrl(context , annotation.item)
                                }
                            })
            }
        }

        ExtendedFloatingActionButton(
            modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .bounceClick() , containerColor = if (fabEnabled) {
            FloatingActionButtonDefaults.containerColor
        }
        else {
            Gray
        } , text = { Text(text = stringResource(id = R.string.agree)) } , onClick = {
            activity.navigateToNext()
        } , icon = {
            Icon(
                Icons.Outlined.CheckCircle , contentDescription = null
            )
        })
    }
}