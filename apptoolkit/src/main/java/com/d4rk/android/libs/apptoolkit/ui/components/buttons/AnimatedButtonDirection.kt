package com.d4rk.android.libs.apptoolkit.ui.components.buttons

import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import com.d4rk.android.libs.apptoolkit.ui.components.modifiers.bounceClick

@Composable
fun AnimatedButtonDirection(
    modifier: Modifier = Modifier ,
    visible: Boolean = true ,
    icon: ImageVector ,
    contentDescription: String? ,
    onClick: () -> Unit ,
    durationMillis: Int = 500 ,
    autoAnimate: Boolean = true ,
    fromRight: Boolean = false
) {
    val animatedVisibility : MutableState<Boolean> = remember { mutableStateOf(value = false) }
    val view : View = LocalView.current

    LaunchedEffect(visible) {
        if (autoAnimate && visible) {
            animatedVisibility.value = true
        } else if (!visible) {
            animatedVisibility.value = false
        }
    }

    AnimatedVisibility(
        visible = animatedVisibility.value && visible,
        enter = fadeIn(animationSpec = tween(durationMillis = durationMillis)) +
                slideInHorizontally(initialOffsetX = { if (fromRight) it else -it }, animationSpec = tween(durationMillis = durationMillis)),
        exit = fadeOut(animationSpec = tween(durationMillis = durationMillis)) +
                slideOutHorizontally(targetOffsetX = { if (fromRight) it else -it }, animationSpec = tween(durationMillis = durationMillis))
    ) {
        IconButton(
            modifier = modifier.bounceClick(),
            onClick = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                onClick()
            }
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    }
}