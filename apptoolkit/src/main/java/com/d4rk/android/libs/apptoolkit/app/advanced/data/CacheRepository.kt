package com.d4rk.android.libs.apptoolkit.app.advanced.data

/**
 * Repository abstraction for cache-related operations.
 */
interface CacheRepository {
    /**
     * Clears the application's cache directories.
     *
     * @return `true` if all directories were deleted successfully, `false` otherwise.
     */
    suspend fun clearCache(): Boolean
}
