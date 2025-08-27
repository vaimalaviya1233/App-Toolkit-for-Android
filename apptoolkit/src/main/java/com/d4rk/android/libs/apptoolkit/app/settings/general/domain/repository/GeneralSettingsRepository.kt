package com.d4rk.android.libs.apptoolkit.app.settings.general.domain.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Repository responsible for providing the content key for the General Settings screen.
 *
 * This implementation performs a lightweight validation on the provided key and
 * executes the work on the provided [CoroutineDispatcher] to keep data
 * operations off the UI layer and allow easier testing.
 */
interface GeneralSettingsRepository {
    /**
     * Returns a [Flow] that emits a valid content key. If the provided key is
     * null or blank, the flow throws an [IllegalArgumentException] when
     * collected. Using a [Flow] allows the repository to expose asynchronous
     * data streams in line with recommended architecture guidelines.
     */
    fun getContentKey(contentKey: String?): Flow<String>
}

class DefaultGeneralSettingsRepository(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : GeneralSettingsRepository {
    override fun getContentKey(contentKey: String?): Flow<String> = flow {
        if (contentKey.isNullOrBlank()) {
            throw IllegalArgumentException("Invalid content key")
        }
        emit(contentKey)
    }.flowOn(dispatcher)
}

