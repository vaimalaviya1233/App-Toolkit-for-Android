package com.d4rk.android.libs.apptoolkit.ui.screens.help.ui

import android.app.Activity
import android.content.Context
import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Shop
import androidx.compose.material.icons.outlined.Support
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import com.d4rk.android.libs.apptoolkit.ui.components.buttons.AnimatedButtonDirection
import com.d4rk.android.libs.apptoolkit.ui.components.buttons.AnimatedExtendedFloatingActionButton
import com.d4rk.android.libs.apptoolkit.ui.components.dialogs.VersionInfoAlertDialog
import com.d4rk.android.libs.apptoolkit.ui.components.layouts.ScreenStateHandler
import com.d4rk.android.libs.apptoolkit.ui.components.modifiers.bounceClick
import com.d4rk.android.libs.apptoolkit.ui.components.navigation.LargeTopAppBarWithScaffold
import com.d4rk.android.libs.apptoolkit.ui.components.network.rememberHtmlData
import com.d4rk.android.libs.apptoolkit.ui.components.spacers.LargeHorizontalSpacer
import com.d4rk.android.libs.apptoolkit.ui.components.spacers.MediumVerticalSpacer
import com.d4rk.android.libs.apptoolkit.ui.components.spacers.SmallVerticalSpacer
import com.d4rk.android.libs.apptoolkit.ui.screens.help.domain.actions.HelpAction
import com.d4rk.android.libs.apptoolkit.ui.screens.help.domain.model.ui.HelpScreenConfig
import com.d4rk.android.libs.apptoolkit.ui.screens.help.domain.model.ui.UiHelpQuestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    activity: Activity,
    viewModel: HelpViewModel,
    config: HelpScreenConfig
) {
    val screenState by viewModel.screenState.collectAsState()
    val context = LocalContext.current
    val view = LocalView.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val htmlData: State<Pair<String?, String?>> = rememberHtmlData(
        context = context,
        currentVersionName = config.versionName,
        packageName = activity.packageName
    )
    val changelogHtmlString: String? = htmlData.value.first
    val eulaHtmlString: String? = htmlData.value.second

    LargeTopAppBarWithScaffold(
        title = stringResource(id = R.string.help),
        onBackClicked = { activity.finish() },
        actions = {
            HelpScreenMenuActions(
                context = context,
                activity = activity,
                showDialog = remember { mutableStateOf(false) },
                eulaHtmlString = eulaHtmlString,
                changelogHtmlString = changelogHtmlString,
                view = view,
                config = config
            )
        },
        scrollBehavior = scrollBehavior,
        floatingActionButton = {
            AnimatedExtendedFloatingActionButton(
                visible = screenState.data?.reviewInfo != null,
                onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    screenState.data?.reviewInfo?.let { reviewInfo ->
                        viewModel.sendEvent(HelpAction.LaunchReviewFlow(activity, reviewInfo))
                    }
                },
                text = { Text(text = stringResource(id = R.string.feedback)) },
                icon = { Icon(Icons.Outlined.RateReview, contentDescription = null) },
                modifier = Modifier.bounceClick()
            )
        }
    ) { paddingValues ->
        ScreenStateHandler(
            screenState = screenState,
            onLoading = { /* Your loading UI */ },
            onEmpty = { /* UI for no data */ },
            onSuccess = { helpData ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding(),
                        start = 16.dp,
                        end = 16.dp
                    )
                ) {
                    item {
                        Text(text = stringResource(id = R.string.popular_help_resources))
                        MediumVerticalSpacer()
                        Card(modifier = Modifier.fillMaxWidth()) {
                            FAQComposable(questions = helpData.questions)
                        }
                        MediumVerticalSpacer()
                        ContactUsCard(onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            IntentsHelper.sendEmailToDeveloper(
                                context = activity,
                                applicationNameRes = config.appName
                            )
                        })
                        Spacer(modifier = Modifier.height(96.dp))
                    }
                }
            },
            onError = { /* Your error UI, e.g., snackbar */ }
        )
    }
}

@Composable
fun FAQComposable(questions : List<UiHelpQuestion>) {
    val expandedStates : SnapshotStateMap<Int , Boolean> = remember { mutableStateMapOf() }

    Column {
        questions.forEachIndexed { index , question ->
            val isExpanded = expandedStates[index] ?: false
            QuestionComposable(title = question.question , summary = question.answer , isExpanded = isExpanded , onToggleExpand = {
                expandedStates[index] = ! isExpanded
            })
        }
    }
}

@Composable
fun QuestionComposable(
    title : String , summary : String , isExpanded : Boolean , onToggleExpand : () -> Unit
) {
    Card(modifier = Modifier
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .clickable { onToggleExpand() }
            .padding(all = 16.dp)
            .animateContentSize()
            .fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically , modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.QuestionAnswer , contentDescription = null , tint = MaterialTheme.colorScheme.primary , modifier = Modifier
                            .size(size = 48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer , shape = CircleShape
                            )
                            .padding(all = 8.dp)
                )
                LargeHorizontalSpacer()

                Text(
                    text = title , style = MaterialTheme.typography.titleMedium , modifier = Modifier.weight(weight = 1f)
                )

                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore , contentDescription = null , tint = MaterialTheme.colorScheme.primary , modifier = Modifier.size(size = 24.dp)
                )
            }
            if (isExpanded) {
                SmallVerticalSpacer()
                Text(
                    text = summary ,
                    style = MaterialTheme.typography.bodyMedium ,
                )
            }
        }
    }
}

@Composable
fun ContactUsCard(onClick : () -> Unit) {
    Card(modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .clickable {
                onClick()
            }) {
        Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp) , verticalAlignment = Alignment.CenterVertically , horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Outlined.Support , contentDescription = null , modifier = Modifier.padding(end = 16.dp))
            Column(
                modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
            ) {
                Text(text = stringResource(id = R.string.contact_us))
                Text(text = stringResource(id = R.string.contact_us_description))
            }
        }
    }
}

@Composable
fun HelpScreenMenuActions(
    context: Context ,
    activity: Activity ,
    showDialog: MutableState<Boolean> ,
    eulaHtmlString: String? ,
    changelogHtmlString: String? ,
    view: View,
    config: HelpScreenConfig
) {
    var showMenu: Boolean by remember { mutableStateOf(value = false) }

    AnimatedButtonDirection(
        fromRight = true,
        contentDescription = null,
        icon = Icons.Default.MoreVert,
        onClick = {  showMenu = true }
    )

    DropdownMenu(expanded = showMenu, onDismissRequest = {
        showMenu = false
    }) {
        DropdownMenuItem(
            modifier = Modifier.bounceClick(),
            text = { Text(text = stringResource(id = R.string.view_in_google_play_store)) },
            leadingIcon = { Icon(imageVector = Icons.Outlined.Shop, contentDescription = null) },
            onClick = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                IntentsHelper.openUrl(
                    context = context , url = "https://play.google.com/store/apps/details?id=${activity.packageName}"
                )
            }
        )
        DropdownMenuItem(
            modifier = Modifier.bounceClick(),
            text = { Text(text = stringResource(id = R.string.version_info)) },
            leadingIcon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = null) },
            onClick = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                showDialog.value = true
            }
        )
        DropdownMenuItem(
            modifier = Modifier.bounceClick(),
            text = { Text(text = stringResource(id = R.string.beta_program)) },
            leadingIcon = { Icon(imageVector = Icons.Outlined.Science, contentDescription = null) },
            onClick = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                IntentsHelper.openUrl(
                    context = context, url = "https://play.google.com/apps/testing/${activity.packageName}"
                )
            }
        )
        DropdownMenuItem(
            modifier = Modifier.bounceClick(),
            text = { Text(text = stringResource(id = R.string.terms_of_service)) },
            leadingIcon = { Icon(imageVector = Icons.Outlined.Description, contentDescription = null) },
            onClick = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                IntentsHelper.openUrl(context = context, url = "https://sites.google.com/view/d4rk7355608/more/apps/terms-of-service")
            }
        )
        DropdownMenuItem(
            modifier = Modifier.bounceClick(),
            text = { Text(text = stringResource(id = R.string.privacy_policy)) },
            leadingIcon = { Icon(imageVector = Icons.Outlined.PrivacyTip, contentDescription = null) },
            onClick = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                IntentsHelper.openUrl(context = context, url = "https://sites.google.com/view/d4rk7355608/more/apps/privacy-policy")
            }
        )
        DropdownMenuItem(
            modifier = Modifier.bounceClick(),
            text = { Text(text = stringResource(id = R.string.oss_license_title)) },
            leadingIcon = { Icon(Icons.Outlined.Balance, contentDescription = null) },
            onClick = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                IntentsHelper.openLicensesScreen(
                    context = context,
                    eulaHtmlString = eulaHtmlString,
                    changelogHtmlString = changelogHtmlString,
                    appName = context.getString(config.appName),
                    appVersion =config.versionName,
                    appVersionCode = config.versionCode,
                    appShortDescription = R.string.app_short_description
                )
            }
        )
    }

    if (showDialog.value) {
        VersionInfoAlertDialog(
            onDismiss = { showDialog.value = false },
            copyrightString = config.copyRightString,
            appName = config.appFullName,
            versionName = config.versionName,
            versionString = R.string.version
        )
    }
}