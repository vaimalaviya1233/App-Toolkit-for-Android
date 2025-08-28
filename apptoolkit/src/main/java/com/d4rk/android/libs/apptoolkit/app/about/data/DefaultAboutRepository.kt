package com.d4rk.android.libs.apptoolkit.app.about.data

import android.content.Context
import com.d4rk.android.libs.apptoolkit.app.about.domain.model.ui.UiAboutScreen
import com.d4rk.android.libs.apptoolkit.app.about.domain.repository.AboutRepository
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.AboutSettingsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ClipboardHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Default implementation of [AboutRepository] that gathers device and build
 * information on an I/O dispatcher.
 */
class DefaultAboutRepository(
    private val deviceProvider: AboutSettingsProvider,
    private val configProvider: BuildInfoProvider,
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : AboutRepository {

    override fun getAboutInfoStream(): Flow<UiAboutScreen> =
        flow {
            val info = withContext(ioDispatcher) {
                UiAboutScreen(
                    appVersion = configProvider.appVersion,
                    appVersionCode = configProvider.appVersionCode,
                    deviceInfo = deviceProvider.deviceInfo,
                )
            }
            emit(info)
        }

    override suspend fun copyDeviceInfo(label: String, deviceInfo: String) {
        withContext(mainDispatcher) {
            ClipboardHelper.copyTextToClipboard(
                context = context,
                label = label,
                text = deviceInfo,
            )
        }
    }
}
