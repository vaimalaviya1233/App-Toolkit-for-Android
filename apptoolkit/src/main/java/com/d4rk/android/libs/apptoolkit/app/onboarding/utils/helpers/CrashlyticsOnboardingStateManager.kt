package com.d4rk.android.libs.apptoolkit.app.onboarding.utils.helpers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Simple state holder used by the Crashlytics onboarding screen to control
 * the visibility of the consent dialog.  Previously this behaviour was
 * implemented in a companion object. Converting it to an [object] keeps the
 * API the same while avoiding an unnecessary class wrapper and making the
 * intent explicit.
 */
object CrashlyticsOnboardingStateManager {
    var showCrashlyticsDialog by mutableStateOf(true)
        private set

    fun openDialog() {
        showCrashlyticsDialog = true
    }

    fun dismissDialog() {
        showCrashlyticsDialog = false
    }
}
