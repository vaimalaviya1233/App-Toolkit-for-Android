package com.d4rk.android.libs.apptoolkit.app.advanced.data

import com.d4rk.android.libs.apptoolkit.core.domain.model.Result

/**
 * Repository abstraction for cache-related operations.
 */
interface CacheRepository {
    /**
     * Clears the application's cache directories.
     *
     * @return [Result.Success] if all directories were deleted successfully or
     * [Result.Error] with the encountered [Exception].
     */
    suspend fun clearCache(): Result<Unit>
}
