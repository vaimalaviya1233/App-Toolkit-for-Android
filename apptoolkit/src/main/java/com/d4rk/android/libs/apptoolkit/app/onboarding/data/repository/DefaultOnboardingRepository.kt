package com.d4rk.android.libs.apptoolkit.app.onboarding.data.repository

import com.d4rk.android.libs.apptoolkit.app.onboarding.domain.repository.OnboardingRepository
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn

/**
 * Default implementation of [OnboardingRepository] backed by [CommonDataStore].
 */
class DefaultOnboardingRepository(
    private val dataStore: CommonDataStore,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : OnboardingRepository {

    override fun observeOnboardingCompletion(): Flow<Boolean> =
        dataStore.startup
            .map { isFirstTime -> !isFirstTime }
            .flowOn(ioDispatcher)

    override suspend fun setOnboardingCompleted() {
        dataStore.saveStartup(isFirstTime = false)
    }
}
