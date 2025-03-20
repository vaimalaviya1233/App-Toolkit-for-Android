package com.d4rk.android.libs.apptoolkit.app.privacy.routes.ads.domain.usecases

import android.content.Context
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.usecases.Repository
import com.d4rk.android.libs.apptoolkit.core.utils.extensions.toError
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoadConsentInfoUseCase(private val context : Context) {
    operator fun invoke() : Flow<DataState<ConsentInformation , Errors>> = flow {
        runCatching {
            val consentInformation = UserMessagingPlatform.getConsentInformation(context)
            emit(DataState.Success(data = consentInformation))
        }.onFailure { throwable ->
            emit(DataState.Error(error = throwable.toError(default = Errors.UseCase.FAILED_TO_LOAD_CONSENT_INFO)))
        }
    }
}
