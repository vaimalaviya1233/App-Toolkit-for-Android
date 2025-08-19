package com.d4rk.android.libs.apptoolkit.app.advanced.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.d4rk.android.libs.apptoolkit.R
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

object CleanHelper {

    private const val TAG = "CleanHelper"

    suspend fun clearApplicationCache(
        context: Context,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) {
        val cacheDirectories: List<File> = listOf(context.cacheDir, context.codeCacheDir, context.filesDir)

        val results: List<Pair<File, Boolean>> = withContext(context = ioDispatcher) {
            coroutineScope {
                cacheDirectories
                    .map { directory ->
                        async {
                            val deleted = runCatching { directory.deleteRecursively() }
                                .getOrElse { false }
                            directory to deleted
                        }
                    }
                    .awaitAll()
            }
        }

        val failedDirs = results.filterNot { it.second }.map { it.first }
        if (failedDirs.isNotEmpty()) {
            Log.w(TAG, "Failed to delete directories: ${failedDirs.joinToString { it.path }}")
        }

        val allDeleted = failedDirs.isEmpty()
        val messageResId: Int = if (allDeleted) R.string.cache_cleared_success else R.string.cache_cleared_error

        withContext(context = Dispatchers.Main) {
            Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show()
        }
    }
}
