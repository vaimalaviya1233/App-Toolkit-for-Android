package com.d4rk.android.libs.apptoolkit.app.ads.data

import android.content.Context
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.utils.extensions.toError
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform

class ConsentRepositoryImpl(private val context : Context) : ConsentRepository {
    override suspend fun loadConsentInfo() : DataState<ConsentInformation , Errors> {
        return runCatching {
            val consentInfo : ConsentInformation = UserMessagingPlatform.getConsentInformation(context)
            DataState.Success<ConsentInformation , Errors>(data = consentInfo)
        }.getOrElse { throwable : Throwable ->
            DataState.Error(error = throwable.toError(default = Errors.UseCase.FAILED_TO_LOAD_CONSENT_INFO))
        }
    }
}