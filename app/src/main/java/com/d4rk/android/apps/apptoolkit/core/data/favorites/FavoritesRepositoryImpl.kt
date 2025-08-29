package com.d4rk.android.apps.apptoolkit.core.data.favorites

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.repository.FavoritesRepository
import com.d4rk.android.apps.apptoolkit.core.broadcast.FavoritesChangedReceiver
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class FavoritesRepositoryImpl(
    private val context: Context,
    private val dataStore: DataStore,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FavoritesRepository {
    override fun observeFavorites(): Flow<Set<String>> = dataStore.favoriteApps.flowOn(ioDispatcher)

    override suspend fun toggleFavorite(packageName: String) {
        withContext(ioDispatcher) {
            dataStore.toggleFavoriteApp(packageName)
            val intent = Intent(FavoritesChangedReceiver.ACTION_FAVORITES_CHANGED).apply {
                component = ComponentName(context, FavoritesChangedReceiver::class.java)
                putExtra(FavoritesChangedReceiver.EXTRA_PACKAGE_NAME, packageName)
            }
            runCatching {
                context.sendBroadcast(intent)
            }.onFailure { e ->
                Log.w("FavoritesRepositoryImpl", "Failed to send favorites broadcast", e)
            }
        }

    }
}

