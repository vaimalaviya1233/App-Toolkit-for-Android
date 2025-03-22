package com.d4rk.android.libs.apptoolkit.app.startup.ui

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick
import com.d4rk.android.libs.apptoolkit.core.ui.components.navigation.TopAppBarScaffold
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.PermissionsHelper

@Composable
fun StartupScreen(activity : StartupActivity) {
    val consentShown by activity.consentShown.collectAsState(initial = false)
    val context = LocalContext.current
    val fabEnabled : MutableState<Boolean> = remember { mutableStateOf(value = false) }

    LaunchedEffect(key1 = consentShown) {
        activity.consentShown.collect { shown ->
            fabEnabled.value = shown
        }
    }

    TopAppBarScaffold(title = stringResource(R.string.welcome)) { padding ->
        Box(
            modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
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
                        .bounceClick() , containerColor = if (fabEnabled.value) {
                FloatingActionButtonDefaults.containerColor
            }
            else {
                Gray
            } , text = { Text(text = stringResource(id = com.d4rk.android.libs.apptoolkit.R.string.agree)) } , onClick = {
                context.startActivity(activity.provider.getNextIntent(context))
            } , icon = {
                Icon(
                    Icons.Outlined.CheckCircle , contentDescription = null
                )
            })
        }
    }
}
