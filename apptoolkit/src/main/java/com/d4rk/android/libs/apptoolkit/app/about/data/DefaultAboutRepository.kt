package com.d4rk.android.libs.apptoolkit.app.about.data

import com.d4rk.android.libs.apptoolkit.app.about.domain.model.ui.UiAboutScreen
import com.d4rk.android.libs.apptoolkit.app.about.domain.repository.AboutRepository
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.AboutSettingsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Default implementation of [AboutRepository] that gathers device and build
 * information on an I/O dispatcher.
 */
class DefaultAboutRepository(
    private val deviceProvider: AboutSettingsProvider,
    private val configProvider: BuildInfoProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AboutRepository {

    override suspend fun getAboutInfo(): Result<UiAboutScreen> = withContext(ioDispatcher) {
        runCatching {
            UiAboutScreen(
                appVersion = configProvider.appVersion,
                appVersionCode = configProvider.appVersionCode,
                deviceInfo = deviceProvider.deviceInfo,
            )
        }
    }
}
