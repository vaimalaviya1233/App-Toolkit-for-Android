package com.d4rk.android.libs.apptoolkit.utils.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.d4rk.android.libs.apptoolkit.R
import com.mikepenz.aboutlibraries.LibsBuilder

/**
 * A utility object for performing common operations such as opening URLs, activities, and app notification settings.
 *
 * This object provides functions to open a URL in the default browser, open an activity, and open the app's notification settings.
 * All operations are performed in the context of an Android application.
 */
object IntentsHelper {

    /**
     * Opens a specified URL in the default browser.
     *
     * This function creates an Intent with the ACTION_VIEW action and the specified URL, and starts an activity with this intent.
     * The activity runs in a new task.
     *
     * @param context The Android context in which the URL should be opened.
     * @param url The URL to open.
     */
    fun openUrl(context : Context , url : String) {
        Intent(Intent.ACTION_VIEW , Uri.parse(url)).let { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * Opens a specified activity.
     *
     * This function creates an Intent with the specified activity class, and starts an activity with this intent. The activity runs in a new task.
     *
     * @param context The Android context in which the activity should be opened.
     * @param activityClass The class of the activity to open.
     */
    fun openActivity(context : Context , activityClass : Class<*>) {
        Intent(context , activityClass).let { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * Opens the app's notification settings.
     *
     * This function creates an Intent with the ACTION_APP_NOTIFICATION_SETTINGS action and the app's package name, and starts an activity with this intent.
     * The activity runs in a new task.
     *
     * @param context The Android context in which the app's notification settings should be opened.
     */
    fun openAppNotificationSettings(context : Context) {
        val intent : Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE , context.packageName)
            }
        }
        else {
            Intent().apply {
                action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                data = Uri.fromParts("package" , context.packageName , null)
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * Opens the app's share sheet, allowing users to share a message about the app.
     *
     * This function constructs a share message using a provided string resource and the app's Play Store link.
     * It then creates an ACTION_SEND intent with this message and uses a chooser intent to present the user with options for sharing the message (e.g., email, messaging apps).
     *
     * @param context The Android context in which the share sheet should be opened.
     * @param shareMessageFormat The resource ID of the string to be used as the base for the share message. This string should include a placeholder, where the app's playstore link will be injected.
     *                           Example : "Check out this awesome app! Download it here: %s"
     *                           The %s will be replaced by the app's Play Store URL.
     */
    fun shareApp(context : Context , shareMessageFormat : Int) {
        val messageToShare : String = context.getString(
            shareMessageFormat ,
            "https://play.google.com/store/apps/details?id=${context.packageName}"
        )
        val sendIntent : Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT , messageToShare)
            type = "text/plain"
        }
        context.startActivity(
            Intent.createChooser(
                sendIntent , context.resources.getText(R.string.send_email_using)
            )
        )
    }

    /**
     * Composes and sends an email to the developer with a predefined subject and message body.
     *
     * This function constructs an email Intent with the developer's email address pre-filled in the "to" field.
     * The subject line includes the application name (obtained from the provided resource ID) for easy identification.
     * The email body starts with a generic salutation and provides a blank line for the user to enter their feedback.
     * A chooser dialog is displayed, allowing the user to select their preferred email application for sending.
     *
     * @param context The Android context used to access resources and start the activity.
     * @param applicationName  The resource ID of the string containing the application's name.
     *
     * @throws android.content.ActivityNotFoundException if no email app is installed on the device.
     */
    fun sendEmailToDeveloper(context : Context , applicationName : Int) {
        val developerEmail = "d4rk7355608@gmail.com"
        val applicationName : String = context.getString(applicationName)
        val subject : String = context.getString(R.string.feedback_for) + applicationName
        val emailBodyTemplate : String = context.getString(R.string.dear_developer) + "\n\n"

        val feedbackEmailIntent : Intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$developerEmail")
            putExtra(Intent.EXTRA_SUBJECT , subject)
            putExtra(Intent.EXTRA_TEXT , emailBodyTemplate)
        }

        context.startActivity(
            Intent.createChooser(
                feedbackEmailIntent , context.resources.getText(R.string.send_email_using)
            )
        )
    }

    /**
     * Opens a screen displaying open-source licenses, EULA, and changelog information.
     *
     * This function uses the `LibsBuilder` library to create and display an activity that shows
     * details about the application, including its open-source licenses, End-User License Agreement (EULA),
     * and changelog. It configures the activity with specific titles, content, and formatting options.
     *
     * @param context The context from which the activity is launched.
     * @param eulaHtmlString An optional HTML string containing the EULA content. If null, a loading message is displayed.
     * @param changelogHtmlString An optional HTML string containing the changelog content. If null, a loading message is displayed.
     * @param ossLicenseTitle The string resource ID for the title of the open-source licenses section.
     * @param appName The string resource ID for the application name.
     * @param appVersion The string resource ID for the application version.
     * @param appVersionCode The integer representing the application's version code.
     * @param eulaTitle The string resource ID for the title of the EULA section.
     * @param loadingEula The string resource ID for the message to display while the EULA is loading.
     * @param changelogTitle The string resource ID for the title of the changelog section.
     * @param loadingChangelog The string resource ID for the message to display while the changelog is loading.
     * @param appShortDescription The string resource ID for a short description of the application.
     */
    fun openLicensesScreen(
        context : Context ,
        eulaHtmlString : String? ,
        changelogHtmlString : String? ,
        ossLicenseTitle : Int ,
        appName : Int ,
        appVersion : Int ,
        appVersionCode : Int ,
        eulaTitle : Int ,
        loadingEula : Int ,
        changelogTitle : Int ,
        loadingChangelog : Int ,
        appShortDescription : Int
    ) {
        LibsBuilder().withActivityTitle(
            activityTitle = context.getString(ossLicenseTitle)
        ).withEdgeToEdge(asEdgeToEdge = true).withShowLoadingProgress(showLoadingProgress = true)
                .withSearchEnabled(searchEnabled = true).withAboutIconShown(aboutShowIcon = true)
                .withAboutAppName(aboutAppName = context.getString(appName))
                .withVersionShown(showVersion = true)
                .withAboutVersionString(aboutVersionString = "$appVersion ($appVersionCode)")
                .withLicenseShown(showLicense = true).withAboutVersionShown(aboutShowVersion = true)
                .withAboutVersionShownName(aboutShowVersion = true)
                .withAboutVersionShownCode(aboutShowVersion = true)
                .withAboutSpecial1(aboutAppSpecial1 = context.getString(eulaTitle))
                .withAboutSpecial1Description(
                    aboutAppSpecial1Description = eulaHtmlString ?: context.getString(loadingEula)
                ).withAboutSpecial2(aboutAppSpecial2 = context.getString(changelogTitle))
                .withAboutSpecial2Description(
                    aboutAppSpecial2Description = changelogHtmlString ?: context.getString(
                        loadingChangelog
                    )
                ).withAboutDescription(aboutDescription = context.getString(appShortDescription))
                .activity(ctx = context)
    }
}