package com.d4rk.android.apps.apptoolkit.app.main.ui

import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.TestDispatchers
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.app.startup.ui.StartupActivity
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ConsentFormHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ConsentManagerHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.InAppUpdateHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ReviewHelper
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.LooperMode
import org.robolectric.junit5.RobolectricExtension

@ExtendWith(RobolectricExtension::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MainActivityStartupTest {

    @BeforeEach
    fun setup() {
        mockkStatic(MobileAds::class)
        every { MobileAds.initialize(any(), any()) } returns Unit

        mockkObject(ConsentManagerHelper)
        every { ConsentManagerHelper.applyInitialConsent(any()) } just Runs

        mockkObject(ReviewHelper)
        every { ReviewHelper.launchInAppReviewIfEligible(any(), any(), any(), any(), any()) } just Runs

        mockkStatic(UserMessagingPlatform::class)
        every { UserMessagingPlatform.getConsentInformation(any()) } returns mockk<ConsentInformation>(relaxed = true)

        mockkObject(ConsentFormHelper)
        every { ConsentFormHelper.showConsentFormIfRequired(any(), any()) } just Runs

        mockkStatic(AppUpdateManagerFactory::class)
        every { AppUpdateManagerFactory.create(any()) } returns mockk<AppUpdateManager>(relaxed = true)

        mockkObject(InAppUpdateHelper)
        every { InAppUpdateHelper.performUpdate(any(), any()) } just Runs
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
        stopKoin()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when startup true launches StartupActivity`() = runTest {
        val fakeDataStore = mockk<DataStore> {
            every { startup } returns flowOf(true)
        }
        val dispatchers = TestDispatchers(testScheduler)
        startKoin {
            modules(module {
                single<DataStore> { fakeDataStore }
                single<DispatcherProvider> { dispatchers }
            })
        }

        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        advanceUntilIdle()

        val intent = shadowOf(controller.get().application).nextStartedActivity
        assertThat(intent.component?.className).isEqualTo(StartupActivity::class.java.name)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `when startup false sets content to MainScreen`() = runTest {
        val fakeDataStore = mockk<DataStore> {
            every { startup } returns flowOf(false)
        }
        val dispatchers = TestDispatchers(testScheduler)
        startKoin {
            modules(module {
                single<DataStore> { fakeDataStore }
                single<DispatcherProvider> { dispatchers }
            })
        }

        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        advanceUntilIdle()

        val shadowApp = shadowOf(controller.get().application)
        assertThat(shadowApp.nextStartedActivity).isNull()

        val content = controller.get().findViewById<ViewGroup>(android.R.id.content)
        val composeView = content.getChildAt(0)
        assertThat(composeView).isInstanceOf(ComposeView::class.java)
    }
}

