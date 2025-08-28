package com.d4rk.android.libs.apptoolkit.app.advanced.ui

import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.advanced.data.CacheRepository
import com.d4rk.android.libs.apptoolkit.core.domain.model.Result
import com.d4rk.android.libs.apptoolkit.app.advanced.domain.actions.AdvancedSettingsEvent
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

private class FakeCacheRepository(private val result: Result<Unit>) : CacheRepository {
    override fun clearCache(): Flow<Result<Unit>> = flowOf(result)
}

class TestAdvancedSettingsViewModel {

    @Test
    fun `onClearCache emits success message`() = runTest {
        println("\uD83D\uDE80 [TEST] onClearCache emits success message")
        val viewModel = AdvancedSettingsViewModel(repository = FakeCacheRepository(Result.Success(Unit)))

        viewModel.onEvent(AdvancedSettingsEvent.ClearCache)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.data?.cacheClearMessage).isEqualTo(R.string.cache_cleared_success)
        viewModel.onEvent(AdvancedSettingsEvent.MessageShown)
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.data?.cacheClearMessage).isNull()
        println("\uD83C\uDFC1 [TEST DONE] onClearCache emits success message")
    }

    @Test
    fun `onClearCache emits error message when failure`() = runTest {
        println("\uD83D\uDE80 [TEST] onClearCache emits error message when failure")
        val viewModel = AdvancedSettingsViewModel(repository = FakeCacheRepository(Result.Error(Exception("fail"))))

        viewModel.onEvent(AdvancedSettingsEvent.ClearCache)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.data?.cacheClearMessage).isEqualTo(R.string.cache_cleared_error)
        println("\uD83C\uDFC1 [TEST DONE] onClearCache emits error message when failure")
    }
}
