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
     * This function creates an email Intent with the developer's email address as the recipient.
     * The email subject is dynamically generated, including the application's name (obtained from the provided resource ID),
     * prefixed with "Feedback for: " to help identify the source of the email.
     * The email body begins with "Dear Developer,\n\n" providing a space for the user to add their feedback, issue report, or other message.
     * It then utilizes a chooser to allow the user to select their preferred email client before sending the email.
     *
     * @param context The Android context used to access resources (like string resources) and start the email activity.
     * @param applicationName  The resource ID of the string containing the application's name. This is used to generate the email subject line.
     *
     * @throws android.content.ActivityNotFoundException if no email application is installed on the device that can handle the email intent.
     */
    fun sendEmailToDeveloper(context : Context , applicationName : Int) {
        val developerEmail = "d4rk7355608@gmail.com"
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
     * This function uses the `LibsBuilder` library to create and display an activity that provides
     * details about the application, including its open-source licenses, End-User License Agreement (EULA),
     * and changelog. It configures the activity's appearance and content using provided parameters.
     *
     * The screen includes:
     *  - A list of open-source licenses used by the application.
     *  - An optional EULA section, which displays the EULA content provided as an HTML string. If no EULA is provided, a loading message is displayed.
     *  - An optional changelog section, displaying the changelog content provided as an HTML string. If no changelog is provided, a loading message is displayed.
     *  - Application name, version, and a short description.
     *
     * @param context The context from which the activity is launched.
     * @param eulaHtmlString An optional HTML string containing the EULA content. If `null`, a loading message is displayed in its place.
     * @param changelogHtmlString An optional HTML string containing the changelog content. If `null`, a loading message is displayed in its place.
     * @param appName The string resource ID for the application's name.
     * @param appVersion The application's version string (e.g., "1.0.0").
     * @param appVersionCode The integer representing the application's version code (e.g., 1).
     * @param appShortDescription The string resource ID for a short description of the application.
     *
     * @see LibsBuilder for more details about the underlying library.
     */
    fun openLicensesScreen(
        context : Context ,
        eulaHtmlString : String? ,
        changelogHtmlString : String? ,
        appName : String ,
        appVersion : String ,
        appVersionCode : Int ,
        appShortDescription : Int
    ) {
        LibsBuilder().withActivityTitle(
            activityTitle = context.getString(R.string.oss_license_title)
        ).withEdgeToEdge(asEdgeToEdge = true).withShowLoadingProgress(showLoadingProgress = true)
                .withSearchEnabled(searchEnabled = true).withAboutIconShown(aboutShowIcon = true)
                .withAboutAppName(aboutAppName = appName)
                .withVersionShown(showVersion = true)
                .withAboutVersionString(aboutVersionString = "$appVersion ($appVersionCode)")
                .withLicenseShown(showLicense = true).withAboutVersionShown(aboutShowVersion = true)
                .withAboutVersionShownName(aboutShowVersion = true)
                .withAboutVersionShownCode(aboutShowVersion = true)
                .withAboutSpecial1(aboutAppSpecial1 = context.getString(R.string.eula_title))
                .withAboutSpecial1Description(
                    aboutAppSpecial1Description = eulaHtmlString ?: context.getString(R.string.loading_eula)
                ).withAboutSpecial2(aboutAppSpecial2 = context.getString(R.string.changelog))
                .withAboutSpecial2Description(
                    aboutAppSpecial2Description = changelogHtmlString ?: context.getString(
                        R.string.loading_changelog
                    )
                ).withAboutDescription(aboutDescription = context.getString(appShortDescription))
                .activity(ctx = context)
    }
}