package com.d4rk.android.apps.apptoolkit.core.data.favorites

import android.content.Context
import android.util.Log
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.repository.FavoritesRepository
import com.d4rk.android.apps.apptoolkit.core.broadcast.FavoritesChangedReceiver
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class FavoritesRepositoryImpl(
    private val context: Context,
    private val dataStore: DataStore,
    private val dispatchers: DispatcherProvider
) : FavoritesRepository {
    override fun observeFavorites(): Flow<Set<String>> = dataStore.favoriteApps.flowOn(dispatchers.io)

    override suspend fun toggleFavorite(packageName: String) {
        withContext(dispatchers.io) {
            dataStore.toggleFavoriteApp(packageName)
            val intent = FavoritesChangedReceiver.createIntentWithPackage(context, packageName)
            runCatching {
                context.sendBroadcast(intent)
            }.onFailure { e ->
                Log.w("FavoritesRepositoryImpl", "Failed to send favorites broadcast", e)
            }
        }

    }
}

