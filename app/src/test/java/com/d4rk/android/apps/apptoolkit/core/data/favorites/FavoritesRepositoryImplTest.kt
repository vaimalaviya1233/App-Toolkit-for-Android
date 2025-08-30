package com.d4rk.android.apps.apptoolkit.core.data.favorites

import android.content.Context
import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesRepositoryImplTest {

    @Test
    fun observeFavorites_emitsUpdatedSetOnToggle() = runTest {
        val favoriteAppsFlow = MutableSharedFlow<Set<String>>(replay = 1)
        favoriteAppsFlow.tryEmit(emptySet())

        val dataStore = mockk<DataStore>()
        every { dataStore.favoriteApps } returns favoriteAppsFlow
        coEvery { dataStore.toggleFavoriteApp(any()) } coAnswers {
            val pkg = firstArg<String>()
            val current = favoriteAppsFlow.replayCache.firstOrNull()?.toMutableSet() ?: mutableSetOf()
            if (!current.add(pkg)) {
                current.remove(pkg)
            }
            favoriteAppsFlow.emit(current)
        }

        val context = mockk<Context>(relaxed = true)
        every { context.packageName } returns "com.test"
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val repository = FavoritesRepositoryImpl(context, dataStore, dispatcher)

        repository.observeFavorites().test {
            assertEquals(emptySet<String>(), awaitItem())

            repository.toggleFavorite("pkg1")
            assertEquals(setOf("pkg1"), awaitItem())

            repository.toggleFavorite("pkg2")
            assertEquals(setOf("pkg1", "pkg2"), awaitItem())

            repository.toggleFavorite("pkg1")
            assertEquals(setOf("pkg2"), awaitItem())
        }
    }
}

