package com.d4rk.android.libs.apptoolkit.app.main.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import dev.jeziellago.compose.markdowntext.MarkdownText
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.core.ui.components.dialogs.BasicAlertDialog
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ChangelogDialog(
    changelogUrl: String,
    buildInfoProvider: BuildInfoProvider,
    dataStore: CommonDataStore,
    lastSeenVersion: String,
    cachedChangelog: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val changelogText: MutableState<String?> = remember {
        mutableStateOf(
            if (lastSeenVersion == buildInfoProvider.appVersion) cachedChangelog else null
        )
    }
    val isError = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun loadChangelog() {
        val client = HttpClient(Android)
        try {
            val content: String = client.get(changelogUrl).body()
            val section = extractChangesForVersion(content, buildInfoProvider.appVersion)
            changelogText.value = section.ifBlank { context.getString(R.string.no_new_updates_message) }
            withContext(Dispatchers.IO) {
                dataStore.saveLastSeenVersion(buildInfoProvider.appVersion)
                dataStore.saveCachedChangelog(changelogText.value!!)
            }
        } catch (_: Exception) {
            isError.value = true
        } finally {
            client.close()
        }
    }

    LaunchedEffect(Unit) {
        if (lastSeenVersion != buildInfoProvider.appVersion) {
            loadChangelog()
        } else {
            changelogText.value = if (cachedChangelog.isNotBlank()) {
                cachedChangelog
            } else {
                context.getString(R.string.no_new_updates_message)
            }
        }
    }

    BasicAlertDialog(
        onDismiss = onDismiss,
        onConfirm = {
            if (isError.value) {
                isError.value = false
                changelogText.value = null
                scope.launch { loadChangelog() }
            } else {
                onDismiss()
            }
        },
        onCancel = onDismiss,
        confirmButtonText = if (isError.value) stringResource(id = R.string.try_again) else null,
        title = stringResource(id = R.string.changelog_title),
        content = {
            when {
                changelogText.value == null && !isError.value -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = stringResource(id = R.string.loading_changelog_message))
                }
                isError.value -> Column(verticalArrangement = Arrangement.Center) {
                    Text(text = stringResource(id = R.string.error_loading_changelog_message))
                }
                else -> MarkdownText(
                    modifier = Modifier.fillMaxWidth(),
                    markdown = changelogText.value!!
                )
            }
        }
    )
}

private fun extractChangesForVersion(markdown: String, version: String): String {
    val lines = markdown.lines()
    val startIndex = lines.indexOfFirst { line -> line.contains(version) }
    if (startIndex == -1) return ""
    val sb = StringBuilder()
    sb.appendLine(lines[startIndex])
    for (i in startIndex + 1 until lines.size) {
        val line = lines[i]
        if (line.startsWith("#")) break
        sb.appendLine(line)
    }
    return sb.toString().trim()
}
