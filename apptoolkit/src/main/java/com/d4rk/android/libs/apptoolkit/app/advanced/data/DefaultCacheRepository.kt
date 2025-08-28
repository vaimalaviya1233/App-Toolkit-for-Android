package com.d4rk.android.libs.apptoolkit.app.advanced.data

import android.content.Context
import com.d4rk.android.libs.apptoolkit.core.domain.model.Result
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

    override suspend fun clearCache(): Result<Unit> = withContext(ioDispatcher) {
        val cacheDirectories: List<File> = listOf(
            context.cacheDir,
            context.codeCacheDir,
            context.filesDir,
        )

        return@withContext try {
            val allDeleted = coroutineScope {
                cacheDirectories
                    .map { directory -> async { directory.deleteRecursively() } }
                    .awaitAll()
                    .all { it }
            }
            if (allDeleted) {
                Result.Success(Unit)
            } else {
                Result.Error(Exception("Failed to clear cache"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
