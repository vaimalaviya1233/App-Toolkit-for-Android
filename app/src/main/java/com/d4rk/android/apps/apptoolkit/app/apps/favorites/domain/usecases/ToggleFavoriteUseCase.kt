package com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases

import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.repository.FavoritesRepository
import com.d4rk.android.libs.apptoolkit.core.domain.usecases.Repository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ToggleFavoriteUseCase(
    private val repository: FavoritesRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : Repository<String, Unit> {
    override suspend operator fun invoke(param: String) {
        withContext(dispatcher) {
            repository.toggleFavorite(param)
        }
    }
}

