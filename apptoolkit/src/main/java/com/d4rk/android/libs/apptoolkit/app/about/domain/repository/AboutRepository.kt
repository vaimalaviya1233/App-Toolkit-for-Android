package com.d4rk.android.libs.apptoolkit.app.about.domain.repository

import com.d4rk.android.libs.apptoolkit.app.about.domain.model.ui.UiAboutScreen

/**
 * Repository responsible for providing data for the about screen.
 */
interface AboutRepository {
    /**
     * Fetch information displayed on the about screen.
     */
    suspend fun getAboutInfo(): UiAboutScreen
}
