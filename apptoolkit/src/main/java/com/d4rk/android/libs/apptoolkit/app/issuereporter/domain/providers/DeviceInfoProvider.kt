package com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.providers

import android.app.Application
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.DeviceInfo
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.AppDispatchers
import javax.inject.Inject
import kotlinx.coroutines.withContext

interface DeviceInfoProvider {
    suspend fun capture(): DeviceInfo
}

class DeviceInfoProviderImpl @Inject constructor(
    private val app: Application,
    private val dispatchers: AppDispatchers,
) : DeviceInfoProvider {
    override suspend fun capture(): DeviceInfo = withContext(dispatchers.io) {
        DeviceInfo.create(app)
    }
}
