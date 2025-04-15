package com.d4rk.android.libs.apptoolkit.app.ads.domain.usecases

import com.d4rk.android.libs.apptoolkit.app.ads.data.ConsentRepository
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.usecases.RepositoryWithoutParam
import com.google.android.ump.ConsentInformation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoadConsentInfoUseCase(private val repository : ConsentRepository) : RepositoryWithoutParam<Flow<DataState<ConsentInformation , Errors>>> {
    override suspend fun invoke() : Flow<DataState<ConsentInformation , Errors>> = flow {
        emit(value = repository.loadConsentInfo())
    }
}