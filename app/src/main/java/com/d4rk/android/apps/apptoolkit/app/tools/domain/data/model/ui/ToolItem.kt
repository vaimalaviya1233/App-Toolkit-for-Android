package com.d4rk.android.apps.apptoolkit.app.tools.domain.data.model.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.d4rk.android.apps.apptoolkit.app.tools.domain.data.model.ToolActionType

data class ToolItem(
    val id: String,
    val icon: ImageVector,
    val iconBackgroundColor: Color,
    val title: String,
    val subtitle: String,
    val actionType: ToolActionType,
    val destinationRoute: String? = null
)