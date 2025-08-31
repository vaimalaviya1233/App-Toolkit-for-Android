package com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class ObserveFavoritesUseCase {
    val flow = MutableSharedFlow<Set<String>>(replay = 1)
    suspend operator fun invoke(): Flow<Set<String>> = flow
}
