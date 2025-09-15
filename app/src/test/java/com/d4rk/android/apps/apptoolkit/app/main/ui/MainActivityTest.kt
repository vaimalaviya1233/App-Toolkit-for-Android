package com.d4rk.android.apps.apptoolkit.app.main.ui

import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.d4rk.android.libs.apptoolkit.app.main.utils.InAppUpdateHelper
import com.d4rk.android.libs.apptoolkit.app.startup.ui.StartupActivity
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ConsentFormHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ConsentManagerHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ReviewHelper
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import com.google.common.truth.Truth.assertThat
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.Robolectric
import org.robolectric.annotation.LooperMode
import org.robolectric.junit5.RobolectricExtension
import org.robolectric.shadows.ShadowLooper

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(RobolectricExtension::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MainActivityTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var dataStore: DataStore
    private lateinit var consentInformation: ConsentInformation
    private lateinit var appUpdateManager: AppUpdateManager

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        dataStore = mockk(relaxed = true)
        consentInformation = mockk(relaxed = true)
        appUpdateManager = mockk(relaxed = true)

        mockkStatic(MobileAds::class)
        every { MobileAds.initialize(any(), any()) } returns Unit

        mockkObject(ConsentManagerHelper)
        coJustRun { ConsentManagerHelper.applyInitialConsent(any()) }

        mockkObject(ConsentFormHelper)
        coJustRun { ConsentFormHelper.showConsentFormIfRequired(any(), any()) }

        mockkObject(ReviewHelper)
        every { ReviewHelper.launchInAppReviewIfEligible(any(), any(), any(), any(), any()) } returns Unit

        mockkObject(InAppUpdateHelper)
        coJustRun { InAppUpdateHelper.performUpdate(any(), any()) }

        mockkObject(IntentsHelper)
        every { IntentsHelper.openActivity(any(), any()) } returns Unit

        mockkStatic(UserMessagingPlatform::class)
        every { UserMessagingPlatform.getConsentInformation(any()) } returns consentInformation

        mockkStatic(AppUpdateManagerFactory::class)
        every { AppUpdateManagerFactory.create(any()) } returns appUpdateManager

        every { dataStore.startup } returns flowOf(false)
        every { dataStore.sessionCount } returns flowOf(0)
        every { dataStore.hasPromptedReview } returns flowOf(false)
        coJustRun { dataStore.setHasPromptedReview(any()) }
        coJustRun { dataStore.incrementSessionCount() }

        stopKoinIfRunning()
        startKoin {
            modules(
                module {
                    single { dataStore }
                    single<DispatcherProvider> { TestDispatcherProvider(dispatcher) }
                }
            )
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoinIfRunning()
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `handleStartup opens StartupActivity on first launch`() {
        every { dataStore.startup } returns flowOf(true)

        val activity = buildActivity()
        drainPendingTasks()

        verify(exactly = 1) {
            IntentsHelper.openActivity(activity = activity, activityClass = StartupActivity::class.java)
        }
    }

    @Test
    fun `handleStartup sets content when not first launch`() {
        val activity = buildActivity()
        drainPendingTasks()

        verify(exactly = 0) { IntentsHelper.openActivity(any(), any()) }

        val contentView = activity.findViewById<ViewGroup>(android.R.id.content)
        assertThat(contentView.childCount).isGreaterThan(0)
    }

    @Test
    fun `checkUserConsent delegates to ConsentFormHelper`() {
        val activity = buildActivity()
        drainPendingTasks()

        coVerify(exactly = 1) {
            ConsentFormHelper.showConsentFormIfRequired(activity = activity, consentInfo = consentInformation)
        }
    }

    @Test
    fun `checkInAppReview delegates to ReviewHelper`() {
        val expectedSessionCount = 7
        every { dataStore.sessionCount } returns flowOf(expectedSessionCount)
        every { dataStore.hasPromptedReview } returns flowOf(true)

        val activity = buildActivity()
        drainPendingTasks()

        val scopeSlot = slot<CoroutineScope>()
        verify(exactly = 1) {
            ReviewHelper.launchInAppReviewIfEligible(
                activity = activity,
                sessionCount = expectedSessionCount,
                hasPromptedBefore = true,
                scope = capture(scopeSlot),
                onReviewLaunched = any()
            )
        }
        assertThat(scopeSlot.captured.coroutineContext[Job]).isNotNull()
    }

    @Test
    fun `checkForUpdates delegates to InAppUpdateHelper`() {
        val activity = buildActivity()
        drainPendingTasks()

        val launcherField = MainActivity::class.java.getDeclaredField("updateResultLauncher")
        launcherField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val expectedLauncher = launcherField.get(activity) as ActivityResultLauncher<IntentSenderRequest>

        coVerify(exactly = 1) {
            InAppUpdateHelper.performUpdate(
                appUpdateManager = appUpdateManager,
                updateResultLauncher = expectedLauncher
            )
        }
    }

    private fun buildActivity(): MainActivity {
        return Robolectric.buildActivity(MainActivity::class.java).setup().get()
    }

    private fun drainPendingTasks() {
        dispatcher.scheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()
        dispatcher.scheduler.advanceUntilIdle()
    }

    private fun stopKoinIfRunning() {
        GlobalContext.getOrNull()?.let { stopKoin() }
    }

    private class TestDispatcherProvider(
        private val dispatcher: CoroutineDispatcher
    ) : DispatcherProvider {
        override val main: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
        override val unconfined: CoroutineDispatcher = dispatcher
    }
}
