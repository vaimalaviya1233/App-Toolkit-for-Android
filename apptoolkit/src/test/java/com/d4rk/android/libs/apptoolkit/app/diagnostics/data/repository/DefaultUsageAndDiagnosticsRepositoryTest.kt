package com.d4rk.android.libs.apptoolkit.app.diagnostics.data.repository

import com.d4rk.android.libs.apptoolkit.app.diagnostics.data.datasource.UsageAndDiagnosticsPreferencesDataSource
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.UnconfinedDispatcherExtension
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

private class FakeUsageAndDiagnosticsPreferencesDataSource : UsageAndDiagnosticsPreferencesDataSource {
    private val usage = MutableStateFlow(true)
    private val analytics = MutableStateFlow(true)
    private val adStorage = MutableStateFlow(true)
    private val adUserData = MutableStateFlow(true)
    private val adPersonalization = MutableStateFlow(true)

    override fun usageAndDiagnostics(default: Boolean) = usage
    override suspend fun saveUsageAndDiagnostics(isChecked: Boolean) { usage.emit(isChecked) }

    override fun analyticsConsent(default: Boolean) = analytics
    override suspend fun saveAnalyticsConsent(isGranted: Boolean) { analytics.emit(isGranted) }

    override fun adStorageConsent(default: Boolean) = adStorage
    override suspend fun saveAdStorageConsent(isGranted: Boolean) { adStorage.emit(isGranted) }

    override fun adUserDataConsent(default: Boolean) = adUserData
    override suspend fun saveAdUserDataConsent(isGranted: Boolean) { adUserData.emit(isGranted) }

    override fun adPersonalizationConsent(default: Boolean) = adPersonalization
    override suspend fun saveAdPersonalizationConsent(isGranted: Boolean) { adPersonalization.emit(isGranted) }
}

private class FakeBuildInfoProvider : BuildInfoProvider {
    override val isDebugBuild: Boolean = false
    override val versionName: String = ""
    override val versionCode: Int = 0
}

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultUsageAndDiagnosticsRepositoryTest {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = UnconfinedDispatcherExtension()
    }

    @Test
    fun `observeSettings reflects data source updates`() = runTest(dispatcherExtension.testDispatcher) {
        val dataSource = FakeUsageAndDiagnosticsPreferencesDataSource()
        val repository = DefaultUsageAndDiagnosticsRepository(
            dataSource = dataSource,
            configProvider = FakeBuildInfoProvider(),
            ioDispatcher = dispatcherExtension.testDispatcher,
        )

        // Initial state should reflect default true values
        assertThat(repository.observeSettings().first().usageAndDiagnostics).isTrue()

        repository.setUsageAndDiagnostics(false)
        advanceUntilIdle()

        assertThat(repository.observeSettings().first().usageAndDiagnostics).isFalse()
    }
}

