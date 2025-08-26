package com.d4rk.android.libs.apptoolkit.app.advanced.utils

import android.content.Context
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

    suspend fun clearApplicationCache(
        context: Context,
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
        mainDispatcher: CoroutineDispatcher = Dispatchers.Main
    ) {
        val cacheDirectories : List<File> = listOf(context.cacheDir , context.codeCacheDir , context.filesDir)

        val allDeleted : Boolean = withContext(context = ioDispatcher) {
            coroutineScope {
                cacheDirectories
                    .map { directory ->
                        async { directory.deleteRecursively() }
                    }
                    .awaitAll()
                    .all { it }
            }
        }

        val messageResId : Int = if (allDeleted) R.string.cache_cleared_success else R.string.cache_cleared_error

        withContext(context = mainDispatcher) {
            Toast.makeText(context , context.getString(messageResId) , Toast.LENGTH_SHORT).show()
        }
    }
}
