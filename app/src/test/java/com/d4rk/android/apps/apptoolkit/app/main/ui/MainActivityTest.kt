
package com.d4rk.android.apps.apptoolkit.app.main.ui

import androidx.lifecycle.lifecycleScope
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ConsentFormHelper
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.Robolectric
import org.robolectric.junit5.RobolectricExtension
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(RobolectricExtension::class)
class MainActivityTest {

    private var mainDispatcherSet: Boolean = false

    @BeforeEach
    fun setUp() {
        mockkStatic(UserMessagingPlatform::class)
        mockkObject(ConsentFormHelper)
    }

    @AfterEach
    fun tearDown() {
        if (mainDispatcherSet) {
            Dispatchers.resetMain()
            mainDispatcherSet = false
        }
        unmockkAll()
    }

    @Test
    fun `checkUserConsent shows consent form when consent info is available`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        mainDispatcherSet = true

        val activity = Robolectric.buildActivity(MainActivity::class.java).get().apply {
            overrideDispatcherProvider(TestDispatcherProvider(dispatcher))
            overrideDataStore(mockk(relaxed = true))
        }

        val consentInfo = mockk<ConsentInformation>()
        every { UserMessagingPlatform.getConsentInformation(activity) } returns consentInfo
        every { ConsentFormHelper.showConsentFormIfRequired(activity, consentInfo) } just Runs

        invokeCheckUserConsent(activity)
        advanceUntilIdle()

        verify(exactly = 1) { UserMessagingPlatform.getConsentInformation(activity) }
        verify(exactly = 1) { ConsentFormHelper.showConsentFormIfRequired(activity, consentInfo) }
    }

    @Test
    fun `checkUserConsent completes when consent information throws`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        mainDispatcherSet = true

        val activity = Robolectric.buildActivity(MainActivity::class.java).get().apply {
            overrideDispatcherProvider(TestDispatcherProvider(dispatcher))
            overrideDataStore(mockk(relaxed = true))
        }

        val failure = IllegalStateException("failed to load consent information")
        every { UserMessagingPlatform.getConsentInformation(activity) } throws failure
        every { ConsentFormHelper.showConsentFormIfRequired(any(), any()) } just Runs

        assertDoesNotThrow {
            invokeCheckUserConsent(activity)
            advanceUntilIdle()
        }

        verify(exactly = 1) { UserMessagingPlatform.getConsentInformation(activity) }
        verify(exactly = 0) { ConsentFormHelper.showConsentFormIfRequired(any(), any()) }

        val parentJob = activity.lifecycleScope.coroutineContext[Job]
        assertNotNull(parentJob)
        assertTrue(parentJob.children.none { it.isActive })
    }
}

private fun MainActivity.overrideDispatcherProvider(provider: DispatcherProvider) {
    val field = MainActivity::class.java.getDeclaredField("dispatchers$delegate")
    field.isAccessible = true
    field.set(this, lazyOf(provider))
}

private fun MainActivity.overrideDataStore(dataStore: DataStore) {
    val field = MainActivity::class.java.getDeclaredField("dataStore$delegate")
    field.isAccessible = true
    field.set(this, lazyOf(dataStore))
}

private fun invokeCheckUserConsent(activity: MainActivity) {
    val method = MainActivity::class.java.getDeclaredMethod("checkUserConsent")
    method.isAccessible = true
    method.invoke(activity)
}

private class TestDispatcherProvider(
    private val dispatcher: CoroutineDispatcher
) : DispatcherProvider {
    override val main: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
    override val unconfined: CoroutineDispatcher = dispatcher
}
