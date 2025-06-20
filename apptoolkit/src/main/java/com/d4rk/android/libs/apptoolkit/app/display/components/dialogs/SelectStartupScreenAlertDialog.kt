package com.d4rk.android.libs.apptoolkit.app.display.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.ui.components.dialogs.BasicAlertDialog
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.sections.InfoMessageSection
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.MediumVerticalSpacer
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import org.koin.compose.koinInject
import org.koin.core.qualifier.named

@Composable
fun SelectStartupScreenAlertDialog(onDismiss: () -> Unit, onStartupSelected: (String) -> Unit) {
    val context = LocalContext.current
    val dataStore = CommonDataStore.getInstance(context)
    val selectedPage = remember { mutableStateOf("") }
    val entries: List<String> = koinInject(qualifier = named("startup_entries"))
    val values: List<String> = koinInject(qualifier = named("startup_values"))
    val startupRoute by dataStore.getStartupPage().collectAsState(initial = values.first())

    LaunchedEffect(startupRoute) {
        selectedPage.value = startupRoute
    }

    BasicAlertDialog(
        onDismiss = onDismiss,
        onConfirm = {
            onStartupSelected(selectedPage.value)
            onDismiss()
        },
        icon = Icons.Outlined.Home,
        title = stringResource(id = R.string.startup_page),
        content = {
            SelectStartupScreenAlertDialogContent(selectedPage, dataStore, entries, values, startupRoute)
        }
    )
}

@Composable
fun SelectStartupScreenAlertDialogContent(
    selectedPage: MutableState<String>,
    dataStore: CommonDataStore,
    startupEntries: List<String>,
    startupValues: List<String>,
    startupRoute: String
) {

    Column {
        Text(text = stringResource(id = R.string.dialog_startup_subtitle))
        Box(modifier = Modifier.fillMaxWidth()) {
            LazyColumn {
                items(startupEntries.size) { index ->
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        RadioButton(
                            selected = selectedPage.value == startupValues[index],
                            onClick = { selectedPage.value = startupValues[index] }
                        )
                        Text(
                            modifier = Modifier.padding(start = SizeConstants.SmallSize),
                            text = startupEntries[index],
                            style = MaterialTheme.typography.bodyMedium.merge()
                        )
                    }
                }
            }
        }
        MediumVerticalSpacer()
        InfoMessageSection(message = stringResource(id = R.string.dialog_info_startup))
    }

    LaunchedEffect(selectedPage.value) {
        if (selectedPage.value.isNotBlank() && selectedPage.value != startupRoute) {
            dataStore.saveStartupPage(selectedPage.value)
        }
    }
}
