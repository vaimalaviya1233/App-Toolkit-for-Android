package com.d4rk.android.libs.apptoolkit.app.display.components.dialogs

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.ui.components.dialogs.BasicAlertDialog
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.sections.InfoMessageSection
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun SelectLanguageAlertDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val context: Context = LocalContext.current
    val dataStore: CommonDataStore = CommonDataStore.getInstance(context)
    val selectedLanguage = remember { mutableStateOf("") }
    val languageEntries: List<String> = stringArrayResource(id = R.array.preference_language_entries).toList()
    val languageValues: List<String> = stringArrayResource(id = R.array.preference_language_values).toList()

    BasicAlertDialog(
        onDismiss = onDismiss,
        onConfirm = {
            onLanguageSelected(selectedLanguage.value)
            onDismiss()
        },
        onCancel = onDismiss,
        icon = Icons.Outlined.Language,
        title = stringResource(id = R.string.select_language_title),
        content = {
            SelectLanguageAlertDialogContent(
                selectedLanguage = selectedLanguage,
                dataStore = dataStore,
                languageEntries = languageEntries,
                languageValues = languageValues
            )
        }
    )
}


@Composable
fun SelectLanguageAlertDialogContent(
    selectedLanguage : MutableState<String> , dataStore : CommonDataStore , languageEntries : List<String> , languageValues : List<String>
) {
    LaunchedEffect(key1 = Unit) {
        selectedLanguage.value = dataStore.getLanguage().firstOrNull() ?: ""
    }

    Column {
        Text(text = stringResource(id = R.string.dialog_language_subtitle))
        Box(
            modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f)
        ) {
            LazyColumn {
                items(languageEntries.size) { index ->
                    Row(
                        Modifier.fillMaxWidth() , verticalAlignment = Alignment.CenterVertically , horizontalArrangement = Arrangement.Start
                    ) {
                        RadioButton(
                            selected = selectedLanguage.value == languageValues[index] , onClick = {
                                selectedLanguage.value = languageValues[index]
                            })
                        Text(
                            modifier = Modifier.padding(start = 8.dp) , text = languageEntries[index] , style = MaterialTheme.typography.bodyMedium.merge()
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(height = 24.dp))
        InfoMessageSection(message = stringResource(id = com.d4rk.android.libs.apptoolkit.R.string.dialog_info_language))
    }

    LaunchedEffect(key1 = selectedLanguage.value) {
        dataStore.saveLanguage(language = selectedLanguage.value)
    }
}