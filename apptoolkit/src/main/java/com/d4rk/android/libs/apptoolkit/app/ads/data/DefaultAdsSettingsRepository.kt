package com.d4rk.android.libs.apptoolkit.app.ads.data

import com.d4rk.android.libs.apptoolkit.app.ads.domain.repository.AdsSettingsRepository
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Default implementation of [AdsSettingsRepository] backed by [CommonDataStore].
 */
class DefaultAdsSettingsRepository(
    private val dataStore: CommonDataStore,
    buildInfoProvider: BuildInfoProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AdsSettingsRepository {

    override val defaultAdsEnabled: Boolean = !buildInfoProvider.isDebugBuild

    override fun observeAdsEnabled(): Flow<Boolean> =
        dataStore.ads(default = defaultAdsEnabled)
            .catch { throwable ->
                if (throwable is CancellationException) throw throwable
                emit(defaultAdsEnabled)
            }
            .flowOn(ioDispatcher)

    override suspend fun setAdsEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        dataStore.saveAds(isChecked = enabled)
    }
}
