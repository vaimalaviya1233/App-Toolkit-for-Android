package com.d4rk.android.apps.apptoolkit.core.data.favorites

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.d4rk.android.apps.apptoolkit.core.broadcast.FavoritesChangedReceiver
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.ContinuationInterceptor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var dataStore: DataStore
    private lateinit var dispatchers: DispatcherProvider
    private lateinit var ioDispatcher: CoroutineDispatcher
    private lateinit var repository: FavoritesRepositoryImpl

    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
        every { context.packageName } returns "com.d4rk.android.apps.apptoolkit"

        dataStore = mockk()
        dispatchers = mockk(relaxed = true)
        ioDispatcher = UnconfinedTestDispatcher()
        every { dispatchers.io } returns ioDispatcher

        repository = FavoritesRepositoryImpl(
            context = context,
            dataStore = dataStore,
            dispatchers = dispatchers,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun observeFavorites_delegatesToDataStoreAndUsesIoDispatcher() = runTest {
        val expectedFavorites = setOf("com.example.app")
        val capturedDispatcher = AtomicReference<CoroutineDispatcher?>()
        every { dataStore.favoriteApps } returns flow {
            val dispatcher = currentCoroutineContext()[ContinuationInterceptor] as? CoroutineDispatcher
            capturedDispatcher.set(dispatcher)
            emit(expectedFavorites)
        }

        val result = repository.observeFavorites().first()

        assertEquals(expectedFavorites, result)
        assertSame(ioDispatcher, capturedDispatcher.get())
        verify(exactly = 1) { dataStore.favoriteApps }
        verify(atLeast = 1) { dispatchers.io }
    }

    @Test
    fun toggleFavorite_togglesAndBroadcastsIntent() = runTest {
        val packageName = "com.example.app"
        coJustRun { dataStore.toggleFavoriteApp(packageName) }
        val intentSlot = slot<Intent>()
        every { context.sendBroadcast(capture(intentSlot)) } returns Unit

        repository.toggleFavorite(packageName)

        coVerify(exactly = 1) { dataStore.toggleFavoriteApp(packageName) }
        verify(exactly = 1) { context.sendBroadcast(any()) }

        val capturedIntent = intentSlot.captured
        assertEquals(FavoritesChangedReceiver.ACTION_FAVORITES_CHANGED, capturedIntent.action)
        val expectedComponent = ComponentName(
            "com.d4rk.android.apps.apptoolkit",
            FavoritesChangedReceiver::class.java.name,
        )
        assertEquals(expectedComponent, capturedIntent.component)
        assertEquals(
            packageName,
            capturedIntent.getStringExtra(FavoritesChangedReceiver.EXTRA_PACKAGE_NAME),
        )
    }

    @Test
    fun toggleFavorite_logsWarningWhenBroadcastFails() = runTest {
        val packageName = "com.example.app"
        coJustRun { dataStore.toggleFavoriteApp(packageName) }
        val failure = IllegalStateException("boom")
        every { context.sendBroadcast(any()) } throws failure
        mockkStatic(Log::class)
        every { Log.w("FavoritesRepositoryImpl", "Failed to send favorites broadcast", failure) } returns 0

        repository.toggleFavorite(packageName)

        coVerify(exactly = 1) { dataStore.toggleFavoriteApp(packageName) }
        verify(exactly = 1) { context.sendBroadcast(any()) }
        verify(exactly = 1) {
            Log.w("FavoritesRepositoryImpl", "Failed to send favorites broadcast", failure)
        }
    }
}
