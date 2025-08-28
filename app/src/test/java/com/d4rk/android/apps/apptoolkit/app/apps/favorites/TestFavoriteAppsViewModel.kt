package com.d4rk.android.apps.apptoolkit.app.apps.favorites

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.StandardDispatcherExtension
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class TestFavoriteAppsViewModel : TestFavoriteAppsViewModelBase() {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = StandardDispatcherExtension()
    }

    @Test
    fun `toggle favorite throws after load`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] toggle favorite throws after load")
        val apps = listOf(AppInfo("App", "pkg", "url"))
        setup(
            fetchApps = apps,
            initialFavorites = emptySet(),
            toggleError = RuntimeException("fail"),
            dispatcher = dispatcherExtension.testDispatcher
        )

        viewModel.favorites.test {
            awaitItem()
            viewModel.toggleFavorite("pkg")
            runCurrent()
            expectNoEvents()
            assertThat(viewModel.favorites.value.contains("pkg")).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
        println("\uD83C\uDFC1 [TEST DONE] toggle favorite throws after load")
    }
}
