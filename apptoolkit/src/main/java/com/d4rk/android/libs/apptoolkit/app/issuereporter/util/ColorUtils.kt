package com.heinrichreimersoftware.androidissuereporter.util

import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils as CoreColorUtils

object ColorUtils {
    fun isDark(@ColorInt color: Int): Boolean {
        return CoreColorUtils.calculateLuminance(color) < 0.6
    }
}
