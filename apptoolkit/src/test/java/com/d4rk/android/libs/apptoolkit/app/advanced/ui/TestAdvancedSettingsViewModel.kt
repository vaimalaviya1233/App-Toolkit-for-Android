package com.d4rk.android.libs.apptoolkit.app.advanced.ui

import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.advanced.data.CacheRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

private class FakeCacheRepository(var result: Boolean) : CacheRepository {
    override suspend fun clearCache(): Boolean = result
}

class TestAdvancedSettingsViewModel {

    @Test
    fun `onClearCache emits success message`() = runTest {
        println("\uD83D\uDE80 [TEST] onClearCache emits success message")
        val viewModel = AdvancedSettingsViewModel(repository = FakeCacheRepository(true))

        viewModel.onClearCache()

        assertThat(viewModel.uiState.value.cacheClearMessage).isEqualTo(R.string.cache_cleared_success)
        viewModel.onMessageShown()
        assertThat(viewModel.uiState.value.cacheClearMessage).isNull()
        println("\uD83C\uDFC1 [TEST DONE] onClearCache emits success message")
    }

    @Test
    fun `onClearCache emits error message when failure`() = runTest {
        println("\uD83D\uDE80 [TEST] onClearCache emits error message when failure")
        val viewModel = AdvancedSettingsViewModel(repository = FakeCacheRepository(false))

        viewModel.onClearCache()

        assertThat(viewModel.uiState.value.cacheClearMessage).isEqualTo(R.string.cache_cleared_error)
        println("\uD83C\uDFC1 [TEST DONE] onClearCache emits error message when failure")
    }
}
