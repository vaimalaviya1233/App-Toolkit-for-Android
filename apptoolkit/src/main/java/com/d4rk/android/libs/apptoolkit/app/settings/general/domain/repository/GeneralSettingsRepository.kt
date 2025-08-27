package com.d4rk.android.libs.apptoolkit.app.settings.general.domain.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository responsible for providing the content key for the General Settings screen.
 *
 * This implementation performs a lightweight validation on the provided key and
 * executes the work on the provided [CoroutineDispatcher] to keep data
 * operations off the UI layer and allow easier testing.
 */
interface GeneralSettingsRepository {
    /**
     * Returns a valid content key. If the provided key is null or blank, the
     * function throws an [IllegalArgumentException]. This function is marked as
     * [suspend] to ensure callers handle it from within a coroutine.
     */
    suspend fun getContentKey(contentKey: String?): String
}

class DefaultGeneralSettingsRepository(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : GeneralSettingsRepository {
    override suspend fun getContentKey(contentKey: String?): String = withContext(dispatcher) {
        if (contentKey.isNullOrBlank()) {
            throw IllegalArgumentException("Invalid content key")
        }
        contentKey
    }
}

