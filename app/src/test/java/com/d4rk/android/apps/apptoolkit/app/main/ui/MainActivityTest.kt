package com.d4rk.android.apps.apptoolkit.app.main.ui

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.TestDispatchers
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.app.main.utils.InAppUpdateHelper
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlin.lazyOf
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.fail
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityTest {

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun checkForUpdates_handlesUpdateFailureWithoutCrashing() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            mockkObject(InAppUpdateHelper)
            mockkStatic(AppUpdateManagerFactory::class)

            val activity = MainActivity()
            prepareLifecycle(activity)
            replaceDelegate(activity, "dispatchers", TestDispatchers(dispatcher))
            replaceDelegate(activity, "dataStore", mockk<DataStore>(relaxed = true))
            val launcher = mockk<ActivityResultLauncher<IntentSenderRequest>>(relaxed = true)
            setUpdateLauncher(activity, launcher)

            val appUpdateManager = mockk<AppUpdateManager>()
            every { AppUpdateManagerFactory.create(activity) } returns appUpdateManager
            coEvery { InAppUpdateHelper.performUpdate(appUpdateManager, launcher) } throws IllegalStateException("boom")

            val method = MainActivity::class.java.getDeclaredMethod("checkForUpdates").apply {
                isAccessible = true
            }

            method.invoke(activity)

            val parentJob = activity.lifecycleScope.coroutineContext[Job] ?: fail("Missing lifecycle job")
            val childJob = parentJob.children.firstOrNull() ?: fail("Expected launched coroutine")
            val completion = CompletableDeferred<Throwable?>()
            childJob.invokeOnCompletion { completion.complete(it) }

            dispatcher.scheduler.advanceUntilIdle()

            assertNull(completion.await(), "Coroutine should complete without propagating an exception")
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun prepareLifecycle(activity: MainActivity) {
        val lifecycle = activity.lifecycle
        if (lifecycle is LifecycleRegistry) {
            lifecycle.currentState = Lifecycle.State.RESUMED
        }
    }

    private fun setUpdateLauncher(
        activity: MainActivity,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
    ) {
        val field = MainActivity::class.java.getDeclaredField("updateResultLauncher")
        field.isAccessible = true
        field.set(activity, launcher)
    }

    private fun replaceDelegate(
        activity: MainActivity,
        name: String,
        value: Any,
    ) {
        val field = MainActivity::class.java.getDeclaredField("${name}\$delegate")
        field.isAccessible = true
        field.set(activity, lazyOf(value))
    }
}
