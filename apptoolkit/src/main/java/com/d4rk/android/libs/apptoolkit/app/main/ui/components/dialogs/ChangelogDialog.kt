package com.d4rk.android.libs.apptoolkit.app.main.ui.components.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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

    LaunchedEffect(Unit) {
        if (lastSeenVersion != buildInfoProvider.appVersion) {
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
        onConfirm = onDismiss,
        title = stringResource(id = R.string.changelog_title),
        content = {
            when {
                changelogText.value == null && !isError.value -> Text(text = stringResource(id = R.string.loading_changelog_message))
                isError.value -> Text(text = stringResource(id = R.string.error_loading_changelog_message))
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
