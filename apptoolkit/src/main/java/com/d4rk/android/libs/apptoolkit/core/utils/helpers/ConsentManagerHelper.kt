package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.first

object ConsentManagerHelper {

    /**
     * Updates the user's consent settings in Firebase Analytics.
     *
     * This function assumes the "Usage and Diagnostics" toggle controls all four consent types
     * (ANALYTICS_STORAGE, AD_STORAGE, AD_USER_DATA, AD_PERSONALIZATION).
     * If your "Usage and Diagnostics" toggle has a more limited scope (e.g., only analytics),
     * you MUST adjust the logic below to correctly set only the relevant consent types
     * and decide how the other types are managed (e.g., separate toggles, manifest defaults).
     *
     * @param usageAndDiagnosticsEnabled True if the user has consented to usage and diagnostics, false otherwise.
     */
    fun updateConsent(usageAndDiagnosticsEnabled: Boolean) {
        val firebaseAnalytics = Firebase.analytics
        val consentSettings =
            mutableMapOf<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>()

        val status =
            if (usageAndDiagnosticsEnabled) FirebaseAnalytics.ConsentStatus.GRANTED else FirebaseAnalytics.ConsentStatus.DENIED

        consentSettings[FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE] = status
        consentSettings[FirebaseAnalytics.ConsentType.AD_STORAGE] = status
        consentSettings[FirebaseAnalytics.ConsentType.AD_USER_DATA] = status
        consentSettings[FirebaseAnalytics.ConsentType.AD_PERSONALIZATION] = status

        firebaseAnalytics.setConsent(consentSettings)
    }

    /**
     * Reads the persisted "Usage and Diagnostics" setting from DataStore and applies
     * it to Firebase Analytics consent settings on app startup.
     *
     * @param dataStore Your instance of CommonDataStore.
     * @param defaultConsent The default value to assume if no preference is found in DataStore.
     *                       This should align with your app's initial desired state
     *                       (e.g., true for opt-out by default regions,
     *                       false for opt-in by default regions before user interaction,
     *                       especially for AD_STORAGE, AD_USER_DATA, and AD_PERSONALIZATION in EEA).
     */
    suspend fun applyInitialConsent(dataStore: CommonDataStore, defaultConsent: Boolean = true) {
        val isEnabled = dataStore.usageAndDiagnostics(default = defaultConsent).first()
        updateConsent(usageAndDiagnosticsEnabled = isEnabled)
    }
}