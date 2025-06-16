package com.d4rk.android.libs.apptoolkit.app.issuereporter.ui

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.actions.IssueReporterEvent
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.github.GithubTarget
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.ui.UiIssueReporterScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.navigation.LargeTopAppBarWithScaffold
import com.d4rk.android.libs.apptoolkit.core.ui.components.snackbar.DefaultSnackbarHandler
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueReporterScreen(activity : Activity) {
    val viewModel : IssueReporterViewModel = koinViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiStateScreen : UiStateScreen<UiIssueReporterScreen> by viewModel.uiState.collectAsState()
    val target : GithubTarget = koinInject()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    LargeTopAppBarWithScaffold(
        title = stringResource(id = R.string.bug_report) ,
        onBackClicked = { activity.finish() } ,
        snackbarHostState = snackbarHostState ,
        scrollBehavior = scrollBehavior ,
        floatingActionButton = {
            // TODO: Here will be placed the sent button as fab
            // TODO: The redirect button as well will be plaed on top of the sent button fab as small fab like this:

            /*
            // TODO: it will be needed a a column like this one (modified of course) to make that
@Composable
fun FloatingActionButtonsColumn(mainScreenState : MainScreenState) {
    Column(horizontalAlignment = Alignment.End) {
        SmallFloatingActionButton(modifier = Modifier.padding(bottom = SizeConstants.MediumSize) , isVisible = mainScreenState.isFabVisible.value , isExtended = mainScreenState.isFabExtended.value , icon = Icons.Outlined.ImportExport , onClick = {
            mainScreenState.navController.currentBackStackEntry?.savedStateHandle?.set("toggleImportDialog" , true)
        })

        AnimatedExtendedFloatingActionButton(visible = mainScreenState.isFabVisible.value , onClick = {
            mainScreenState.navController.currentBackStackEntry?.savedStateHandle?.set("openNewCartDialog" , true)
        } , text = { Text(text = stringResource(id = R.string.add_new_cart)) } , icon = { Icon(imageVector = Icons.Outlined.AddShoppingCart , contentDescription = null) } , modifier = Modifier.bounceClick() , expanded = mainScreenState.isFabExtended.value)
    }
}

            */

        }
    ) { paddingValues : PaddingValues ->
        IssueReporterScreenContent(paddingValues = paddingValues , viewModel = viewModel , uiStateScreen = uiStateScreen , target = target)
        DefaultSnackbarHandler(screenState = uiStateScreen , snackbarHostState = snackbarHostState , getDismissEvent = { IssueReporterEvent.DismissSnackbar } , onEvent = { viewModel.onEvent(it) })
    }
}

@Composable
fun IssueReporterScreenContent(
    paddingValues : PaddingValues , viewModel : IssueReporterViewModel , uiStateScreen : UiStateScreen<UiIssueReporterScreen> , target : GithubTarget
) {
    val data = uiStateScreen.data ?: UiIssueReporterScreen()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp , vertical = 16.dp) ,
        verticalArrangement = Arrangement.spacedBy(24.dp) ,
    ) {

        Text(text = stringResource(id = R.string.issue_section_label) , style = MaterialTheme.typography.titleMedium)

        Surface(
            tonalElevation = 1.dp , shape = MaterialTheme.shapes.medium , modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp) , verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = data.title ,
                                  onValueChange = { viewModel.onEvent(IssueReporterEvent.UpdateTitle(it)) } ,
                                  label = { Text(stringResource(id = R.string.issue_title_label)) } ,
                                  leadingIcon = { Icon(Icons.Outlined.Title , contentDescription = null) } ,
                                  modifier = Modifier.fillMaxWidth() ,
                                  singleLine = true ,
                                  keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = data.description ,
                                  onValueChange = { viewModel.onEvent(IssueReporterEvent.UpdateDescription(it)) } ,
                                  label = { Text(stringResource(id = R.string.issue_description_label)) } ,
                                  leadingIcon = { Icon(Icons.Outlined.Info , contentDescription = null) } ,
                                  modifier = Modifier.fillMaxWidth() ,
                                  minLines = 4
                )

                OutlinedTextField(
                    value = data.email ,
                                  onValueChange = { viewModel.onEvent(IssueReporterEvent.UpdateEmail(it)) } ,
                                  label = { Text(stringResource(id = R.string.issue_email_label)) } ,
                                  placeholder = { Text(stringResource(id = R.string.optional_placeholder)) } ,
                                  leadingIcon = { Icon(Icons.Outlined.Email , contentDescription = null) } ,
                                  modifier = Modifier.fillMaxWidth() ,
                                  singleLine = true ,
                                  keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            }
        }

        Text(text = stringResource(id = R.string.login_section_label) , style = MaterialTheme.typography.titleMedium)

        Surface(
            tonalElevation = 1.dp , shape = MaterialTheme.shapes.medium , modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp) , verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RadioOption(
                    selected = ! data.anonymous , text = stringResource(id = R.string.use_github_account) , onClick = { viewModel.onEvent(IssueReporterEvent.SetAnonymous(false)) }) // FIXME: Unresolved reference 'SetAnonymous'.
                RadioOption(
                    selected = data.anonymous , text = stringResource(id = R.string.send_anonymously) , onClick = { viewModel.onEvent(IssueReporterEvent.SetAnonymous(true)) }) // FIXME: Unresolved reference 'anonymous'.
            }
        }

        Button(
            onClick = { viewModel.onEvent(IssueReporterEvent.Send(context)) } , modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
        ) {
            Icon(Icons.Outlined.BugReport , contentDescription = null , modifier = Modifier.padding(end = 8.dp))
            Text(stringResource(id = R.string.issue_send))
        }

        FilledTonalButton(
            onClick = {
                IntentsHelper.openUrl(context , "https://github.com/${target.username}/${target.repository}/issues")
            } , modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
        ) {
            Icon(Icons.Outlined.Link , contentDescription = null , modifier = Modifier.padding(end = 8.dp))
            Text(stringResource(id = R.string.view_on_github))
        }
    }
}

@Composable
private fun RadioOption(selected : Boolean , text : String , onClick : () -> Unit) {
    androidx.compose.material3.RadioButton(
        selected = selected , onClick = onClick , modifier = Modifier.padding(end = 8.dp)
    )
    Text(text = text , style = MaterialTheme.typography.bodyLarge)
}