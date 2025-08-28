package com.d4rk.android.libs.apptoolkit.app.about.domain.repository

import com.d4rk.android.libs.apptoolkit.app.about.domain.model.ui.UiAboutScreen
import kotlinx.coroutines.flow.Flow

/**
 * Repository responsible for providing data for the about screen.
 */
interface AboutRepository {
    /**
     * Stream information displayed on the about screen.
     *
     * @return A [Flow] emitting [UiAboutScreen] data.
     */
    suspend fun getAboutInfoStream(): UiAboutScreen

    /**
     * Copy the provided [deviceInfo] string to the clipboard with the given [label].
     *
     * Implementations should handle threading to ensure this call is safe from the
     * main thread and can be executed off the UI thread when necessary.
     */
    suspend fun copyDeviceInfo(label: String, deviceInfo: String)
}
