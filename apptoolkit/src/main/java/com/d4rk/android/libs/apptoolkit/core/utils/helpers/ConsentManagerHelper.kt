package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.first
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent

object ConsentManagerHelper : KoinComponent {

    val configProvider: BuildInfoProvider by inject()
    val defaultAnalyticsGranted = configProvider.isDebugBuild

    /**
     * Updates the user's consent settings in Firebase Analytics.
     *
     * This function assumes the "Usage and Diagnostics" toggle controls all four consent types
     * (ANALYTICS_STORAGE, AD_STORAGE, AD_USER_DATA, AD_PERSONALIZATION).
     * If your "Usage and Diagnostics" toggle has a more limited scope (e.g., only analytics),
     * you MUST adjust the logic below to correctly set only the relevant consent types
     * and decide how the other types are managed (e.g., separate toggles, manifest defaults).
     *
     */
    fun updateConsent(
        analyticsGranted: Boolean,
        adStorageGranted: Boolean,
        adUserDataGranted: Boolean,
        adPersonalizationGranted: Boolean
    ) {
        val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics
        val consentSettings: MutableMap<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus> =
                mutableMapOf<FirebaseAnalytics.ConsentType, FirebaseAnalytics.ConsentStatus>()

        consentSettings[FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE] =
                if (analyticsGranted) FirebaseAnalytics.ConsentStatus.GRANTED else FirebaseAnalytics.ConsentStatus.DENIED

        consentSettings[FirebaseAnalytics.ConsentType.AD_STORAGE] =
                if (adStorageGranted) FirebaseAnalytics.ConsentStatus.GRANTED else FirebaseAnalytics.ConsentStatus.DENIED

        consentSettings[FirebaseAnalytics.ConsentType.AD_USER_DATA] =
                if (adUserDataGranted) FirebaseAnalytics.ConsentStatus.GRANTED else FirebaseAnalytics.ConsentStatus.DENIED

        consentSettings[FirebaseAnalytics.ConsentType.AD_PERSONALIZATION] =
                if (adPersonalizationGranted) FirebaseAnalytics.ConsentStatus.GRANTED else FirebaseAnalytics.ConsentStatus.DENIED

        firebaseAnalytics.setConsent(consentSettings)
    }



    /**
     * Reads the persisted "Usage and Diagnostics" setting from DataStore and applies
     * it to Firebase Analytics consent settings on app startup.
     *
     * @param dataStore Your instance of CommonDataStore.
     */
    suspend fun applyInitialConsent(dataStore: CommonDataStore) {
        val analyticsGranted: Boolean = dataStore.analyticsConsent(defaultAnalyticsGranted).first()
        val adStorageGranted: Boolean = dataStore.adStorageConsent(defaultAnalyticsGranted).first()
        val adUserDataGranted: Boolean = dataStore.adUserDataConsent(defaultAnalyticsGranted).first()
        val adPersonalizationGranted: Boolean = dataStore.adPersonalizationConsent(defaultAnalyticsGranted).first()

        updateConsent(
            analyticsGranted = analyticsGranted,
            adStorageGranted = adStorageGranted,
            adUserDataGranted = adUserDataGranted,
            adPersonalizationGranted = adPersonalizationGranted
        )

        updateAnalyticsCollectionFromDatastore(dataStore = dataStore)
    }

    fun updateAnalyticsCollectionFromDatastore(dataStore: CommonDataStore) {
        val usageAndDiagnosticsGranted: Boolean = dataStore.usageAndDiagnostics(defaultAnalyticsGranted).first()
        Firebase.analytics.setAnalyticsCollectionEnabled(usageAndDiagnosticsGranted)
        Firebase.analytics.setAnalyticsCollectionEnabled(usageAndDiagnosticsGranted)
    }
}