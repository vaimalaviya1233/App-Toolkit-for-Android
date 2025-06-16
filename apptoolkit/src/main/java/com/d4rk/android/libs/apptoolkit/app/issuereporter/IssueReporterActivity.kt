package com.d4rk.android.libs.apptoolkit.app.issuereporter

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.actions.IssueReporterEvent
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.UiIssueReporterScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.snackbar.DefaultSnackbarHandler
import org.koin.androidx.viewmodel.ext.android.viewModel

open class IssueReporterActivity : AppCompatActivity() {
    private val viewModel: IssueReporterViewModel by viewModel()
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IssueReporterScreen(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueReporterScreen(viewModel: IssueReporterViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val screenState: UiStateScreen<UiIssueReporterScreen> by viewModel.uiState.collectAsState()
    val data = screenState.data ?: UiIssueReporterScreen()
    Scaffold(snackbarHost = { DefaultSnackbarHandler(screenState = screenState, snackbarHostState = snackbarHostState, getDismissEvent = { IssueReporterEvent.DismissSnackbar }, onEvent = { viewModel.onEvent(it) }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TextField(value = data.title, onValueChange = { viewModel.onEvent(IssueReporterEvent.UpdateTitle(it)) }, label = { Text(stringResource(id = R.string.issue_title_label)) })
            TextField(value = data.description, onValueChange = { viewModel.onEvent(IssueReporterEvent.UpdateDescription(it)) }, label = { Text(stringResource(id = R.string.issue_description_label)) })
            TextField(value = data.email, onValueChange = { viewModel.onEvent(IssueReporterEvent.UpdateEmail(it)) }, label = { Text(stringResource(id = R.string.issue_email_label)) })
            Button(onClick = { viewModel.onEvent(IssueReporterEvent.Send(context = this@IssueReporterActivity)) }) {
                Text(text = stringResource(id = R.string.issue_send))
            }
        }
    }
}