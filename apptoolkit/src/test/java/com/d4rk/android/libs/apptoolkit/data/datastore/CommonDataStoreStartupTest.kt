package com.d4rk.android.libs.apptoolkit.data.datastore

import app.cash.turbine.test
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import android.content.Context
import com.d4rk.android.libs.apptoolkit.core.di.TestDispatchers
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.UnconfinedDispatcherExtension
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.io.File

class CommonDataStoreStartupTest {
    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = UnconfinedDispatcherExtension()
    }

    @Test
    fun `getStartupPage returns default then persisted value`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] getStartupPage returns default then persisted value")
        val memoryStore: DataStore<Preferences> = PreferenceDataStoreFactory.createWithMemory(scope = this)

        val context = mockk<Context>()
        every { context.applicationContext } returns context
        val tmpDir = File("build/tmp/commonDataStoreTest").apply { mkdirs() }
        every { context.filesDir } returns tmpDir

        val store = CommonDataStore(context, TestDispatchers(dispatcherExtension.testDispatcher))
        val dataStoreField = CommonDataStore::class.java.getDeclaredField("dataStore")
        dataStoreField.isAccessible = true
        dataStoreField.set(store, memoryStore)

        val defaultRoute = "home"
        store.getStartupPage(defaultRoute).test {
            assertThat(awaitItem()).isEqualTo(defaultRoute)
            cancelAndIgnoreRemainingEvents()
        }

        val savedRoute = "settings"
        store.saveStartupPage(savedRoute)

        store.getStartupPage(defaultRoute).test {
            assertThat(awaitItem()).isEqualTo(savedRoute)
            cancelAndIgnoreRemainingEvents()
        }

        println("\uD83C\uDFC1 [TEST DONE] getStartupPage returns default then persisted value")
    }
}

