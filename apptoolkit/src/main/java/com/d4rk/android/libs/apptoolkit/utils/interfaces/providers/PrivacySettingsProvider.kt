package com.d4rk.android.libs.apptoolkit.utils.interfaces.providers

interface PrivacySettingsProvider {
    val privacyPolicyUrl : String
        get() = "https://sites.google.com/view/d4rk7355608/more/apps/privacy-policy"

    val termsOfServiceUrl : String
        get() = "https://sites.google.com/view/d4rk7355608/more/apps/terms-of-service"

    val codeOfConductUrl : String
        get() = "https://sites.google.com/view/d4rk7355608/more/code-of-conduct"

    val legalNoticesUrl : String
        get() = "https://sites.google.com/view/d4rk7355608/more/apps/legal-notices"

    val licenseUrl : String
        get() = "https://www.gnu.org/licenses/gpl-3.0"

    fun openPermissionsScreen()
    fun openAdsScreen()
    fun openUsageAndDiagnosticsScreen()
}