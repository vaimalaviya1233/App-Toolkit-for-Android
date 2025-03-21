package com.d4rk.android.libs.apptoolkit.app.settings.settings.ui

import android.app.Activity
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ContactSupport
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.help.ui.HelpActivity
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsConfig
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.LoadingScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.NoDataScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.ScreenStateHandler
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick
import com.d4rk.android.libs.apptoolkit.core.ui.components.navigation.LargeTopAppBarWithScaffold
import com.d4rk.android.libs.apptoolkit.core.ui.components.preferences.SettingsPreferenceItem
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.ButtonIconSpacer
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.ExtraTinyVerticalSpacer
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.LargeVerticalSpacer
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ScreenHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel : SettingsViewModel) {
    val screenState : UiStateScreen<SettingsConfig> by viewModel.screenState.collectAsState()
    val context = LocalContext.current

    println(message = "screenState for settings is: $screenState")

    LargeTopAppBarWithScaffold(
        title = stringResource(id = R.string.settings) , onBackClicked = { (context as Activity).finish() }) { paddingValues ->
        ScreenStateHandler(screenState = screenState , onLoading = { LoadingScreen() } , onEmpty = { NoDataScreen(icon = Icons.Outlined.Settings , showRetry = true , onRetry = { viewModel.loadSettings(context) }) } , onSuccess = { settingsConfig ->
            SettingsContent(paddingValues , settingsConfig)
        })
    }
}

@Composable
fun SettingsContent(paddingValues : PaddingValues , settingsConfig : SettingsConfig) {
    val isTabletOrLandscape = ScreenHelper.isLandscapeOrTablet(LocalContext.current)

    if (isTabletOrLandscape) {
        TabletSettingsScreen(paddingValues , settingsConfig)
    }
    else {
        PhoneSettingsScreen(paddingValues , settingsConfig)
    }
}

@Composable
fun PhoneSettingsScreen(paddingValues : PaddingValues , settingsConfig : SettingsConfig) {
    SettingsList(
        paddingValues = paddingValues , settingsConfig = settingsConfig
    )
}

@Composable
fun TabletSettingsScreen(paddingValues : PaddingValues , settingsConfig : SettingsConfig) {
    var selectedPreference by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
        ) {
            SettingsList(paddingValues , settingsConfig) { selectedPreference = it }
        }

        Box(
            modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
        ) {
            AnimatedContent(targetState = selectedPreference) { preference ->
                preference?.let {
                    SettingsDetail(preference = it , paddingValues = paddingValues , context = context)
                } ?: SettingsDetailPlaceholder(paddingValues)
            }
        }
    }
}

@Composable
fun SettingsDetailPlaceholder(paddingValues : PaddingValues) {
    val context : Context = LocalContext.current

    LazyColumn(
        contentPadding = paddingValues , modifier = Modifier.fillMaxHeight()
    ) {
        item {
            Card(
                modifier = Modifier
                        .padding(top = SizeConstants.LargeSize , end = SizeConstants.LargeSize)
                        .fillMaxSize()
                        .wrapContentHeight() ,
                shape = RoundedCornerShape(size = 28.dp) ,
            ) {
                Column(
                    modifier = Modifier.padding(all = 24.dp) , horizontalAlignment = Alignment.CenterHorizontally , verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = R.drawable.il_settings , contentDescription = null , modifier = Modifier
                                .size(size = 258.dp)
                                .fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(height = SizeConstants.LargeSize))
                    Text(
                        modifier = Modifier.fillMaxWidth() , text = stringResource(id = R.string.app_name) , style = MaterialTheme.typography.titleMedium , color = MaterialTheme.colorScheme.onSurface , textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(height = 8.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth() , text = stringResource(id = R.string.settings_placeholder_description) , style = MaterialTheme.typography.bodyMedium , color = MaterialTheme.colorScheme.onSurfaceVariant , textAlign = TextAlign.Center
                    )
                }

                OutlinedButton(modifier = Modifier
                        .padding(all = 24.dp)
                        .align(Alignment.Start)
                        .bounceClick() , onClick = {
                    IntentsHelper.openActivity(
                        context = context , activityClass = HelpActivity::class.java
                    )
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ContactSupport , contentDescription = null
                    )
                    ButtonIconSpacer()
                    Text(text = stringResource(id = R.string.get_help))
                }
            }
        }
    }
}

@Composable
fun SettingsDetail(preference : String , context : Context , paddingValues : PaddingValues) {

}

@Composable
fun SettingsList(paddingValues : PaddingValues , settingsConfig : SettingsConfig , onPreferenceClick : (String) -> Unit = {}) {
    LazyColumn(contentPadding = paddingValues , modifier = Modifier.fillMaxHeight()) {
        settingsConfig.categories.forEach { category ->
            item {
                LargeVerticalSpacer()
                Column(
                    modifier = Modifier
                            .padding(start = SizeConstants.LargeSize , end = SizeConstants.LargeSize)
                            .clip(shape = RoundedCornerShape(size = SizeConstants.ExtraLargeSize))
                ) {
                    category.preferences.forEach { preference ->
                        SettingsPreferenceItem(icon = preference.icon , title = preference.title , summary = preference.summary , onClick = { preference.action.invoke() })
                        ExtraTinyVerticalSpacer()
                    }
                }
            }
        }
    }
}