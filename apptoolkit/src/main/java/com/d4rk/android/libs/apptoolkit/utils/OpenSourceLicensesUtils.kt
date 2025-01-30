package com.d4rk.android.libs.apptoolkit.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import com.d4rk.android.libs.apptoolkit.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Utility object for fetching and processing open source licenses and related information
 * from GitHub repositories.
 *
 * This object provides functions to retrieve Markdown files such as changelogs and EULAs
 * from GitHub, extract specific version changelogs, convert Markdown to HTML,
 * and bundle this information for display within an application.
 *
 * It relies on coroutines for performing network operations and uses the CommonMark library
 * for Markdown parsing and rendering.
 */
object OpenSourceLicensesUtils {

    private const val GITHUB_BASE_URL = "https://raw.githubusercontent.com/D4rK7355608"

    /**
     * Fetches a Markdown file from a GitHub repository.
     *
     * This function retrieves the content of a specified Markdown file from the `master` branch
     * of a GitHub repository. It performs the network operation on a background thread using
     * `Dispatchers.IO`.
     *
     * @param packageName The name of the GitHub repository (e.g., "owner/repo-name").
     * @param fileName The name of the Markdown file to fetch (e.g., "README.md").
     * @param errorResId The string resource ID of an error message to return if the fetch fails.
     * @param context The Android Context used for accessing resources.
     * @return The content of the Markdown file as a String, or an error message String if the fetch fails.
     */
    private suspend fun fetchMarkdownFile(
        packageName : String , fileName : String , @StringRes errorResId : Int , context : Context
    ) : String {
        return withContext(Dispatchers.IO) {
            val fileUrl = "$GITHUB_BASE_URL/$packageName/refs/heads/master/$fileName"
            val url = URL(fileUrl)
            (url.openConnection() as? HttpURLConnection)?.let { connection ->
                try {
                    connection.requestMethod = "GET"
                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                            return@withContext reader.readText()
                        }
                    }
                    else {
                        context.getString(errorResId)
                    }
                } finally {
                    connection.disconnect()
                }
            } ?: context.getString(errorResId)
        }
    }

    /**
     * Retrieves the changelog Markdown content from the GitHub repository for the specified package.
     *
     * This function fetches the `CHANGELOG.md` file from the repository associated with the given package name
     * and returns its content as a String. It handles errors by providing a user-friendly message.
     *
     * @param packageName The name of the package (usually matching the repository name) for which to retrieve the changelog.
     * @param context The Android Context used to access resources, particularly for error messages.
     * @return The Markdown content of the changelog file as a String, or an empty string if the file cannot be fetched.
     */
    private suspend fun getChangelogMarkdown(
        packageName : String , context : Context
    ) : String {
        return fetchMarkdownFile(
            packageName = packageName ,
            fileName = "CHANGELOG.md" ,
            errorResId = R.string.error_loading_changelog_message ,
            context = context
        )
    }

    /**
     * Retrieves the End User License Agreement (EULA) Markdown content from a GitHub repository.
     *
     * This function fetches the `EULA.md` file from the designated GitHub repository for the specified
     * package name. It uses the `fetchMarkdownFile` function to handle the actual retrieval and error handling.
     *
     * @param packageName The name of the package (also used as the repository name) for which to fetch the EULA.
     *                   This should correspond to the repository name on GitHub (e.g., "my-awesome-package").
     * @param context The Android Context, used to access resources and display error messages.
     * @return A String containing the EULA content in Markdown format, or an empty string if an error occurred.
     *
     * @see fetchMarkdownFile
     */
    private suspend fun getEulaMarkdown(
        packageName : String , context : Context
    ) : String {
        return fetchMarkdownFile(
            packageName = packageName ,
            fileName = "EULA.md" ,
            errorResId = R.string.error_loading_eula_message ,
            context = context
        )
    }

    /**
     * Extracts the changelog for a specific version from a markdown-formatted changelog string.
     *
     * This function parses a markdown string, typically representing a changelog, and
     * extracts the section corresponding to the provided `currentVersionName`. It expects
     * changelog entries to be demarcated by headings of the form `# Version <version_name>`.
     *
     * The extracted changelog will include the heading for the specified version and all
     * content up to the next version heading or the end of the string.
     *
     * If a changelog entry for the specified version is not found, a default message is returned.
     *
     * @param markdown The markdown-formatted changelog string.
     * @param currentVersionName The name (or number) of the version to extract the changelog for.
     *                       This should match the version name used in the changelog's headings
     *                       (e.g., "1.0.0", "v2.1", etc.)
     * @return The changelog for the specified version as a String, or a message indicating
     *         that no changelog was found if the version is absent. The extracted changelog will
     *         retain the markdown format, including the version heading.
     *
     * @sample
     *   val changelog = """
     *       # Version 1.0.0
     *       - Initial release
     *
     *       # Version 1.1.0
     *       - Added feature A
     *       - Fixed bug B
     *   """
     *   val version110Changelog = extractLatestVersionChangelog(changelog, "1.1.0")
     *   // version110Changelog will be:
     *   // "# Version 1.1.0\n- Added feature A\n- Fixed bug B"
     *
     *   val version120Changelog = extractLatestVersionChangelog(changelog, "1.2.0")
     *   // version120Changelog will be:
     *   // "No changelog available for version 1.2.0"
     */
    private fun extractLatestVersionChangelog(
        markdown : String , currentVersionName : String
    ) : String {
        val regex = Regex("# Version\\s+$currentVersionName:?\\s*[\\s\\S]*?(?=# Version|$)")
        val match = regex.find(markdown)
        return match?.value?.trim() ?: "No changelog available for version $currentVersionName"
    }

    /**
     * Converts a Markdown string to its HTML representation using the CommonMark library.
     *
     * This function utilizes a `Parser` to interpret the Markdown syntax and an `HtmlRenderer`
     * to generate the corresponding HTML output. It handles standard Markdown elements and attributes
     * supported by the CommonMark specification.
     *
     * @param markdown The Markdown string to be converted.
     * @return The HTML representation of the input Markdown string.
     *
     * @throws Exception If an error occurs during the parsing or rendering process.  (Optional, if exceptions are possible)
     *
     * Example:
     * ```kotlin
     *  val markdownText = "# Heading 1\nThis is a paragraph."
     *  val htmlText = convertMarkdownToHtml(markdownText)
     *  println(htmlText)
     *  // Expected Output: "<h1>Heading 1</h1>\n<p>This is a paragraph.</p>\n"
     * ```
     */
    private fun convertMarkdownToHtml(markdown : String) : String {
        val parser = Parser.builder().build()
        val renderer = HtmlRenderer.builder().build()
        val document = parser.parse(markdown)
        return renderer.render(document)
    }

    /**
     * Loads and returns a Pair containing the Changelog HTML and EULA HTML.
     *
     * This function retrieves the changelog and EULA markdown files associated with a given package,
     * extracts the changelog specific to the current version, converts both to HTML, and returns them as a Pair.
     *
     * @param packageName The package name of the application.
     * @param currentVersionName The current version name of the application.
     * @param context The Android Context.
     * @return A Pair where the first element is the Changelog HTML (String or null if not found) and the second element is the EULA HTML (String or null if not found).
     */
    suspend fun loadHtmlData(
        packageName : String , currentVersionName : String , context : Context
    ) : Pair<String? , String?> {
        val changelogMarkdown = getChangelogMarkdown(packageName , context)
        val extractedChangelog =
                extractLatestVersionChangelog(changelogMarkdown , currentVersionName)
        val changelogHtml = convertMarkdownToHtml(extractedChangelog)

        val eulaMarkdown = getEulaMarkdown(packageName , context)
        val eulaHtml = convertMarkdownToHtml(eulaMarkdown)

        return changelogHtml to eulaHtml
    }
}

/**
 * A Composable function that loads HTML data for the changelog and EULA.
 *
 * This function uses [produceState] to fetch HTML data asynchronously and exposes the result as a
 * `State<Pair<String?, String?>>` for UI consumption. The pair represents the changelog HTML and the
 * EULA HTML, respectively.
 *
 * The function utilizes [OpenSourceLicensesUtils.loadHtmlData] to retrieve the HTML data.
 *
 * @param packageName The package name of the application. This is used to locate the relevant HTML files.
 * @param currentVersionName The current version name of the application. This is used to potentially
 *  load version specific changelog information.
 * @param context The Android [Context] required to access resources.
 * @return A [State] object containing a [Pair] where the first element is the changelog HTML as a String
 *  (or null if not found) and the second element is the EULA HTML as a String (or null if not found).
 */
@Composable
fun rememberHtmlData(
    packageName : String , currentVersionName : String , context : Context
) : State<Pair<String? , String?>> {
    return produceState<Pair<String? , String?>>(
        initialValue = null to null
    ) {
        val changelogMarkdown = OpenSourceLicensesUtils.loadHtmlData(
            packageName = packageName , currentVersionName = currentVersionName , context = context
        )
        value = changelogMarkdown
    }
}