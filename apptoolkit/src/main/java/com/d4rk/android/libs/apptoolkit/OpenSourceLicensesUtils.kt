package com.d4rk.android.libs.apptoolkit

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object OpenSourceLicensesUtils {
    private suspend fun getChangelogMarkdown(packageName : String , context : Context) : String {
        return withContext(Dispatchers.IO) {
            val url =
                    URL("https://raw.githubusercontent.com/D4rK7355608/$packageName/refs/heads/master/CHANGELOG.md")
            (url.openConnection() as? HttpURLConnection)?.let { connection ->
                try {
                    connection.requestMethod = "GET"
                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                            return@withContext reader.readText()
                        }
                    }
                    else {
                        context.getString(R.string.error_loading_changelog)
                    }
                } finally {
                    connection.disconnect()
                }
            } ?: context.getString(R.string.error_loading_changelog)
        }
    }

    private suspend fun getEulaMarkdown(packageName : String , context : Context) : String {
        return withContext(Dispatchers.IO) {
            val url =
                    URL("https://raw.githubusercontent.com/D4rK7355608/$packageName/refs/heads/master/EULA.md")
            (url.openConnection() as? HttpURLConnection)?.let { connection ->
                try {
                    connection.requestMethod = "GET"
                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                            return@withContext reader.readText()
                        }
                    }
                    else {
                        context.getString(R.string.error_loading_eula)
                    }
                } finally {
                    connection.disconnect()
                }
            } ?: context.getString(R.string.error_loading_eula)
        }
    }

    private fun extractLatestVersionChangelog(
        markdown : String ,
        currentVersionName : String
    ) : String {
        val regex =
                Regex(pattern = "(# Version\\s+$currentVersionName:\\s*[\\s\\S]*?)(?=# Version|$)")
        val match = regex.find(markdown)
        return match?.groups?.get(1)?.value?.trim()
            ?: "No changelog available for version $currentVersionName"
    }

    private fun convertMarkdownToHtml(markdown : String) : String {
        val parser = Parser.builder().build()
        val renderer = HtmlRenderer.builder().build()
        val document = parser.parse(markdown)
        return renderer.render(document)
    }

    suspend fun loadHtmlData(
        packageName : String ,
        currentVersionName : String ,
        context : Context
    ) : Pair<String? , String?> {
        val changelogMarkdown = getChangelogMarkdown(packageName = packageName , context = context)
        val extractedChangelog = extractLatestVersionChangelog(
            markdown = changelogMarkdown ,
            currentVersionName = currentVersionName
        )
        val changelogHtml = convertMarkdownToHtml(extractedChangelog)

        val eulaMarkdown = getEulaMarkdown(packageName = packageName , context = context)
        val eulaHtml = convertMarkdownToHtml(eulaMarkdown)

        return changelogHtml to eulaHtml
    }
}

@Composable
fun rememberHtmlData(
    packageName : String ,
    currentVersionName : String ,
    context : Context
) : State<Pair<String? , String?>> {
    val htmlDataState = remember { mutableStateOf<Pair<String? , String?>>(value = null to null) }

    LaunchedEffect(Unit) {
        htmlDataState.value = OpenSourceLicensesUtils.loadHtmlData(
            packageName = packageName ,
            currentVersionName = currentVersionName ,
            context = context
        )
    }

    return htmlDataState
}