package com.d4rk.android.apps.apptoolkit.core.data.favorites

import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.core.broadcast.FavoritesChangedReceiver
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesRepositoryImplTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Test
    fun `observeFavorites emits underlying datastore flow`() = runTest(dispatcher) {
        val favoritesFlow = MutableStateFlow(setOf("one"))
        val dataStore = mockk<DataStore>()
        every { dataStore.favoriteApps } returns favoritesFlow

        val repository = FavoritesRepositoryImpl(
            context = mockk(relaxed = true),
            dataStore = dataStore,
            ioDispatcher = dispatcher,
        )

        repository.observeFavorites().test {
            assertThat(awaitItem()).containsExactly("one")
            favoritesFlow.value = setOf("one", "two")
            assertThat(awaitItem()).containsExactly("one", "two")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleFavorite updates datastore and sends broadcast`() = runTest(dispatcher) {
        val fakeStore = FakeDataStore()
        val dataStore = mockk<DataStore>()
        every { dataStore.favoriteApps } returns fakeStore.favorites
        coEvery { dataStore.toggleFavoriteApp(any()) } coAnswers { fakeStore.toggleFavoriteApp(firstArg()) }

        val receiver = RecordingReceiver()
        val base = mockk<Context>(relaxed = true) { every { packageName } returns "pkg" }
        val context = RecordingContext(base, receiver)

        val repository = FavoritesRepositoryImpl(context, dataStore, dispatcher)
        val pkg = "com.test.app"

        repository.toggleFavorite(pkg)

        assertThat(fakeStore.favorites.value).containsExactly(pkg)
        val sent = receiver.intent
        assertThat(sent?.action).isEqualTo(FavoritesChangedReceiver.ACTION_FAVORITES_CHANGED)
        assertThat(sent?.getStringExtra(FavoritesChangedReceiver.EXTRA_PACKAGE_NAME)).isEqualTo(pkg)
    }

    private class FakeDataStore {
        val favorites = MutableStateFlow<Set<String>>(emptySet())

        suspend fun toggleFavoriteApp(packageName: String) {
            val current = favorites.value.toMutableSet()
            if (!current.add(packageName)) {
                current.remove(packageName)
            }
            favorites.value = current
        }
    }

    private class RecordingReceiver : BroadcastReceiver() {
        var intent: Intent? = null
        override fun onReceive(context: Context?, intent: Intent?) {
            this.intent = intent
        }
    }

    private class RecordingContext(base: Context, private val receiver: RecordingReceiver) : ContextWrapper(base) {
        override fun sendBroadcast(intent: Intent?) {
            receiver.onReceive(this, intent)
        }
    }
}

