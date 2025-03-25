package com.d4rk.android.libs.apptoolkit.app.ads.domain.usecases

import android.content.Context
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.usecases.RepositoryWithoutParam
import com.d4rk.android.libs.apptoolkit.core.utils.extensions.toError
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoadConsentInfoUseCase(private val context: Context) : RepositoryWithoutParam<Flow<DataState<ConsentInformation , Errors>>> {
    override suspend operator fun invoke(): Flow<DataState<ConsentInformation, Errors>> = flow {
        runCatching {
            val consentInformation : ConsentInformation = UserMessagingPlatform.getConsentInformation(context)
            emit(value = DataState.Success(data = consentInformation))
        }.onFailure { throwable : Throwable ->
            emit(value = DataState.Error(error = throwable.toError(default = Errors.UseCase.FAILED_TO_LOAD_CONSENT_INFO)))
        }
    }
}