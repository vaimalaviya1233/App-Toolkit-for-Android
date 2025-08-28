package com.d4rk.android.libs.apptoolkit.app.advanced.data

import android.content.Context
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Default implementation of [CacheRepository] that clears cache directories using the provided [Context].
 */
class DefaultCacheRepository(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CacheRepository {

    override suspend fun clearCache(): Boolean = withContext(ioDispatcher) {
        val cacheDirectories: List<File> = listOf(
            context.cacheDir,
            context.codeCacheDir,
            context.filesDir,
        )

        return@withContext coroutineScope {
            cacheDirectories
                .map { directory -> async { directory.deleteRecursively() } }
                .awaitAll()
                .all { it }
        }
    }
}
