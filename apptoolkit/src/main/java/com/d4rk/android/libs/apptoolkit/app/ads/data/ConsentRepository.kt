package com.d4rk.android.libs.apptoolkit.app.ads.data

import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.google.android.ump.ConsentInformation

interface ConsentRepository {
    suspend fun loadConsentInfo() : DataState<ConsentInformation , Errors>
}