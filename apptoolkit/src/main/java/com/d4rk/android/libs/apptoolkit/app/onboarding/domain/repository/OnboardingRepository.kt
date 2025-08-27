package com.d4rk.android.libs.apptoolkit.app.onboarding.domain.repository

/**
 * Abstraction for onboarding-related data operations.
 */
interface OnboardingRepository {
    suspend fun setOnboardingCompleted()
}
