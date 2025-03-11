package com.d4rk.android.libs.apptoolkit.core.utils.constants.datastore

/**
 * Defines constants for the names of different DataStore files used in the application.
 *
 * This class provides a centralized location for accessing DataStore file names,
 * ensuring consistency and avoiding hardcoding these values throughout the codebase.
 *
 * Each constant represents the name of a specific DataStore file, used to persist
 * different types of application data such as settings, user preferences, and
 * other application states.
 */
open class DataStoreNamesConstants {
    companion object {
        const val DATA_STORE_SETTINGS = "settings"
        const val DATA_STORE_LAST_USED = "last_used"
        const val DATA_STORE_STARTUP = "startup"
        const val DATA_STORE_THEME_MODE = "theme_mode"
        const val DATA_STORE_AMOLED_MODE = "amoled_mode"
        const val DATA_STORE_DYNAMIC_COLORS = "dynamic_colors"
        const val DATA_STORE_BOUNCY_BUTTONS = "bouncy_buttons"
        const val DATA_STORE_LANGUAGE = "language"
        const val DATA_STORE_USAGE_AND_DIAGNOSTICS = "usage_and_diagnostics"
        const val DATA_STORE_ADS = "ads"
    }
}