package com.d4rk.android.libs.apptoolkit.core.ui.components.buttons.fab

import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnimatedFloatingActionButton(modifier : Modifier = Modifier , isVisible : Boolean , icon : ImageVector , contentDescription : String? = null , onClick : () -> Unit) {
    val hapticFeedback : HapticFeedback = LocalHapticFeedback.current
    val view : View = LocalView.current
    var checked by remember { mutableStateOf(false) }

    AnimatedVisibility(visible = isVisible , enter = scaleIn() + fadeIn() , exit = scaleOut() + fadeOut()) {
        ToggleFloatingActionButton(
            checked = checked,
            onCheckedChange = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                hapticFeedback.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.ContextClick)
                checked = it
                onClick()
            },
            modifier = modifier.bounceClick()
        ) {
            Icon(imageVector = icon , contentDescription = contentDescription)
        }
    }
}
