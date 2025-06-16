package com.d4rk.android.libs.apptoolkit.app.issuereporter

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.actions.IssueReporterEvent
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.UiIssueReporterScreen
import com.d4rk.android.libs.apptoolkit.app.theme.style.AppTheme
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.navigation.LargeTopAppBarWithScaffold
import com.d4rk.android.libs.apptoolkit.core.ui.components.snackbar.DefaultSnackbarHandler
import org.koin.compose.viewmodel.koinViewModel

open class IssueReporterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.Companion.fillMaxSize() , color = MaterialTheme.colorScheme.background
                ) {
                    IssueReporterScreen(activity = this@IssueReporterActivity)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueReporterScreen(activity : IssueReporterActivity) {
    val viewModel : IssueReporterViewModel = koinViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiStateScreen : UiStateScreen<UiIssueReporterScreen> by viewModel.uiState.collectAsState()

    LargeTopAppBarWithScaffold(title = stringResource(id = R.string.bug_report) , onBackClicked = { activity.finish() } , snackbarHostState = snackbarHostState) { paddingValues : PaddingValues ->
        IssueReporterScreenContent(paddingValues = paddingValues , viewModel = viewModel , uiStateScreen = uiStateScreen)
        DefaultSnackbarHandler(screenState = uiStateScreen , snackbarHostState = snackbarHostState , getDismissEvent = { IssueReporterEvent.DismissSnackbar } , onEvent = { viewModel.onEvent(it) })
    }
}

@Composable
fun IssueReporterScreenContent(paddingValues : PaddingValues , viewModel : IssueReporterViewModel , uiStateScreen : UiStateScreen<UiIssueReporterScreen>) {

    val data = uiStateScreen.data ?: UiIssueReporterScreen()
    val context = LocalContext.current

    Column(
        modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp) , verticalArrangement = Arrangement.spacedBy(16.dp) , horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = data.title ,
            onValueChange = { viewModel.onEvent(IssueReporterEvent.UpdateTitle(it)) } ,
            label = { Text(stringResource(id = R.string.issue_title_label)) } ,
            modifier = Modifier.fillMaxWidth() ,
            leadingIcon = { Icon(Icons.Filled.Title , contentDescription = null) } ,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next) ,
            singleLine = true ,
        )

        OutlinedTextField(
            value = data.description ,
            onValueChange = { viewModel.onEvent(IssueReporterEvent.UpdateDescription(it)) } ,
            label = { Text(stringResource(id = R.string.issue_description_label)) } ,
            modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) ,
            leadingIcon = { Icon(Icons.Filled.Info , contentDescription = null) } ,
            minLines = 3 ,
        )

        OutlinedTextField(
            value = data.email ,
            onValueChange = { viewModel.onEvent(IssueReporterEvent.UpdateEmail(it)) } ,
            label = { Text(stringResource(id = R.string.issue_email_label)) } ,
            modifier = Modifier.fillMaxWidth() ,
            leadingIcon = { Icon(Icons.Filled.Email , contentDescription = null) } ,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done) ,
            singleLine = true ,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.onEvent(IssueReporterEvent.Send(context = context)) } ,
            modifier = Modifier.fillMaxWidth() ,
        ) {
            Text(text = stringResource(id = R.string.issue_send))
        }
    }
}