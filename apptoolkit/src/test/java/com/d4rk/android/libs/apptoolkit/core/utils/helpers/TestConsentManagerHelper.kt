package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.FirebasePerformance
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class TestConsentManagerHelper {

    @Test
    fun `updateConsent passes values to firebase`() {
        val analytics = mockk<FirebaseAnalytics>(relaxed = true)
        mockkObject(Firebase)
        every { Firebase.analytics } returns analytics
        justRun { analytics.setConsent(any()) }

        ConsentManagerHelper.updateConsent(
            analyticsGranted = true,
            adStorageGranted = false,
            adUserDataGranted = true,
            adPersonalizationGranted = false
        )

        verify {
            analytics.setConsent(match {
                it[FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE] == FirebaseAnalytics.ConsentStatus.GRANTED &&
                it[FirebaseAnalytics.ConsentType.AD_STORAGE] == FirebaseAnalytics.ConsentStatus.DENIED &&
                it[FirebaseAnalytics.ConsentType.AD_USER_DATA] == FirebaseAnalytics.ConsentStatus.GRANTED &&
                it[FirebaseAnalytics.ConsentType.AD_PERSONALIZATION] == FirebaseAnalytics.ConsentStatus.DENIED
            })
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `applyInitialConsent reads datastore and initializes firebase`() = runTest {
        val dataStore = mockk<CommonDataStore>()
        every { dataStore.analyticsConsent(any()) } returns flowOf(true)
        every { dataStore.adStorageConsent(any()) } returns flowOf(false)
        every { dataStore.adUserDataConsent(any()) } returns flowOf(true)
        every { dataStore.adPersonalizationConsent(any()) } returns flowOf(false)
        every { dataStore.usageAndDiagnostics(any()) } returns flowOf(true)

        val provider = mockk<BuildInfoProvider>()
        every { provider.isDebugBuild } returns false
        startKoin { modules(module { single<BuildInfoProvider> { provider } }) }

        val analytics = mockk<FirebaseAnalytics>(relaxed = true)
        mockkObject(Firebase)
        every { Firebase.analytics } returns analytics

        val crashlytics = mockk<FirebaseCrashlytics>(relaxed = true)
        val performance = mockk<FirebasePerformance>(relaxed = true)
        mockkStatic(FirebaseCrashlytics::class)
        mockkStatic(FirebasePerformance::class)
        every { FirebaseCrashlytics.getInstance() } returns crashlytics
        every { FirebasePerformance.getInstance() } returns performance

        ConsentManagerHelper.applyInitialConsent(dataStore)

        verify { analytics.setConsent(any()) }
        verify { analytics.setAnalyticsCollectionEnabled(true) }
        verify { crashlytics.isCrashlyticsCollectionEnabled = true }
        verify { performance.isPerformanceCollectionEnabled = true }

        stopKoin()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `updateAnalyticsCollectionFromDatastore sets collection flags`() = runTest {
        val dataStore = mockk<CommonDataStore>()
        every { dataStore.usageAndDiagnostics(any()) } returnsMany listOf(flowOf(true), flowOf(false))

        val analytics = mockk<FirebaseAnalytics>(relaxed = true)
        mockkObject(Firebase)
        every { Firebase.analytics } returns analytics
        val crashlytics = mockk<FirebaseCrashlytics>(relaxed = true)
        val performance = mockk<FirebasePerformance>(relaxed = true)
        mockkStatic(FirebaseCrashlytics::class)
        mockkStatic(FirebasePerformance::class)
        every { FirebaseCrashlytics.getInstance() } returns crashlytics
        every { FirebasePerformance.getInstance() } returns performance

        ConsentManagerHelper.updateAnalyticsCollectionFromDatastore(dataStore)
        verify { analytics.setAnalyticsCollectionEnabled(true) }
        verify { crashlytics.isCrashlyticsCollectionEnabled = true }
        verify { performance.isPerformanceCollectionEnabled = true }

        ConsentManagerHelper.updateAnalyticsCollectionFromDatastore(dataStore)
        verify { analytics.setAnalyticsCollectionEnabled(false) }
        verify { crashlytics.isCrashlyticsCollectionEnabled = false }
        verify { performance.isPerformanceCollectionEnabled = false }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `applyInitialConsent propagates io exception`() = runTest {
        val dataStore = mockk<CommonDataStore>()
        every { dataStore.analyticsConsent(any()) } returns flow { throw java.io.IOException("io") }
        every { dataStore.adStorageConsent(any()) } returns flowOf(true)
        every { dataStore.adUserDataConsent(any()) } returns flowOf(true)
        every { dataStore.adPersonalizationConsent(any()) } returns flowOf(true)

        val provider = mockk<BuildInfoProvider>()
        every { provider.isDebugBuild } returns false
        startKoin { modules(module { single<BuildInfoProvider> { provider } }) }

        val field = ConsentManagerHelper::class.java.getDeclaredField("defaultAnalyticsGranted\$delegate")
        field.isAccessible = true
        field.set(ConsentManagerHelper, lazy { !provider.isDebugBuild })

        assertFailsWith<java.io.IOException> {
            ConsentManagerHelper.applyInitialConsent(dataStore)
        }

        stopKoin()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `applyInitialConsent propagates cancellation exception`() = runTest {
        val dataStore = mockk<CommonDataStore>()
        every { dataStore.analyticsConsent(any()) } returns flow { throw kotlinx.coroutines.CancellationException("cancel") }
        every { dataStore.adStorageConsent(any()) } returns flowOf(true)
        every { dataStore.adUserDataConsent(any()) } returns flowOf(true)
        every { dataStore.adPersonalizationConsent(any()) } returns flowOf(true)

        val provider = mockk<BuildInfoProvider>()
        every { provider.isDebugBuild } returns false
        startKoin { modules(module { single<BuildInfoProvider> { provider } }) }

        val field = ConsentManagerHelper::class.java.getDeclaredField("defaultAnalyticsGranted\$delegate")
        field.isAccessible = true
        field.set(ConsentManagerHelper, lazy { !provider.isDebugBuild })

        assertFailsWith<kotlinx.coroutines.CancellationException> {
            ConsentManagerHelper.applyInitialConsent(dataStore)
        }

        stopKoin()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `updateAnalyticsCollectionFromDatastore propagates firebase failure`() = runTest {
        val dataStore = mockk<CommonDataStore>()
        every { dataStore.usageAndDiagnostics(any()) } returns flowOf(true)

        val analytics = mockk<FirebaseAnalytics>(relaxed = true)
        mockkObject(Firebase)
        every { Firebase.analytics } returns analytics
        val crashlytics = mockk<FirebaseCrashlytics>()
        val performance = mockk<FirebasePerformance>(relaxed = true)
        mockkStatic(FirebaseCrashlytics::class)
        mockkStatic(FirebasePerformance::class)
        every { FirebaseCrashlytics.getInstance() } returns crashlytics
        every { FirebasePerformance.getInstance() } returns performance
        every { crashlytics.isCrashlyticsCollectionEnabled = any() } throws RuntimeException("fail")

        assertFailsWith<RuntimeException> {
            ConsentManagerHelper.updateAnalyticsCollectionFromDatastore(dataStore)
        }
    }

    @Test
    fun `defaultAnalyticsGranted false when debug build`() {
        val provider = mockk<BuildInfoProvider>()
        every { provider.isDebugBuild } returns true
        startKoin { modules(module { single<BuildInfoProvider> { provider } }) }

        val field = ConsentManagerHelper::class.java.getDeclaredField("defaultAnalyticsGranted\$delegate")
        field.isAccessible = true
        field.set(ConsentManagerHelper, lazy { !provider.isDebugBuild })

        assertFalse(ConsentManagerHelper.defaultAnalyticsGranted)

        stopKoin()
    }
}
