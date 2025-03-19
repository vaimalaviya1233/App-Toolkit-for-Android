package com.d4rk.android.libs.apptoolkit.app.settings.utils.providers

/**
 * Interface for providing access to various privacy-related settings and resources.
 *
 * This interface defines a set of properties and functions that expose information
 * about the application's privacy policy, terms of service, code of conduct,
 * legal notices, and license. It also provides methods for navigating to specific
 * screens related to privacy and user data within the application, such as
 * permissions, ads, and usage/diagnostics.
 *
 * Implementors of this interface are responsible for providing the actual
 * URLs and handling the navigation logic to these screens.
 */
interface PrivacySettingsProvider {

    /**
     * The URL of the privacy policy.
     *
     * This property returns the URL for the application's privacy policy.
     *
     * @see <a href="https://sites.google.com/view/d4rk7355608/more/apps/privacy-policy">Privacy Policy</a>
     */
    val privacyPolicyUrl : String
        get() = "https://sites.google.com/view/d4rk7355608/more/apps/privacy-policy"

    /**
     * The URL of the terms of service.
     *
     * This property returns the URL for the application's terms of service.
     *
     * @see <a href="https://sites.google.com/view/d4rk7355608/more/apps/terms-of-service">Terms of Service</a>
     */
    val termsOfServiceUrl : String
        get() = "https://sites.google.com/view/d4rk7355608/more/apps/terms-of-service"

    /**
     * The URL of the code of conduct.
     *
     * This property returns the URL for the application's code of conduct.
     *
     * @see <a href="https://sites.google.com/view/d4rk7355608/more/code-of-conduct">Code of Conduct</a>
     */
    val codeOfConductUrl : String
        get() = "https://sites.google.com/view/d4rk7355608/more/code-of-conduct"

    /**
     * The URL of the legal notices.
     *
     * @see <a href="https://sites.google.com/view/d4rk7355608/more/apps/legal-notices">Legal Notices</a>
     */
    val legalNoticesUrl : String
        get() = "https://sites.google.com/view/d4rk7355608/more/apps/legal-notices"

    /**
     * The URL of the license under which this software is distributed.
     *
     * This property returns the URL for the GNU General Public License, version 3.0.
     *
     * @see <a href="https://www.gnu.org/licenses/gpl-3.0">GNU GPLv3</a>
     */
    val licenseUrl : String
        get() = "https://www.gnu.org/licenses/gpl-3.0"

    fun openPermissionsScreen()

    /**
     * Opens the Ads screen in the application.
     *
     * This function is responsible for navigating the user to the screen
     * where they can view and interact with advertisements. It typically
     * initiates a transition to a new activity or fragment that displays
     * ad content.
     *
     * **Note:** The exact behavior and implementation of this function
     * may vary depending on the application's architecture and the
     * specific advertising platform being used. It could potentially
     * handle:
     *   - Loading ads from a server.
     *   - Displaying interstitial or banner ads.
     *   - Handling user clicks on ads.
     *   - Rewarded video ads and user reward logic.
     *
     * **Preconditions:**
     *   - The application must be properly initialized and in a state
     *     where it can display ads.
     *   - Any required permissions for displaying ads (e.g., network access)
     *     must be granted.
     *
     * **Postconditions:**
     *   - The user is presented with the Ads screen.
     *   - The Ads screen is visible and interactive.
     *   - Any necessary ad loading and display operations have begun.
     *
     * **Side Effects:**
     *   - Navigation state of the application changes.
     *   - Ad loading may begin, consuming network resources.
     *   - Potentially modifies the user interface.
     *
     * **Example Usage:**
     * ```kotlin
     * // ... in some part of your application logic ...
     * openAdsScreen() // Opens the Ads screen
     * ```
     */
    fun openAdsScreen()

    /**
     * Opens the Usage and Diagnostics screen in the application.
     *
     * This function is responsible for navigating the user to the screen where they
     * can view and manage their usage data and diagnostic information related to the app.
     *
     * This screen typically allows users to:
     *  - View how much data they have used.
     *  - See any diagnostics information collected.
     *  - Potentially opt-in or opt-out of usage data collection.
     *  - Access further settings related to data privacy and diagnostics.
     *
     * The specific implementation of how this screen is opened (e.g., using an Intent,
     * a Fragment transaction, or another navigation method) is determined by the
     * application's architecture and is handled internally by this function.
     *
     * @throws SomeExceptionType If there's a specific error scenario where this function might fail, document that here. For example:
     *         - ActivityNotFoundException: If there is no screen configured to handle the Usage and Diagnostics Intent.
     *
     * @see SomeRelatedClass If there's a related class or function relevant to this screen, link it here for more context.
     */
    fun openUsageAndDiagnosticsScreen()
}