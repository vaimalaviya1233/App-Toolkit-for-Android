package com.d4rk.android.libs.apptoolkit.app.onboarding.data.repository

import com.d4rk.android.libs.apptoolkit.app.onboarding.domain.repository.OnboardingRepository
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore

/**
 * Default implementation of [OnboardingRepository] backed by [CommonDataStore].
 */
class DefaultOnboardingRepository(
    private val dataStore: CommonDataStore
) : OnboardingRepository {

    override suspend fun setOnboardingCompleted() {
        dataStore.saveStartup(isFirstTime = false)
    }
}
