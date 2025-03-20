package com.d4rk.android.libs.apptoolkit.app.advanced.utils

import android.content.Context
import android.widget.Toast
import com.d4rk.android.libs.apptoolkit.R
import java.io.File

object CleanHelper {

    fun clearApplicationCache(context : Context) {
        val cacheDirectories : List<File> = listOf(context.cacheDir , context.codeCacheDir , context.filesDir)

        var allDeleted = true
        for (directory in cacheDirectories) {
            if (! deleteRecursive(fileOrDirectory = directory)) {
                allDeleted = false
            }
        }

        val messageResId : Int = if (allDeleted) R.string.cache_cleared_success else R.string.cache_cleared_error

        Toast.makeText(context , context.getString(messageResId) , Toast.LENGTH_SHORT).show()
    }

    private fun deleteRecursive(fileOrDirectory : File) : Boolean {
        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles()?.let { children ->
                for (child in children) {
                    if (! deleteRecursive(child)) {
                        return false
                    }
                }
            }
        }
        return fileOrDirectory.delete()
    }
}