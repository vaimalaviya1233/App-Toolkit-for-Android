package com.heinrichreimersoftware.androidissuereporter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import com.d4rk.android.libs.apptoolkit.R

class IssueReporterActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras

        setContent {
            IssueReporterScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueReporterScreen() {
    val title = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8f)
        ) {
            TextField(value = title.value, onValueChange = { title.value = it }, label = { Text(stringResource(id = R.string.issue_title_label)) })
            TextField(value = description.value, onValueChange = { description.value = it }, label = { Text(stringResource(id = R.string.issue_description_label)) })
            TextField(value = username.value, onValueChange = { username.value = it }, label = { Text(stringResource(id = R.string.issue_username_label)) })
            TextField(value = password.value, onValueChange = { password.value = it }, label = { Text(stringResource(id = R.string.issue_password_label)) }, visualTransformation = PasswordVisualTransformation())
            TextField(value = email.value, onValueChange = { email.value = it }, label = { Text(stringResource(id = R.string.issue_email_label)) })
            Button(onClick = { /* TODO send */ }) {
                Text(text = stringResource(id = R.string.issue_send))
            }
        }
    }
}

@Preview
@Composable
private fun PreviewIssueReporter() {
    IssueReporterScreen()
}
