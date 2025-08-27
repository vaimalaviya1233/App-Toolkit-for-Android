package com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.providers

import android.app.Application
import com.d4rk.android.libs.apptoolkit.app.issuereporter.domain.model.DeviceInfo
import javax.inject.Inject

interface DeviceInfoProvider {
    suspend fun capture(): DeviceInfo
}

class DeviceInfoProviderImpl @Inject constructor(
    private val app: Application,
) : DeviceInfoProvider {
    override suspend fun capture(): DeviceInfo = DeviceInfo.create(app)
}
