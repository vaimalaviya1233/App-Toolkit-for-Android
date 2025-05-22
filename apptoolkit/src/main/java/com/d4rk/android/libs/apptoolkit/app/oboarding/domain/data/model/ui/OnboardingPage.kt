package com.d4rk.android.libs.apptoolkit.app.oboarding.domain.data.model.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.graphics.shapes.RoundedPolygon

data class OnboardingPage(
    val key: String,
    val title: String,
    val description: String,
    val imageVector: ImageVector,
    val shape: RoundedPolygon? = null,
    val isEnabled: Boolean = true // <-- Use this to skip pages
)