package com.d4rk.android.libs.apptoolkit.ui.components.network

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.AboutLibrariesHelper

/**
 * A Composable function that loads HTML data for the changelog and EULA.
 *
 * This function uses [produceState] to fetch HTML data asynchronously and exposes the result as a
 * `State<Pair<String?, String?>>` for UI consumption. The pair represents the changelog HTML and the
 * EULA HTML, respectively.
 *
 * The function utilizes [AboutLibrariesHelper.loadHtmlData] to retrieve the HTML data.
 *
 * @param packageName The package name of the application. This is used to locate the relevant HTML files.
 * @param currentVersionName The current version name of the application. This is used to potentially
 *  load version specific changelog information.
 * @param context The Android [Context] required to access resources.
 * @return A [State] object containing a [Pair] where the first element is the changelog HTML as a String
 *  (or null if not found) and the second element is the EULA HTML as a String (or null if not found).
 */
@Composable
fun rememberHtmlData(packageName : String , currentVersionName : String , context : Context) : State<Pair<String? , String?>> {
    return produceState<Pair<String? , String?>>(initialValue = null to null) {
        val changelogMarkdown : Pair<String? , String?> = AboutLibrariesHelper.loadHtmlData(packageName = packageName , currentVersionName = currentVersionName , context = context)
        value = changelogMarkdown
    }
}