package com.d4rk.android.apps.apptoolkit.core.data.favorites

import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.repository.FavoritesRepository
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FavoritesRepositoryImpl(private val dataStore: DataStore) : FavoritesRepository {
    override fun observeFavorites(): Flow<Set<String>> = dataStore.favoriteApps

    override suspend fun toggleFavorite(packageName: String) {
        withContext(context = Dispatchers.IO) {
            dataStore.toggleFavoriteApp(packageName)
        }
    }
}

