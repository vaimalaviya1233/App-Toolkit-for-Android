package com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class ObserveFavoriteAppsUseCase {
    val flow = MutableSharedFlow<DataState<List<AppInfo>, RootError>>()
    suspend operator fun invoke(): Flow<DataState<List<AppInfo>, RootError>> = flow
}
