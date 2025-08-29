package com.d4rk.android.apps.apptoolkit.core.data.favorites

import android.content.Context
import android.content.Intent
import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.UnconfinedDispatcherExtension
import com.d4rk.android.apps.apptoolkit.core.broadcast.FavoritesChangedReceiver
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesRepositoryImplTest {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = UnconfinedDispatcherExtension()
    }

    @Test
    fun `observeFavorites forwards datastore flow`() = runTest(dispatcherExtension.testDispatcher) {
        val context = mockk<Context>()
        val dataStore = mockk<DataStore>()
        val favoritesFlow = MutableSharedFlow<Set<String>>()
        every { dataStore.favoriteApps } returns favoritesFlow
        val repository = FavoritesRepositoryImpl(
            context = context,
            dataStore = dataStore,
            ioDispatcher = dispatcherExtension.testDispatcher,
        )

        repository.observeFavorites().test {
            favoritesFlow.emit(setOf("pkg1"))
            assertThat(awaitItem()).containsExactly("pkg1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleFavorite toggles and broadcasts`() = runTest(dispatcherExtension.testDispatcher) {
        val context = mockk<Context>()
        val dataStore = mockk<DataStore>()
        coEvery { dataStore.toggleFavoriteApp(any()) } returns Unit
        val intentSlot = slot<Intent>()
        every { context.sendBroadcast(capture(intentSlot)) } returns Unit
        val repository = FavoritesRepositoryImpl(
            context = context,
            dataStore = dataStore,
            ioDispatcher = dispatcherExtension.testDispatcher,
        )

        repository.toggleFavorite("pkg.name")

        coVerify { dataStore.toggleFavoriteApp("pkg.name") }
        verify { context.sendBroadcast(any()) }
        assertThat(intentSlot.captured.action).isEqualTo(FavoritesChangedReceiver.ACTION_FAVORITES_CHANGED)
        assertThat(intentSlot.captured.getStringExtra(FavoritesChangedReceiver.EXTRA_PACKAGE_NAME)).isEqualTo("pkg.name")
    }
}

