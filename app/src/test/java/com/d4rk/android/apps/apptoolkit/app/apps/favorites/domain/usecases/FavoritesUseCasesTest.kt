package com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases

import com.d4rk.android.apps.apptoolkit.app.apps.favorites.FakeFavoritesRepository
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.StandardDispatcherExtension
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ObserveFavoritesUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ToggleFavoriteUseCase

class FavoritesUseCasesTest {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = StandardDispatcherExtension()
    }

    @Test
    fun `observe favorites emits initial set`() = runTest(dispatcherExtension.testDispatcher) {
        val repository = FakeFavoritesRepository(initialFavorites = setOf("pkg1", "pkg2"))
        val useCase = ObserveFavoritesUseCase(repository)

        useCase().test {
            assertThat(awaitItem()).containsExactly("pkg1", "pkg2")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle favorite updates repository`() = runTest(dispatcherExtension.testDispatcher) {
        val repository = FakeFavoritesRepository()
        val toggleUseCase = ToggleFavoriteUseCase(repository)
        val observeUseCase = ObserveFavoritesUseCase(repository)

        observeUseCase().test {
            assertThat(awaitItem()).isEmpty()
            toggleUseCase("pkg")
            assertThat(awaitItem()).containsExactly("pkg")
            toggleUseCase("pkg")
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
