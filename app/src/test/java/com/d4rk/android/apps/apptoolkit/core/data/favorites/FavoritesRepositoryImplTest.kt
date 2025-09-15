package com.d4rk.android.apps.apptoolkit.core.data.favorites

import android.content.Context
import android.content.Intent
import android.util.Log
import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.TestDispatchers
import com.d4rk.android.apps.apptoolkit.core.broadcast.FavoritesChangedReceiver
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesRepositoryImplTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)
    private val dispatchers = TestDispatchers(dispatcher)
    private val context = mockk<Context>(relaxed = true)
    private val dataStore = mockk<DataStore>(relaxed = true)

    @BeforeEach
    fun setUp() {
        clearMocks(context, dataStore, answers = true)
    }

    @Test
    fun `observeFavorites relays DataStore updates`() = runTest(scheduler = scheduler) {
        val favoritesFlow = MutableSharedFlow<Set<String>>()
        every { dataStore.favoriteApps } returns favoritesFlow
        val repository = FavoritesRepositoryImpl(context, dataStore, dispatchers)

        repository.observeFavorites().test {
            val first = setOf("pkg.one", "pkg.two")
            favoritesFlow.emit(first)
            assertThat(awaitItem()).isEqualTo(first)

            val second = setOf("pkg.three")
            favoritesFlow.emit(second)
            assertThat(awaitItem()).isEqualTo(second)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleFavorite mutates datastore and broadcasts package name`() = runTest(scheduler = scheduler) {
        val packageName = "com.example.app"
        every { context.packageName } returns packageName
        val intentSlot = slot<Intent>()
        coEvery { dataStore.toggleFavoriteApp("pkg.name") } just Runs
        every { context.sendBroadcast(capture(intentSlot)) } just Runs
        val repository = FavoritesRepositoryImpl(context, dataStore, dispatchers)

        repository.toggleFavorite("pkg.name")

        coVerify(exactly = 1) { dataStore.toggleFavoriteApp("pkg.name") }
        verify(exactly = 1) { context.sendBroadcast(any()) }
        val intent = intentSlot.captured
        assertThat(intent.action).isEqualTo(FavoritesChangedReceiver.ACTION_FAVORITES_CHANGED)
        assertThat(intent.component?.className).isEqualTo(FavoritesChangedReceiver::class.java.name)
        assertThat(intent.component?.packageName).isEqualTo(packageName)
        assertThat(intent.getStringExtra(FavoritesChangedReceiver.EXTRA_PACKAGE_NAME)).isEqualTo("pkg.name")
    }

    @Test
    fun `toggleFavorite logs failure when broadcast throws`() = runTest(scheduler = scheduler) {
        every { context.packageName } returns "com.example.app"
        coEvery { dataStore.toggleFavoriteApp("pkg.name") } just Runs
        every { context.sendBroadcast(any()) } throws IllegalStateException("boom")
        val repository = FavoritesRepositoryImpl(context, dataStore, dispatchers)

        mockkStatic(Log::class)
        every { Log.w(any(), any<String>(), any()) } returns 0

        try {
            repository.toggleFavorite("pkg.name")

            coVerify(exactly = 1) { dataStore.toggleFavoriteApp("pkg.name") }
            verify(exactly = 1) { context.sendBroadcast(any()) }
            verify(exactly = 1) {
                Log.w("FavoritesRepositoryImpl", "Failed to send favorites broadcast", any())
            }
        } finally {
            unmockkStatic(Log::class)
        }
    }
}
