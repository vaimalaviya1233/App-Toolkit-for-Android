package com.d4rk.android.apps.apptoolkit.core.data.favorites

import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.repository.FavoritesRepository
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import kotlinx.coroutines.flow.Flow

class FavoritesRepositoryImpl(private val dataStore: DataStore) : FavoritesRepository {
    override fun observeFavorites(): Flow<Set<String>> = dataStore.favoriteApps

    override suspend fun toggleFavorite(packageName: String) {
        dataStore.toggleFavoriteApp(packageName)
    }
}

